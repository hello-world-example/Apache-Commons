package xyz.kail.demo.apache.httpclient;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class SimpleHttpClient {

    public static void main(String[] args) throws IOException {

    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(500)
            .setSocketTimeout(2_000)
            .build();

    CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(500).setSocketTimeout(2_000).build())
            .build();

        HttpRequestBase request = new HttpGet("");
        request.setConfig(requestConfig);

        httpClient.execute(request);

        CloseableHttpClient client = HttpClients.createDefault();


    }

}
