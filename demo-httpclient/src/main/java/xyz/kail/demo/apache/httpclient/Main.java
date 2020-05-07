package xyz.kail.demo.apache.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    static CloseableHttpClient httpClient;

    static PoolingHttpClientConnectionManager pooling;

    public static void main(String[] args) throws IOException, InterruptedException {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(1_000)
                .setSocketTimeout(2_000)
                // 从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(100_000)
                .build();

        pooling = new PoolingHttpClientConnectionManager(10, TimeUnit.SECONDS);
        pooling.setMaxTotal(1000);
        pooling.setDefaultMaxPerRoute(1);

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .setConnectionManager(pooling)
                .build();

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                try {
                    request();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        TimeUnit.SECONDS.sleep(2);
        executorService.shutdown();

    }

    static void request() throws IOException {
//        HttpRoute httpRoute = new HttpRoute(new HttpHost("pubapi.ttpai.cn", 80));
//        HttpGet httpGet = new HttpGet("http://pubapi.ttpai.cn/v1.0/auction/state?appid=10000&auctionId=45645457");
        HttpRoute httpRoute = new HttpRoute(new HttpHost("www.baidu.com", 80));
        HttpGet httpGet = new HttpGet("http://www.baidu.com");


        String threadName = Thread.currentThread().getName();

        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String print = threadName + "--" + EntityUtils.toString(entity);
//            System.out.println(print);
        response.close();

        System.out.println(threadName + "--" + pooling.getStats(httpRoute));
        System.out.println(threadName + "--" + pooling.getTotalStats());
    }

}
