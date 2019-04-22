package com.github.pvkr.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

public class GetTimeLambda implements RequestHandler<Object, Object> {

    private static final HttpClient httpClient;
    private static final S3Client s3;
    private static final String bucket = "time-log-pvkr-demo";
    private static final String key = "time";
    static {
        httpClient = HttpClientBuilder.create().build();
        s3 = S3Client.builder().region(Region.EU_CENTRAL_1).build();
    }

    public Object handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);

        try {
            HttpResponse response = httpClient.execute(new HttpGet("http://worldclockapi.com/api/json/est/now"));
            if ((response.getStatusLine().getStatusCode() / 100) == 2) {
                HttpEntity entity = response.getEntity();
                InputStream content = response.getEntity().getContent();

                s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
                        RequestBody.fromContentProvider(() -> content, entity.getContentLength(), "application/json")
                );
                context.getLogger().log("Put time to s3");
            }
        } catch (IOException e) {
            context.getLogger().log(e.getMessage());
        }

        return null;
    }
}
