# 链接复用 和 KeepAlive 策略

- `ConnectionReuseStrategy` 连接复用策略 来决定连接使用之后是否应给被回收
- `ConnectionKeepAliveStrategy` KeepAlive 策略 来决定，连接的存活时间



## HttpClientBuilder.build()

```java
// 连接复用策略
ConnectionReuseStrategy reuseStrategyCopy = this.reuseStrategy;
// 如果没有 明确指定 时的默认行为
if (reuseStrategyCopy == null) {
  // systemProperties 默认是 false，通过 useSystemProperties() 激活
  if (systemProperties) {
    final String s = System.getProperty("http.keepAlive", "true");
    if ("true".equalsIgnoreCase(s)) {
      reuseStrategyCopy = DefaultClientConnectionReuseStrategy.INSTANCE;
    } else {
      reuseStrategyCopy = NoConnectionReuseStrategy.INSTANCE;
    }
  } else {
    reuseStrategyCopy = DefaultClientConnectionReuseStrategy.INSTANCE;
  }
}

// KeepAlive 策略
ConnectionKeepAliveStrategy keepAliveStrategyCopy = this.keepAliveStrategy;
// 如果没有 明确指定 时的默认行为
if (keepAliveStrategyCopy == null) {
  keepAliveStrategyCopy = DefaultConnectionKeepAliveStrategy.INSTANCE;
}
```



## DefaultClientConnectionReuseStrategy

```java
public class DefaultClientConnectionReuseStrategy extends DefaultConnectionReuseStrategy {
  // ...
  @Override
  public boolean keepAlive(final HttpResponse response, final HttpContext context) {
    // 上下文中获取 http.request 请求信息（一般可获取到）
    final HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
    if (request != null) {
      // 获取 Connection 请求头
      // 默认请求头【Connection: Keep-Alive】默认是 Keep-Alive 
      // 如果请求头【Connection: Close】不复用连接
      final Header[] connHeaders = request.getHeaders(HttpHeaders.CONNECTION);
      if (connHeaders.length != 0) {
        final TokenIterator ti = new BasicTokenIterator(new BasicHeaderIterator(connHeaders, null));
        while (ti.hasNext()) {
          final String token = ti.nextToken();
          // 如果【Connection: Close】不复用连接
          if (HTTP.CONN_CLOSE.equalsIgnoreCase(token)) {
            return false;
          }
        }
      }
    }
    
    // DefaultConnectionReuseStrategy 的 复用策略
    return super.keepAlive(response, context);
  }

}
```



### DefaultConnectionReuseStrategy (httpcore)

```java
public class DefaultConnectionReuseStrategy implements ConnectionReuseStrategy {
  // ...
  @Override
  public boolean keepAlive(final HttpResponse response, final HttpContext context) {
    // ①
		// 【不符合规范的情况】 状态码是 204， 说明没有响应内容
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      // 204 但是 Content-length 却 > 0， 说明有内容，与 204 状态不符【不复用连接】
      final Header clh = response.getFirstHeader(HTTP.CONTENT_LEN);
      if (clh != null && Integer.parseInt(clh.getValue()) > 0) {
        return false;
      }
			// 204 但是 Transfer-Encoding 不为空，与 204 状态不符【不复用连接】
      // 最新的 HTTP 规范里，只定义了一种传输编码：Transfer-Encoding: chunked 分块传输
      final Header teh = response.getFirstHeader(HTTP.TRANSFER_ENCODING);
      if (teh != null) {
        return false;
      }
    }

    // ②
    // 与 DefaultClientConnectionReuseStrategy 中的逻辑基本一致，即：
    // 请求头【Connection: Keep-Alive】复用，【Connection: Close】不复用，默认复用
    final HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
    if (request != null) {
      try {
        final TokenIterator ti = new BasicTokenIterator(request.headerIterator(HttpHeaders.CONNECTION));
        while (ti.hasNext()) {
          final String token = ti.nextToken();
          if (HTTP.CONN_CLOSE.equalsIgnoreCase(token)) {
            return false;
          }
        }
      } catch (final ParseException px) {
        // invalid connection header. do not re-use
        return false;
      }
    }

    // ③
    final ProtocolVersion ver = response.getStatusLine().getProtocolVersion();
    // Transfer-Encoding
    final Header teh = response.getFirstHeader(HTTP.TRANSFER_ENCODING);
    // 默认是 null
    if (teh != null) {
      // Transfer-Encoding != chunked【不复用】
      if (!HTTP.CHUNK_CODING.equalsIgnoreCase(teh.getValue())) {
        return false;
      }
    } else {
      // 【不符合规范的情况】有响应内容，但是 Content-Length 却是 负数，不复用
      if (canResponseHaveBody(request, response)) {
        final Header[] clhs = response.getHeaders(HTTP.CONTENT_LEN);
        // Do not reuse if not properly content-length delimited
        if (clhs.length == 1) {
          final Header clh = clhs[0];
          try {
            final int contentLen = Integer.parseInt(clh.getValue());
            if (contentLen < 0) {
              return false;
            }
          } catch (final NumberFormatException ex) {
            return false;
          }
        } else {
          return false;
        }
      }
    }

    //       Connection 用于说明与 直连服务器 的链接情况，常见如：close、Keep-Alive
    // Proxy-Connection 用于说明与 代理服务器 的链接情况，值同上
    // Proxy-Connetion 出现的意义是为了前后兼容，详见 Read More
    HeaderIterator headerIterator = response.headerIterator(HTTP.CONN_DIRECTIVE);
    if (!headerIterator.hasNext()) {
      headerIterator = response.headerIterator("Proxy-Connection");
    }

    // ...

    // 默认 HTTP/1.1 复用, HTTP/1.0 不复用
    return !ver.lessEquals(HttpVersion.HTTP_1_0);
  }

  // ...
}
```



## DefaultConnectionKeepAliveStrategy

```java
public class DefaultConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
  // ... 获取连接保持的时间
  @Override
  public long getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
    // 获取 Keep-Alive 响应头
    // Spring Boot 1【无，无限时长】
    // Spring Boot 2【Keep-Alive: timeout=60】
    final HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));

    while (it.hasNext()) {
      final HeaderElement he = it.nextElement();
      // timeout
      final String param = he.getName();
      // 60s
      final String value = he.getValue();
      if (value != null && param.equalsIgnoreCase("timeout")) {
        return Long.parseLong(value) * 1000;
      }
    }
    // 默认无限
    return -1;
  }
}
```



## 策略的配合 

### MainClientExec

```java
// ...

// 使用连接获取响应之后
response = requestExecutor.execute(request, managedConn, context);

// 连接复用策略，查看是否可复用
if (reuseStrategy.keepAlive(response, context)) {
  // keepAlive 策略，获取保持时间
  final long duration = keepAliveStrategy.getKeepAliveDuration(response, context);
  // ... 
  // 链接续期
  connHolder.setValidFor(duration, TimeUnit.MILLISECONDS);
  // 标记为复用
  connHolder.markReusable();
} else {
  // 标记为不可复用
  connHolder.markNonReusable();
}

// ... 使用之后都会释放连接，根据是否复用来 真实关闭链接 或 放入链接池中
connHolder.releaseConnection();
```



### ConnectionHolder.releaseConnection

```java
@Override
public void releaseConnection() {
  // 连接被标记为复用
  releaseConnection(this.reusable);
}


private void releaseConnection(final boolean reusable) {
  if (this.released.compareAndSet(false, true)) {
    synchronized (this.managedConn) {
      if (reusable) {
        // 如果链接可复用，放入连接池（manager 默认是 PoolingHttpClientConnectionManager ）
        this.manager.releaseConnection(this.managedConn, this.state, this.validDuration, this.tunit);
      } else {
        try {
          // 如果不可复用，关闭 TCP 链接
          this.managedConn.close();
          // ...
        } finally {
          // 放入连接池，但是连接有效时间是 0，会从 池中清除
          this.manager.releaseConnection( this.managedConn, null, 0, TimeUnit.MILLISECONDS);
        }
      }
    }
  }
}
```



## 小结

- HTTP/1.0 默认不支持持久连接，请求头 必须添加 `Connecttion: Keep-Alive` 来协商持久连接，**服务端返回同样的内容**，这个连接就会被保持供后续使用
- HTTP/1.1，除了显式自定 `Connection: close`，默认都是持久连接，`Connection: Keep-Alive` 已经失去意义了
- HttpClient 的链接保持除了遵循 HTTP 规范外，还对异常情况进行校验，不符合规范的链接，不复用被回收
- `Connection:Keep-Alive` 表示链接保持，配合 `Keep-Alive: timeout=60` 表明链接保持的空闲时间间隔，空闲时间超过 60秒之后，就会失效，60s 内 再次使用此连接，则连接仍然有效，使用完之后，重新计数
- 默认是长连接，如果没有 `Keep-Alive: timeout=xxx`，默认无限期使用
- HTTP 头信息，不区分大小写



## Read More

- [HTTP 协议中的 Transfer-Encoding](https://imququ.com/post/transfer-encoding-header-in-http.html)
- [Http 请求头中的 Proxy-Connection](https://imququ.com/post/the-proxy-connection-header-in-http-request.html)
- [HTTP长连接、短连接使用及测试](https://www.cnblogs.com/shoren/p/http-connection.html)