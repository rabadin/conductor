package com.netflix.conductor.contribs.publisher;

import com.google.inject.Inject;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RestClientManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientManager.class);
    private final PublisherConfiguration config;
    private PoolingHttpClientConnectionManager cm;
    private RequestConfig rc;
    private HttpRequestRetryHandler retryHandle;
    private CloseableHttpClient client;

    enum NotificationType {
        TASK,
        WORKFLOW
    };

    private String notifType;
    private String notifId;

    @Inject
    public RestClientManager(PublisherConfiguration config){
        this.config = config;
        this.cm = prepareConnManager();
        this.rc = prepareRequestConfig();
        this.retryHandle = prepareRetryHandle();
        this.client = prepareClient();
    }

    private PoolingHttpClientConnectionManager prepareConnManager (){
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(this.config.getConnectionPoolMaxRequest());
        connManager.setDefaultMaxPerRoute(this.config.getConnectionPoolMaxRequestPerRoute());
        return connManager;
    }

    private RequestConfig prepareRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(this.config.getConnectionMgrTimeoutInSec() * 1000)
                .setSocketTimeout(this.config.getSocketTimeoutInSec() * 1000)
                .setConnectTimeout(this.config.getRequestTimeoutInSec() * 1000).build();
    }

    private HttpRequestRetryHandler prepareRetryHandle() {
        return (exception, executionCount, context) -> {
            LOGGER.info("Retrying {} Id: {}", this.notifType, this.notifId);
            return executionCount <= this.config.getRequestRetryCount();
        };
    }

    // by default retries 3 times
    private CloseableHttpClient prepareClient() {
        return HttpClients.custom()
                .setConnectionManager(this.cm)
                .setDefaultRequestConfig(this.rc)
                .setRetryHandler(this.retryHandle)
                .build();
    }

    void postNotification(RestClientManager.NotificationType notifType, String data, String domainGroupMoId, String accountMoId, String id) throws IOException {
        this.notifType = notifType.toString();
        notifId = id;
        String url = prepareUrl(notifType);

        Map<String, String> headers = new HashMap<>();
        headers.put(this.config.getHeaderPrefer(), this.config.getHeaderPreferValue());
        headers.put(this.config.getHeaderDomainGroup(), domainGroupMoId);
        headers.put(this.config.getHeaderAccountCookie(), accountMoId);

        HttpPost request = createPostRequest(url, data, headers);

        executePost(request);
    }

    private String prepareUrl(RestClientManager.NotificationType notifType) {
        String urlEndPoint = "";

        if (notifType == RestClientManager.NotificationType.TASK) {
            urlEndPoint = this.config.getEndPointTask();

        }
        else if (notifType == RestClientManager.NotificationType.WORKFLOW){
            urlEndPoint = this.config.getEndPointWorkflow();
        }
        return this.config.getNotificationUrl() + "/" + urlEndPoint;
    }

    private HttpPost createPostRequest(String url, String data, Map<String, String> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(data);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        headers.forEach(httpPost::setHeader);
        return httpPost;
    }

    private void executePost(HttpPost httpPost) throws IOException {
        try (CloseableHttpResponse response = client.execute(httpPost)) {
            int sc = response.getStatusLine().getStatusCode();
            if (!(sc == HttpStatus.SC_ACCEPTED || sc == HttpStatus.SC_OK)) {
                throw new ClientProtocolException("Unexpected response status: " + sc);
            }
        } finally {
            httpPost.releaseConnection(); // release the connection gracefully so the connection can be reused by connection manager
        }
    }
}