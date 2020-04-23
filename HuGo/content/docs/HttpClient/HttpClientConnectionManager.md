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
    // ❤❤❤❤❤❤ 两次获取间隔超过 2000ms 则检车链接可用性 ❤❤❤❤❤❤
    this.pool.setValidateAfterInactivity(2000);
    // 默认 DefaultHttpClientConnectionOperator，
    this.connectionOperator = Args.notNull(httpClientConnectionOperator, "HttpClientConnectionOperator");
    this.isShutDown = new AtomicBoolean(false);
  }
}
```

### 获取链接

```java
@Override
public ConnectionRequest requestConnection(
  final HttpRoute route,
  final Object state
) {
  // .. 
  // 从 Pool 中获取链接 Future
  // 
  final Future<CPoolEntry> future = this.pool.lease(route, state, null);

  return new ConnectionRequest() {

    @Override
    public boolean cancel() {
      return future.cancel(true);
    }

    @Override
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
```



### 关闭链接

```bash

```







## 如何使用

### 基础超时配置

```java
RequestConfig requestConfig = RequestConfig.custom()
  // 链接超时
  .setConnectTimeout(1_000)
  // 读写超时
  .setSocketTimeout(2_000)
  .build();

CloseableHttpClient httpClient = HttpClients.custom()
  // ❤❤❤ 默认配置
  .setDefaultRequestConfig(requestConfig)
  .build();

HttpRequestBase request = new HttpGet("");
// ❤❤❤ 可覆盖 默认配置
request.setConfig(requestConfig);

httpClient.execute(request);
```

### 链接池配置

```java

```







## HTTP Keep-Alive

- HTTP/1.0通过在 Header 中添加 `Connection:Keep-Alive` 来表示支持长连接
- HTTP/1.1**默认支持长连接**, 除非在 Header 中显式指定 `Connection:Close` , 才被视为短连接模式



## 参数配置

### RequestConfig.custom()

| 参数                       | 默认 |  建议  | 简介                             |
| -------------------------- | :--: | :----: | -------------------------------- |
| `connectTimeout`           | `-1` | `500`  | 客户端和服务器建立连接的超时时间 |
| `socketTimeout`            | `-1` | `2000` | 客户端从服务器读取数据的超时时间 |
| `connectionRequestTimeout` | `-1` |        | 从连接池中获取连接的超时时间     |

### PoolingHttpClientConnectionManager
| 参数                     | 默认 | 建议 | 简介                                                         |
| ------------------------ | :--: | :--: | ------------------------------------------------------------ |
| `maxTotal`               | `0`  |      | 整个链接池的最大链接数                                       |
| `defaultMaxPerRoute`     | `0`  |      | 某个主机的最大连接数，所有主机加起来不能超过，MAX_CONNECTION_TOTAL |
| RETRY_COUNT              |  3   |      | 重试次数                                                     |
| CONNECTION_IDLE_TIME_OUT | 5000 |      | 闲置的连接的时间阀值                                         |
| DEFAULT_KEEP_ALIVE_TIME_MILLIS | 20 * 1000 |      | 默认保活时间              


### HttpClients.custom()

| 参数                           |   默认    | 建议 | 简介                                                         |
| ------------------------------ | :-------: | :--: | ------------------------------------------------------------ |
| `maxConnTotal`                 |    `0`    |      | 整个链接池的最大链接数                                       |
| `maxConnPerRoute`              |    `0`    |      | 某个主机的最大连接数，所有主机加起来不能超过，MAX_CONNECTION_TOTAL |
| RETRY_COUNT                    |     3     |      | 重试次数                                                     |
| CONNECTION_IDLE_TIME_OUT       |   5000    |      | 闲置的连接的时间阀值                                         |
| DEFAULT_KEEP_ALIVE_TIME_MILLIS | 20 * 1000 |      | 默认保活时间                                                 |

## 设置多少？

## 性能有多少提升？

## Read More

- [HttpClient 连接池也能这样用](https://mp.weixin.qq.com/s/e29_LFHFAMcvrZpOJ9y2kw)
- [HttpClient 连接池设置引发的一次雪崩](https://mp.weixin.qq.com/s/Bg9Jc7x64j_-sSPhvScQ0g)
- [Http 持久连接与 HttpClient连接池](https://www.cnblogs.com/kingszelda/p/8988505.html)
- TODO http://confluence.ttpai.cn/pages/viewpage.action?pageId=8130736 记一次 HttpClient 死锁问题
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=9155747 高并发场景下的httpClient优化使用
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=8154751 HttpClient你不一定会用
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=9156972 
- http://confluence.ttpai.cn/pages/viewpage.action?pageId=8158882 HttpClient 4.4 无法携带 Cookies 问题 
- https://www.jianshu.com/p/14c005e9287c

