package com.opentok.api;

import java.util.Map;
import java.util.Map.Entry;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokRequestException;

public class OpenTokHTTPClient {
    
    private static final AsyncHttpClient client = new AsyncHttpClient();
    private static final String apiUrl = "https://api.tokbox.com";
    private static int apiKey;
    private static String apiSecret;
    
    protected static void initialize(int apiKey, String apiSecret) {
        OpenTokHTTPClient.apiKey = apiKey;
        OpenTokHTTPClient.apiSecret = apiSecret;
    }

    protected static String makeDeleteRequest(String resource) throws OpenTokException {
        BoundRequestBuilder get = client.prepareDelete(apiUrl + resource);
        addCommonHeaders(get);

        try {
            Response result = get.execute().get();
            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: "
                        + result.getStatusText());
            }
            return result.getResponseBody();
        } catch (OpenTokRequestException e) {
            throw new OpenTokRequestException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw new OpenTokRequestException(500, e.getMessage());
        }
    }
 
    protected static String makeGetRequest(String resource) throws OpenTokException {
        BoundRequestBuilder get = client.prepareGet(apiUrl + resource);
        addCommonHeaders(get);

        try {
            Response result = get.execute().get();
            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: "
                        + result.getStatusText());
            }
            return result.getResponseBody();
        } catch (OpenTokRequestException e) {
            throw new OpenTokRequestException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw new OpenTokRequestException(500, e.getMessage());
        }
    }

    protected static String makePostRequest(String resource, Map<String, String> headers, Map<String, String> params,
            String postData) throws OpenTokException {
        BoundRequestBuilder post = client.preparePost(apiUrl + resource);
        if (params != null) {
            for (Entry<String, String> pair : params.entrySet()) {
                post.addParameter(pair.getKey(), pair.getValue());
            }
        }

        if (headers != null) {
            for (Entry<String, String> pair : headers.entrySet()) {
                post.addHeader(pair.getKey(), pair.getValue());
            }
        }

        addCommonHeaders(post);

        if (postData != null) {
            post.setBody(postData);
        }
        
        try {
            Response result = post.execute().get();

            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new OpenTokRequestException(result.getStatusCode(), "Error response: message: " + result.getStatusText());
            }

            return result.getResponseBody();
        } catch (OpenTokRequestException e) {
            throw new OpenTokRequestException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            throw new OpenTokRequestException(500, e.getMessage());
        }
    }

    private static void addCommonHeaders(BoundRequestBuilder get) {
        get.addHeader("X-TB-PARTNER-AUTH", String.format("%s:%s", apiKey, apiSecret));
        get.addHeader("X-TB-VERSION", "1");
        get.addHeader("User-Agent", "OpenTok-Java-SDK/2.0.0-beta");
    }
}
