# HttpClient 链接池

> - HttpClient 4.5 
>   - [官方文档](http://hc.apache.org/httpcomponents-client-4.5.x/tutorial/html/index.html)
>   - [官方示例](http://hc.apache.org/httpcomponents-client-4.5.x/examples.html)



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



## Read More

- [HttpClient 连接池也能这样用](https://mp.weixin.qq.com/s/e29_LFHFAMcvrZpOJ9y2kw)
- [HttpClient 连接池设置引发的一次雪崩](https://mp.weixin.qq.com/s/Bg9Jc7x64j_-sSPhvScQ0g)
- [Http 持久连接与 HttpClient连接池](https://www.cnblogs.com/kingszelda/p/8988505.html)

