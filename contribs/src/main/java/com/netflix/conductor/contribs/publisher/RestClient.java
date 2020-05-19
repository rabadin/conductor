package com.netflix.conductor.contribs.publisher;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
    //private static final String URL = "http://bullwinkle.default:7979/v1/workflow";
    private static final String URL = "http://bullwinkle.default.svc.cluster.local:7979/v1";
    private static final String HEADER_DOMAIN_GROUP = "X-Starship-DomainGroup";
    private static final String HEADER_ACCOUNT_COOKIE = "x-barracuda-account";
    private static final String HEADER_PREFER = "Prefer";
    private static final String HEADER_PREFER_VALUE = "respond-async";
    private Client client = Client.create();
    // default timeout value for all requests

    public Client getClient(String notificationType) {
        return client;
    }

    public String createUrl(String notificationType) {
        return URL + "/" + notificationType;
    }

    public void post(String uri, String input, String domainGroupMoId, String accountMoId) {
        LOGGER.info("URL: " + uri);
        LOGGER.info("Input: " + input);
        LOGGER.info("domainGroupId: " + domainGroupMoId);
        int timeoutInSeconds = 10;
        client.setConnectTimeout(1000 * timeoutInSeconds);
        client.setReadTimeout(1000 * timeoutInSeconds );

        try {
            WebResource webResource = client.resource(uri);
            ClientResponse response = webResource.type("application/json")
                    .header(HEADER_DOMAIN_GROUP, domainGroupMoId)
                    .header(HEADER_ACCOUNT_COOKIE, accountMoId)
                    .header(HEADER_PREFER, HEADER_PREFER_VALUE)
                    .post(ClientResponse.class, input);
            if (response.getStatus() != HttpStatus.SC_ACCEPTED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            response.bufferEntity();
            LOGGER.info("Response" + response.getEntity(String.class));
        }
        catch (Exception e) {
            LOGGER.info("#####3" + e.toString());
            e.printStackTrace();
        }
    }

    public void get(String uri) {
        LOGGER.info("URL: " + uri);
        try {
            LOGGER.info("#####1");
            WebResource webResource = client.resource(uri);
            ClientResponse response = webResource.type("application/json")
                    .get(ClientResponse.class);
            LOGGER.info("#####2");
            LOGGER.info(response.toString());
            if (response.getStatus() != 200) {
                LOGGER.info("Failed : HTTP error code : "
                        + response.getStatus());
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
            LOGGER.info("Record sent with Status " + response.getEntity(String.class));
        }
        catch (Exception e) {
            LOGGER.info("#####3" + e.toString());
            e.printStackTrace();
        }
    }
}
