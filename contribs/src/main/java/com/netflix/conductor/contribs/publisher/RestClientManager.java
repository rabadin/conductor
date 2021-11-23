package com.netflix.conductor.contribs.publisher;

import com.google.inject.Inject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
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
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class RestClientManager {
    private static final Logger logger = LoggerFactory.getLogger(RestClientManager.class);
    private PublisherConfiguration config;
    private CloseableHttpClient client;
    private String notifType;
    private String notifId;

    enum NotificationType {
        TASK,
        WORKFLOW
    };

    public RestClientManager(PublisherConfiguration config){
        this.config = config;
        this.client = prepareClient();
    }

    private PoolingHttpClientConnectionManager prepareConnManager (){
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(config.getConnectionPoolMaxRequest());
        connManager.setDefaultMaxPerRoute(config.getConnectionPoolMaxRequestPerRoute());
        return connManager;
    }

    private RequestConfig prepareRequestConfig() {
        return RequestConfig.custom()
                // The time to establish the connection with the remote host [http.connection.timeout].
                // Responsible for java.net.SocketTimeoutException: connect timed out.
                .setConnectTimeout(config.getRequestTimeoutInMillisec())

                // The time waiting for data after the connection was established [http.socket.timeout]. The maximum time
                // of inactivity between two data packets. Responsible for java.net.SocketTimeoutException: Read timed out.
                .setSocketTimeout(config.getSocketTimeoutInMillisec())

                // The time to wait for a connection from the connection manager/pool [http.connection-manager.timeout].
                // Responsible for org.apache.http.conn.ConnectionPoolTimeoutException.
                .setConnectionRequestTimeout(config.getConnectionMgrTimeoutInMillisec()).build();
    }

    /**
     * Custom HttpRequestRetryHandler implementation to customize retries for different IOException
     */
    private class CustomHttpRequestRetryHandler implements HttpRequestRetryHandler {
        int maxRetriesCount    = config.getRequestRetryCount();
        int retryIntervalInMilisec = config.getRequestRetryInterval();

        /**
         * Triggered only in case of exception
         *
         * @param exception      The cause
         * @param executionCount Retry attempt sequence number
         * @param context        {@link HttpContext}
         * @return True if we want to retry request, false otherwise
         */
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            Throwable rootCause = ExceptionUtils.getRootCause(exception);
            logger.warn("Retrying {} notification. Id: {}, root cause: {}", notifType, notifId, rootCause.toString());

            if (executionCount >= maxRetriesCount) {
                logger.warn("{} notification failed after {} retries. Id: {} .", notifType, executionCount, notifId);
                return false;
//            } else if (rootCause instanceof SocketTimeoutException) {
//                return true;
            } else if (rootCause instanceof SocketException
                    || rootCause instanceof InterruptedIOException
                    || exception instanceof SSLException) {
                try {
                    Thread.sleep(retryIntervalInMilisec);
                } catch (InterruptedException e) {
                    e.printStackTrace(); // do nothing
                }
                return true;
            } else
                return false;
        }
    }

    /**
     * Custom ServiceUnavailableRetryStrategy implementation to retry on HTTP 503 (= service unavailable)
     */
    private class CustomServiceUnavailableRetryStrategy implements ServiceUnavailableRetryStrategy {
        int maxRetriesCount    = config.getRequestRetryCount();
        int retryIntervalInMilisec = config.getRequestRetryInterval();

        @Override
        public boolean retryRequest(final HttpResponse response, final int executionCount, final HttpContext context) {

            int httpStatusCode = response.getStatusLine().getStatusCode();
            if (httpStatusCode != 503)
                return false; // retry only on HTTP 503

            if (executionCount >= maxRetriesCount) {
                logger.warn("HTTP 503 error. {} notification failed after {} retries. Id: {} .", notifType, executionCount, notifId);
                return false;
            } else {
                logger.warn("HTTP 503 error. {} notification failed after {} retries. Id: {} .", notifType, executionCount, notifId);
                return true;
            }
        }

        @Override
        public long getRetryInterval() {
            // Retry interval between subsequent requests, in milliseconds.
            // If not set, the default value is 1000 milliseconds.
            return retryIntervalInMilisec;
        }
    }

    // by default retries 3 times
    private CloseableHttpClient prepareClient() {
        return HttpClients.custom()
                .setConnectionManager(prepareConnManager())
                .setDefaultRequestConfig(prepareRequestConfig())
                .setRetryHandler(new CustomHttpRequestRetryHandler())
                .setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy())
                .build();
    }

    void postNotification(RestClientManager.NotificationType notifType, String data, String domainGroupMoId, String accountMoId, String id) throws IOException {
        this.notifType = notifType.toString();
        notifId = id;
        String url = prepareUrl(notifType);

        Map<String, String> headers = new HashMap<>();
        headers.put(config.getHeaderPrefer(), config.getHeaderPreferValue());
        headers.put(config.getHeaderDomainGroup(), domainGroupMoId);
        headers.put(config.getHeaderAccountCookie(), accountMoId);

        HttpPost request = createPostRequest(url, data, headers);
        long start = System.currentTimeMillis();
        executePost(request);
        long end = System.currentTimeMillis();
        logger.info("Round trip response time = " + (end-start) + " millis");
    }

    private String prepareUrl(RestClientManager.NotificationType notifType) {
        String urlEndPoint = "";

        if (notifType == RestClientManager.NotificationType.TASK) {
            urlEndPoint = config.getEndPointTask();

        }
        else if (notifType == RestClientManager.NotificationType.WORKFLOW){
            urlEndPoint = config.getEndPointWorkflow();
        }
        return config.getNotificationUrl() + "/" + urlEndPoint;
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