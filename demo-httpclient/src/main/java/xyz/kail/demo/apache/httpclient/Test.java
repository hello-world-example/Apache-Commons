package xyz.kail.demo.apache.httpclient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

public class Test {

    static boolean isRepeatable(final HttpRequest request) {
        if (request instanceof HttpEntityEnclosingRequest) {
            final HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null) {
                return entity.isRepeatable();
            }
        }
        return false;
    }


    public static void main(String[] args) {
        System.out.println(isRepeatable(new HttpGet("")));
        System.out.println(isRepeatable(new HttpDelete("")));
        System.out.println(isRepeatable(new HttpPost("")));
        System.out.println(isRepeatable(new HttpPut("")));

    }


}
