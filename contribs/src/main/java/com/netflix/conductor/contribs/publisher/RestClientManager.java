package com.netflix.conductor.contribs.publisher;

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
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*

https://www.baeldung.com/httpclient-timeout
the Connection Timeout (http.connection.timeout) – the time to establish the connection with the remote host
the Socket Timeout (http.socket.timeout) – the time waiting for data – after establishing the connection; maximum time of inactivity between two data packets
the Connection Manager Timeout (http.connection-manager.timeout) – the time to wait for a connection from the connection manager/pool

 https://learnbyinsight.com/2020/06/29/httpclient-single-instance-or-multiple/

 https://www.programcreek.com/java-api-examples/?api=org.apache.http.impl.conn.PoolingHttpClientConnectionManager
 */
@Singleton
public class RestClientManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientManager.class);

    enum NotificationType {
        TASK,
        WORKFLOW
    };
    private static final String TASK_NOTIFICATION_ENDPOINT = "workflow/TaskNotifications";
    private static final String WORKFLOW_NOTIFICATION_ENDPOINT = "workflow/WorkflowNotifications";
    private static final String URL = "http://bullwinkle.default.svc.cluster.local:7979/v1";

    private static final int DEFAULT_CONNECT_TIMEOUT = 10;
    private static final int DEFAULT_REQUEST_TIMEOUT = 10;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10;
    private static final int DEFAULT_RETRY_COUNT = 5;
    private static final int DEFAULT_RETRY_INTERVAL = 10;

    private static final String HEADER_DOMAIN_GROUP = "X-Starship-DomainGroup";
    private static final String HEADER_ACCOUNT_COOKIE = "x-barracuda-account";
    private static final String HEADER_PREFER = "Prefer";
    private static final String HEADER_PREFER_VALUE = "respond-async";

    private PoolingHttpClientConnectionManager cm = prepareConnManager();
    private RequestConfig rc = prepareRequestConfig();
    private HttpRequestRetryHandler retryHandle = prepareRetryHandle();
    private CloseableHttpClient client = prepareClient();

    private String NotifType;
    private String NotifId;

    private PoolingHttpClientConnectionManager prepareConnManager (){
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(5);
        connManager.setDefaultMaxPerRoute(3);
        return connManager;
    }

    private RequestConfig prepareRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT * 1000)
                .setConnectionRequestTimeout(DEFAULT_REQUEST_TIMEOUT * 1000)
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT * 1000).build();
    }

    private HttpRequestRetryHandler prepareRetryHandle() {
        return (exception, executionCount, context) -> {
            LOGGER.info("Retrying {} Id: {}", this.NotifType, this.NotifId);
            return executionCount <= DEFAULT_RETRY_COUNT;
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

    public RestClientManager(){
    }

    public void postNotification(RestClientManager.NotificationType notifType, String data, String domainGroupMoId, String accountMoId, String id) throws IOException {
        NotifType = notifType.toString();
        NotifId = id;
        String url = prepareUrl(notifType);

        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_PREFER, HEADER_PREFER_VALUE);
        headers.put(HEADER_DOMAIN_GROUP, domainGroupMoId);
        headers.put(HEADER_ACCOUNT_COOKIE, accountMoId);

        HttpPost request = createPostRequest(url, data, headers);

        executePost(request);
    }

    private String prepareUrl(RestClientManager.NotificationType notifType) {
        String urlEndPoint = "";

        if (notifType == RestClientManager.NotificationType.TASK) {
            urlEndPoint = TASK_NOTIFICATION_ENDPOINT;

        }
        else if (notifType == RestClientManager.NotificationType.WORKFLOW){
            urlEndPoint = WORKFLOW_NOTIFICATION_ENDPOINT;
        }
        return URL + "/" + urlEndPoint;
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
        CloseableHttpResponse response = client.execute(httpPost);
        try {
            int sc = response.getStatusLine().getStatusCode();
            if (!(sc == HttpStatus.SC_ACCEPTED || sc == HttpStatus.SC_OK)){
                throw new ClientProtocolException("Unexpected response status: " + sc);
            }
        }
        finally {
            response.close();
            httpPost.releaseConnection(); // release the connection gracefully so the connection can be reused by connection manager
        }
    }
}
