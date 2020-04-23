# HttpClient 重试机制



## 目的

重试机制有以下好处

- 自动容错，在网络抖动的时候保证结果整体可用

也会代理以下问题

- 如果接口不幂等，会导致重复提交
- 同一个操作被多次执行

这里主要目的就是为了确定，HttpClient 内置的重试机制是否会导致副作用，针对 `4.5.x`

## 默认行为

### 默认构建方式

```java
// 等同于 CloseableHttpClient httpClient = HttpClients.createDefault();
// 等同于 CloseableHttpClient httpClient = HttpClientBuilder.create().build();
CloseableHttpClient httpClient = HttpClients.custom().build();
```



### HttpClientBuilder.build() 方法

```java
public CloseableHttpClient build() {
  // HttpClient 的执行过程采用，责任链模式，一层包一层，通过 构造函数传递下一个链节点
  // 重试（RetryExec） 是整个请求责任链中的一个节点，
  // 根据指定的条件（HttpRequestRetryHandler 的实现），判断是否需要重新执行后续责任链
  ClientExecChain execChain = ... ; 
  
  // MainClientExec             主链
  // ProtocolExec               协议链
  // ...
  // RetryExec                  ❤❤❤ 重试链（ IOException 异常时的重试策略） ❤❤❤ 
  
  // ❤❤❤  默认 false，不禁用重试
  // ❤❤❤  可通过 HttpClientBuilder.disableAutomaticRetries() 禁用
  if (!automaticRetriesDisabled) {
    HttpRequestRetryHandler retryHandlerCopy = this.retryHandler;
    if (retryHandlerCopy == null) {
      // ❤❤❤  如果没有自定义 HttpRequestRetryHandler， 使用默认配置
      retryHandlerCopy = DefaultHttpRequestRetryHandler.INSTANCE;
    }
    // ❤❤❤ RetryExec 包裹 重试策略，加入到 责任链路中
    execChain = new RetryExec(execChain, retryHandlerCopy);
  }
  
  //  ❤❤❤ 服务不可用时的重试策略 ❤❤❤ 
  ServiceUnavailableRetryStrategy serviceUnavailStrategyCopy = this.serviceUnavailStrategy;
  // ❤❤❤ 默认是空 不会启用 ❤❤❤ 
  if (serviceUnavailStrategyCopy != null) {
    execChain = new ServiceUnavailableRetryExec(execChain, serviceUnavailStrategyCopy);
  }
  
  // RedirectExec
  // BackoffStrategyExec

  // ...

  // CloseableHttpClient 的默认实现 InternalHttpClient
  return new InternalHttpClient(
    execChain,
    connManagerCopy,
    routePlannerCopy,
    cookieSpecRegistryCopy,
    authSchemeRegistryCopy,
    defaultCookieStore,
    defaultCredentialsProvider,
    defaultRequestConfig != null ? defaultRequestConfig : RequestConfig.DEFAULT,
    closeablesCopy
  );
}
```

### RetryExec

```java
public class RetryExec implements ClientExecChain {

  // ...

  @Override
  public CloseableHttpResponse execute(...) throws IOException, HttpException {
    // 无限循环，直到抛出异常
    for (int execCount = 1;; execCount++) {
      try {
        // 先执行后续责任链逻辑
        return this.requestExecutor.execute(route, request, context, execAware);
        // ❤❤❤ 如果出现 IOException，执行后续重试逻辑的判断 ❤❤❤ 
      } catch (final IOException ex) {
        // 如果当前执行流程已经被中断，不进行重试，直接抛出一下，如：异步批量执行的情况的
        // HttpRequestFutureTask<Object> futureTask = FutureRequestExecutionService.execute(reuqest);
        // futureTask.cancel(true)
        if (execAware != null && execAware.isAborted()) { throw ex; }


        // ❤❤❤ 如果重试策略判断为可重试，不抛出异常， for 继续，执行下一次 execute ❤❤❤
        if (retryHandler.retryRequest(ex, execCount, context)) {
          // ❤❤❤ 判断当前请求是否支持重试，主要根据 ❤❤❤
          // 1. 当前请求是否包含请求体(HttpEntityEnclosingRequest 的子类)
          //    常见的包含请求体的：POST、PUT、PATCH
          // 2. 如果有请求体（HttpEntity），判断 HttpEntity 的实现是否支持重试，常见的如
          // StringEntity          可重试 ❤ post json 
          // UrlEncodedFormEntity  可重试 ❤ post kv 表单
          // MultipartFormEntity   可重试 ❤ post 文件、字节流
          // InputStreamEntity    不可重试
          // BasicHttpEntity      不可重试
          if (!RequestEntityProxy.isRepeatable(request)) {
            throw new NonRepeatableRequestException("Cannot retry request with a non-repeatable request entity", ex);
          }
          // ... 一些日志打印
        } else {
          // ... 省略对 NoHttpResponseException 异常的包装
          // ❤❤❤ 如果不能重试，则抛出异常，结束循环，退出当前执行链
          throw ex;
        }

      } // -- catch end
    } //  -- for end
  } // -- execute end

}
```



### DefaultHttpRequestRetryHandler

```java
public class DefaultHttpRequestRetryHandler implements HttpRequestRetryHandler {

  public static final DefaultHttpRequestRetryHandler INSTANCE = new DefaultHttpRequestRetryHandler();

  // 重试次数，默认 3 次
  private final int retryCount;
  // 已经发送成功的数据是否重试，默认 false（避免重复发送）
  private final boolean requestSentRetryEnabled;
  // ❤❤❤ 不进行重试的 IOException，默认
  // - ConnectException                     连接建立失败
  // - InterruptedIOException
  // - - - java.net.SocketTimeoutException
  // - - - - - - connect timed out          链接超时，被封装成 ConnectTimeoutException
  // - - - - - - Read timed out             读超时（连接建立后，响应超过指定的时间）
  // - UnknownHostException                 域名解析失败
  // - SSLException                         HTTPs 失败
  private final Set<Class<? extends IOException>> nonRetriableClasses;

  // ...

  public DefaultHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
    // ❤❤❤ 默认，不进行重试的 IOException
    this(retryCount, requestSentRetryEnabled, Arrays.asList(
      InterruptedIOException.class, 
      UnknownHostException.class, 
      ConnectException.class, 
      SSLException.class
    ));
  }

  public DefaultHttpRequestRetryHandler() {
    // 默认重试 3次， INSTANCE = new DefaultHttpRequestRetryHandler();
    this(3, false);
  }

  @Override
  public boolean retryRequest(final IOException exception, final int executionCount, final HttpContext context) {
		// [不重试] 起始是 1，超过重试次数（默认是3）结束循环
    if (executionCount > this.retryCount) { return false; }
    
    //  [不重试] 发生的异常是 不进行重试的异常
    if (this.nonRetriableClasses.contains(exception.getClass())) {
      return false;
    } else {
      for (Class<? extends IOException> rejectException : this.nonRetriableClasses) {
        //  [不重试] 发生的异常是 不进行重试的异常 的子类实现
        if (rejectException.isInstance(exception)) {
          return false;
        }
      }
    }
    
    // ...

    // [不重试]请求流程被取消
    if(requestIsAborted(request)){ return false; }

    // 【【【重试】】】请求是幂等的【【【重试】】】
    // 幂等的 判断条件是：不是 HttpEntityEnclosingRequest 的实例，常见实现 POST、PUT、PATCH
    // 常见的幂等请求类型有：GET、DELETE、HEAD、OPTIONS、TRACE （没有请求体）
    if (handleAsIdempotent(request)) { return true; }

    // 【【【重试】】】数没发送完 || requestSentRetryEnabled 默认 false
    // 是否发送完，通过 HttpCoreContext.HTTP_REQ_SENT(http.request_sent) 变量标示，大致流程是
    // 1. 提交数据之前 标示 HttpCoreContext.HTTP_REQ_SENT 为 false
    // 2. conn.flush（ outStream.flush() 提交数据 ），如果 flush 出现异常，则当前请求未完成
    // 3. 如果 flush 操作没抛异常，标示 HttpCoreContext.HTTP_REQ_SENT 为 false
    if (!clientContext.isRequestSent() || this.requestSentRetryEnabled){ return true; }
    
    // [不重试]
    return false;
  }
  // ...
}
```



## 结论

- HttpClient 的重试策略算是比较严格的（这下就放心了），**要想自动重试，需要满足一下条件**，发生 `IOException` 的情况下
  - **❤ 请求是幂等的：`GET`、`DELETE` ...**
  - **❤ 请求是非幂等的：`POST`、`PUT` ... 但是数据未真正提交成功（服务端未接收到成功）** 
- 以下请求**不会重试**，发生 `IOException` 的情况下
  - **❤ `POST`、`PUT` .. 请求，且数据正常发送**
  - 发生已下 `IOException` 
    - `ConnectException` 连接建立失败
    - `UnknownHostException` 域名解析失败
    - `SSLException`  HTTPs 失败
    - `InterruptedIOException`
      - `org.apache.http.conn.ConnectTimeoutException` 
        - `org.apache.http.conn.ConnectionPoolTimeoutException`
      - `java.net.SocketTimeoutException` **connect timed out** 、**Read timed out**     
      - `org.apache.http.impl.execchain.RequestAbortedException`
- **如果接口设计符合标准，HttpClient 的默认重试策略，不会造成 重复提交、执行多次的问题**
  - ~~如果在 GET 请求里面有新增数据的操作，则重试策略可能会导致新增多条~~



## 思考

为什么 HttpClient 会这样设计重试策略？ 可能基于以下几点，如果出现异常：

- 重试是否会造成重复数据，如果不会，则尝试重试
- 下次重试是否有可能成功？ 如果可能成功，则重试重试

以上几点在实际业务中可能都不一定完全合适

- HttpClient 是按照接口规范进行设计，实际业务场景中，不一定所有人都能遵循接口规范
- `ConnectTimeoutException` 可能由于网络抖动，是暂时行的，下次重试是有可能成功的



## 如何设置

```java
RequestConfig requestConfig = RequestConfig.custom()
  .setConnectTimeout(500).setSocketTimeout(2_000)
  .build();

CloseableHttpClient httpClient = HttpClients.custom()
  // 超时时间设置
  .setDefaultRequestConfig(requestConfig)
  // ❤❤❤ 禁用重试
  .disableAutomaticRetries()
  // ❤❤❤ 自定义重试策略
  // .setRetryHandler(...)
  .build();
```



## 其它细节

### StandardHttpRequestRetryHandler

- HttpRequestRetryHandler(org.apache.http.client)
  - **DefaultHttpRequestRetryHandler** (org.apache.http.impl.client)
    - StandardHttpRequestRetryHandler (org.apache.http.impl.client)



`HttpRequestRetryHandler` 除了 默认的 `DefaultHttpRequestRetryHandler` 实现外，还有一个标准实现，针对 [RFC-2616](https://tools.ietf.org/html/rfc2616) (**Hypertext Transfer Protocol -- HTTP/1.1**) 标准来判断接口是是幂等，而不是  `DefaultHttpRequestRetryHandler` 根据是否有请求体来判断。

即 **除了 `POST` / `CONNECT`(ws) 是非幂等的**，其他都是幂等 请求方式

```java
public class StandardHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {

  private final Map<String, Boolean> idempotentMethods;

  public StandardHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
    super(retryCount, requestSentRetryEnabled);
    this.idempotentMethods = new ConcurrentHashMap<String, Boolean>();
    this.idempotentMethods.put("GET", Boolean.TRUE);
    this.idempotentMethods.put("HEAD", Boolean.TRUE);
    this.idempotentMethods.put("PUT", Boolean.TRUE);
    this.idempotentMethods.put("DELETE", Boolean.TRUE);
    this.idempotentMethods.put("OPTIONS", Boolean.TRUE);
    this.idempotentMethods.put("TRACE", Boolean.TRUE);
  }

  public StandardHttpRequestRetryHandler() {
    this(3, false);
  }

  @Override
  protected boolean handleAsIdempotent(final HttpRequest request) {
    final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
    final Boolean b = this.idempotentMethods.get(method);
    return b != null && b.booleanValue();
  }

}
```



### ServiceUnavailableRetryStrategy

服务不可用重试策略，被 `ServiceUnavailableRetryExec` 引用， 在 `RetryExec` 之前，执行的时候先执行。

与 `DefaultHttpRequestRetryHandler` 的区别在于，**服务不可用的策略重试期间会休眠一段时间**

#### ServiceUnavailableRetryExec

```java
@Override
public CloseableHttpResponse execute(...) throws IOException, HttpException {
  // ..
  for (int c = 1;; c++) {
    final CloseableHttpResponse response = this.requestExecutor.execute(...);
    
    // 重试策略返回是否重试
    if (this.retryStrategy.retryRequest(response, c, context) && 
        // 与 RetryExec 中的判断 request 是否可重试逻辑一致
        // 即常见的 POST、GET 都是支持重试的 ，InputStreamEntity 等流式 Entity 不支持重试
        RequestEntityProxy.isRepeatable(request)) {
      
      response.close();

      // 获取 设置的 重置间隔
      final long nextInterval = this.retryStrategy.getRetryInterval();
      //... 休眠之后重试
      if (nextInterval > 0) { Thread.sleep(nextInterval); }
    } else {
      return response;
    }
    // ...
  }
}
```

#### DefaultServiceUnavailableRetryStrategy

```java
public class DefaultServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {

  // 重试次数（默认 1次）
  private final int maxRetries;
  /// 休眠时间（默认 1秒）
  private final long retryInterval;
  // ...

  public DefaultServiceUnavailableRetryStrategy() {
    this(1, 1000);
  }

  @Override
  public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {
    // 没到重试次数 && 状态码是 503 进行重试
    return executionCount <= maxRetries &&
      response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE;
  }
  // ..

}
```



## Read More

- [关于HttpClient重试策略的研究](https://www.cnblogs.com/kingszelda/p/8886403.html)