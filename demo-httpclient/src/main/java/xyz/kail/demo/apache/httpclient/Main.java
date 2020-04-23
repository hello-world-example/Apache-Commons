package xyz.kail.demo.apache.httpclient;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(1_000)
                .setSocketTimeout(2_000)
                // 从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1_000)
                .build();

        // 需要自己管理释放连接
        PoolingHttpClientConnectionManager pooling = new PoolingHttpClientConnectionManager();
        pooling.setMaxTotal(111);
        pooling.setDefaultMaxPerRoute(1000);

        //
        pooling.closeIdleConnections(100, TimeUnit.HOURS);


        CloseableHttpClient httpClient = HttpClients.custom()
                // 会覆盖 pooling.setMaxTotal
                .setMaxConnTotal(2000)
                // 会覆盖 pooling.setDefaultMaxPerRoute
                .setMaxConnPerRoute(1000)
                // 设置连接池
                .setConnectionManager(pooling)

                // ❤❤❤ 禁用重试
                .disableAutomaticRetries()
                .setRetryHandler(new DefaultHttpRequestRetryHandler())

                .build();

        HttpGet httpGet = new HttpGet("");
        httpGet.abort();



    }

}
