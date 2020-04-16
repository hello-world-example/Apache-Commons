# HttpClient 的创建

> 基于： `4.5.x`

## 继承关系

- **CloseableHttpClient** ：`4.3.x` 依赖的核心接口
  - MinimalHttpClient ： **包访问级别**， `HttpClient` **极简实现**，核心设计的初级模型
  - **InternalHttpClient** ： **包访问级别**， 通过  `HttpClientBuilder` 构建的 `HttpClient` **核心实现**
  - ~~AbstractHttpClient~~ ： 标记过时，
    - ~~DefaultHttpClient~~ ： 标记过时，
      - ~~ContentEncodingHttpClient~~ ： 标记过时，
      - ~~SystemDefaultHttpClient~~ ： 标记过时，
- ~~AutoRetryHttpClient~~ ： 已过时， `HttpClient` 和 `ServiceUnavailableRetryStrategy` 的包装器，现在建议通过 `HttpClientBuilder` 构建 `CloseableHttpClient`
- ~~DecompressingHttpClient~~ ： 已过时，对请求和响应进行编码解码，~~`DefaultHttpClient`~~ 、`HttpXxxInterceptor` 的包装器



>- 可以 `new` 出来的 `HttpClient`  全都被标记为过时了
>- 没有过时的 **`InternalHttpClient`** 、`MinimalHttpClient` 两个实现都是 `package` 级别的访问级别，外部无法直接创建
>- 建议通过 `HttpClientBuilder` 进行创建，`CloseableHttpClient client = HttpClientBuilder.create().xxx(配置).build();` 创建出来的是 `InternalHttpClient` 的实例
>- `HttpClients` 是  `HttpClientBuilder`  的封装，接口更简单 `CloseableHttpClient client = HttpClients.createDefault();` 创建出来的 `client` 都是默认配置 



## HttpClients 工具类

```java
/**
 * @since 4.3
 */
public class HttpClients {

  /**
   * ❤❤❤ 自定义 HttpClientBuilder 参数 ❤❤❤ 
   */
  public static HttpClientBuilder custom() {
    return HttpClientBuilder.create();
  }

  /**
   * 默认的 InternalHttpClient
   */
  public static CloseableHttpClient createDefault() {
    return HttpClientBuilder.create().build();
  }

  /**
   * ❤❤❤ useSystemProperties 的作用是：支持通过 System.getProperty("") 读取系统参数（ -D ），如：
   * 
   * https： https.protocols / https.cipherSuites
   * 连接池相关(连接复用)： http.keepAlive(true)(DefaultMaxPerRoute) / http.maxConnections(5)(MaxTotal)
   * HTTP 先关： http.agent（User-Agent）默认：Apache-HttpClient/4.5.12 (Java/1.8.0_131)
   * 代理： SystemDefaultRoutePlanner
   * 安全： SystemDefaultCredentialsProvider
   */
  public static CloseableHttpClient createSystem() {
    return HttpClientBuilder.create().useSystemProperties().build();
  }

  /**
   * MinimalHttpClient 使用 默认 Http 连接池
   */
  public static CloseableHttpClient createMinimal() {
    return new MinimalHttpClient(new PoolingHttpClientConnectionManager());
  }

  /**
   * MinimalHttpClient，自定义 Http 连接池
   */
  public static CloseableHttpClient createMinimal(final HttpClientConnectionManager connManager) {
    return new MinimalHttpClient(connManager);
  }

}
```

## 构造 CloseableHttpClient

```java
RequestConfig requestConfig = RequestConfig.custom()
  .setConnectTimeout(1_000)
  .setSocketTimeout(2_000)
  .build();

CloseableHttpClient httpClient = HttpClients.custom()
  .setDefaultRequestConfig(requestConfig)
  .build();
```



