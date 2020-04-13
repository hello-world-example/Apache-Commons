package xyz.kail.demo.apache.httpclient;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SimpleHttpClient {

    public static void main(String[] args) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(1_000)
                .setSocketTimeout(2_000)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpRequestBase request = new HttpGet("");
        request.setConfig(requestConfig);

        httpClient.execute(request);


    }

}
