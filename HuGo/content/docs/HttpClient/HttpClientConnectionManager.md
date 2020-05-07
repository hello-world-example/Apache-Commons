# HttpClient 链接池

> - HttpClient 4.5 [官方文档](http://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/index.html)、[官方示例](http://hc.apache.org/httpcomponents-client-4.5.x/examples.html)



## 链接管理器 的实现

- `HttpClientConnectionManager` 链接管理器
  - BasicHttpClientConnectionManager ：只维护一个连接，且是只能单线程访问（`synchronized`）
  - **`PoolingHttpClientConnectionManager`** ： 连接池（池化链接管理器）



## PoolingHttpClientConnectionManager

### 构造方式

```java
public class PoolingHttpClientConnectionManager
  implements HttpClientConnectionManager, 
             // 定义了 如何设置 总计最大链接数、每个 Host 的最大链接数，获取 PoolStats 统计
             ConnPoolControl<HttpRoute>,  
             Closeable {

  // ...

  /**
   * @since 4.4
   */
  public PoolingHttpClientConnectionManager(
    final HttpClientConnectionOperator httpClientConnectionOperator,
    final HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory,
    final long timeToLive, final TimeUnit timeUnit
  ) {
    super();
    // 保存 默认 SocketConfig、ConnectionConfig 和 
    // HttpHost 对应的 SocketConfig、ConnectionConfig
    this.configData = new ConfigData();
    // 
    this.pool = new CPool(
      new InternalConnectionFactory(this.configData, connFactory), 
      2,  // ❤❤❤ defaultMaxPerRoute 即 默认对每个主机的访问并发 2 ❤❤❤
      20, // ❤❤❤ maxTotal           即 默认合计并发 20          ❤❤❤
      timeToLive, timeUnit //❤❤❤ TTL 链接超时时间 ❤❤❤
    );
    // ❤❤❤❤❤❤ 两次获取间隔超过 2000ms 则检测链接可用性 ❤❤❤❤❤❤
    this.pool.setValidateAfterInactivity(2000);
    // 默认 DefaultHttpClientConnectionOperator，
    this.connectionOperator = Args.notNull(httpClientConnectionOperator, "HttpClientConnectionOperator");
    this.isShutDown = new AtomicBoolean(false);
  }
}
```

### 获取链接

#### MainClientExec

```java
public class MainClientExec implements ClientExecChain {
  // ...
  public CloseableHttpResponse execute(...) throws IOException, HttpException {
    // ...
    // ❤❤❤ HttpClientConnectionManager 返回的是一个回调，是对 Future 的包装
    // 通过 connRequest.get > pool.get 获取链接
    final ConnectionRequest connRequest = connManager.requestConnection(route, userToken);
    // .. 判断请求是否被取消

    final RequestConfig config = context.getRequestConfig();
    // ❤❤❤ 从 Pool 中获取链接的超时时间
    final int timeout = config.getConnectionRequestTimeout();
    // 调用 回调的 get 方法，获取链接
    final HttpClientConnection managedConn = connRequest.get(timeout > 0 ? timeout : 0, TimeUnit.MILLISECONDS);

    // ...
  }
}
```

#### requestConnection

```java
public class PoolingHttpClientConnectionManager implements ... {
  
  @Override
  public ConnectionRequest requestConnection(final HttpRoute route, final Object state  ) {
    // .. 
    // 从 Pool(AbstractConnPool) 中获取链接 Future
    final Future<CPoolEntry> future = this.pool.lease(route, state, null);

    return new ConnectionRequest() {
			// ...
      public HttpClientConnection get(final long timeout, final TimeUnit timeUnit) .. {
        // future.get 过程中的异常和日志处理
        final HttpClientConnection conn = leaseConnection(future, timeout, timeUnit);
        if (conn.isOpen()) {
          // ..
          final SocketConfig socketConfig = resolveSocketConfig(host);
          conn.setSocketTimeout(socketConfig.getSoTimeout());
        }
        return conn;
      }

    };

  }
}
```

#### AbstractConnPool.lease

```java
@Override
public Future<E> lease(final T route, final Object state, final FutureCallback<E> callback) {
  // ...
  return new Future<E>() {
    // ...
    public E get(final long timeout, final TimeUnit tunit) throws InterruptedException, ExecutionException, TimeoutException {
      final E entry = entryRef.get();
      if (entry != null) { return entry; }
      
      synchronized (this) {
        try {
          for (;;) {
            // ❤❤❤ 从池中获取一个 Entry 相当于链接 ❤❤❤
            final E leasedEntry = getPoolEntryBlocking(route, state, timeout, tunit, this);
            // ❤❤❤ HttpClientBuilder.ConnectionTimeToLive ❤❤❤
            if (validateAfterInactivity > 0)  {
              // 超过 ConnectionTimeToLive 未被使用时
              if (leasedEntry.getUpdated() + validateAfterInactivity <= System.currentTimeMillis()) {
                // 校验是否可用、释放、重新获取
                if (!validate(leasedEntry)) {
                  leasedEntry.close();
                  release(leasedEntry, false);
                  continue;
                }
              }
            }
            entryRef.set(leasedEntry);
            done.set(true);
            onLease(leasedEntry);
            if (callback != null) {
              callback.completed(leasedEntry);
            }
            // Future 标记为完成，返回 Entry
            return leasedEntry;
          }
        } catch (final IOException ex) {
          // .. 标记为完成，抛出异常
        }
      }
    }

  };
}
```

#### AbstractConnPool.getPoolEntryBlocking

```java
private E getPoolEntryBlocking(
  final T route, final Object state,
  final long timeout, final TimeUnit tunit,
  final Future<E> future) throws IOException, InterruptedException, TimeoutException {

  Date deadline = null;
  if (timeout > 0) {
    // ConnectionRequestTimeout 不为空，计算截止日期（当前时间 + ConnectionRequestTimeout）
    deadline = new Date (System.currentTimeMillis() + tunit.toMillis(timeout));
  }
  
  this.lock.lock();
  try {
    // route 对应的 Pool，维护了以下三类集合
    // Set<E> leased;                 以租出去占用的   
    // LinkedList<E> available;       未租出去可用的
    // LinkedList<Future<E>> pending; 已创建 Future 待 get 的
    final RouteSpecificPool<T, C, E> pool = getPool(route);
    E entry;
    for (;;) {
      Asserts.check(!this.isShutDown, "Connection pool shut down");
      for (;;) {
        // 获取一个空闲的链接
        entry = pool.getFree(state);
        // 池中没有链接，break 当前循环走创建连接流程
        if (entry == null) {
          break;
        }
        if (entry.isExpired(System.currentTimeMillis())) {
          entry.close();
        }
        if (entry.isClosed()) {
          this.available.remove(entry);
          pool.free(entry, false);
        } else {
          break;
        }
      }
      
      if (entry != null) {
        this.available.remove(entry);
        this.leased.add(entry);
        onReuse(entry);
        return entry;
      }

      // 当前路由配置可创建的最大链接数
      final int maxPerRoute = getMax(route);
      // AllocatedCount（available + leased）
      final int excess = Math.max(0, pool.getAllocatedCount() + 1 - maxPerRoute);
      // 已经创建的链接 > 配置的最大链接数, 先缩容（所有available释放， available 大概率是 空）
      if (excess > 0) {
        for (int i = 0; i < excess; i++) {
          final E lastUsed = pool.getLastUsed();
          if (lastUsed == null) {
            break;
          }
          lastUsed.close();
          this.available.remove(lastUsed);
          pool.remove(lastUsed);
        }
      }

      // 如果还允许分配
      if (pool.getAllocatedCount() < maxPerRoute) {
        final int totalUsed = this.leased.size();
        // 配置的最大链接数 > 所有 Route 正在使用的链接，
        final int freeCapacity = Math.max(this.maxTotal - totalUsed, 0);
        if (freeCapacity > 0) {
          // ... 创建 Socket 链接
          final C conn = this.connFactory.create(route);
          entry = pool.add(conn);
          this.leased.add(entry);
          return entry;
        }
      }

      // 如果已经不允许分配，加入 pending 队列，等到被唤醒 或者 超时
      boolean success = false;
      try {
        // .. future.isCancelled() 判断
        // Route 的 pending 队列
        pool.queue(future);
        // 链接池的 pending 队列
        this.pending.add(future);
        if (deadline != null) {
          // 等待到 deadline
          success = this.condition.awaitUntil(deadline);
        } else {
          // 一直等待，知道成功
          this.condition.await();
          success = true;
        }
        // .. future.isCancelled() 判断
      } finally {
        pool.unqueue(future);
        this.pending.remove(future);
      }
      // 被唤醒 或者 等待到 deadline 自己醒来后，判断是否到期
      if (!success && (deadline != null && deadline.getTime() <= System.currentTimeMillis())) {
        break;
      }
    }
    // ConnectionRequestTimeout 时间内仍未获取到链接，抛出超时异常
    throw new TimeoutException("Timeout waiting for connection");
  } finally {
    this.lock.unlock();
  }
}
```

### 释放链接 ？？？？ 

??? 为什么 `EntityUtils.toString(entity)` 执行与不执行，链接池的状态不一样

- pooling.getStats(httpRoute)
- pooling.getTotalStats()

```bash

```



## 如何配置

```java
RequestConfig config = RequestConfig.custom()
  .setConnectTimeout(1_000)
  .setSocketTimeout(2_000)
  // 从连接池中获取连接的超时时间
  .setConnectionRequestTimeout(1_000)
  .build();


CloseableHttpClient httpClient = HttpClients.custom()
  .setDefaultRequestConfig(config)
  // 会覆盖 pooling.setMaxTotal
  .setMaxConnTotal(2000)
  // 会覆盖 pooling.setDefaultMaxPerRoute
  .setMaxConnPerRoute(1000)
  // 链接过期时间
  .setConnectionTimeToLive(10, TimeUnit.SECONDS)
  .build();
```



## HTTP Keep-Alive

- HTTP/1.0通过在 Header 中添加 `Connection:Keep-Alive` 来表示支持长连接
- HTTP/1.1**默认支持长连接**, 除非在 Header 中显式指定 `Connection:Close` , 才被视为短连接模式



## 设置多少？

## 性能有多少提升？

## Read More

- Confluence [Httpclient 核心架构设计](https://yq.aliyun.com/articles/93801)
- [HttpClient 连接池也能这样用](https://mp.weixin.qq.com/s/e29_LFHFAMcvrZpOJ9y2kw)
- [HttpClient 连接池设置引发的一次雪崩](https://mp.weixin.qq.com/s/Bg9Jc7x64j_-sSPhvScQ0g)
- [Http 持久连接与 HttpClient连接池](https://www.cnblogs.com/kingszelda/p/8988505.html)
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=8154751 HttpClient你不一定会用
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=9156972 
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=8158882 HttpClient 4.4 无法携带 Cookies 问题 
- https://www.jianshu.com/p/14c005e9287c

