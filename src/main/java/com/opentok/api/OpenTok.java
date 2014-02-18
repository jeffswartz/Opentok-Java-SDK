
/*!
 * OpenTok Java Library
 * http://www.tokbox.com/
 *
 * Copyright 2010, TokBox, Inc.
 *
 * Last modified: @opentok.sdk.java.mod_time@
 */

package com.opentok.api;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.opentok.api.constants.RoleConstants;
import com.opentok.api.constants.SessionProperties;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.OpenTokInvalidArgumentException;
import com.opentok.exception.OpenTokRequestException;
import com.opentok.exception.OpenTokSessionNotFoundException;
import com.opentok.util.TokBoxXML;

/**
* Contains methods for creating OpenTok sessions and generating tokens.
* <p>
* To create a new OpenTokSDK object, call the OpenTokSDK constructor with your OpenTok API key
* and the API secret from <a href="https://dashboard.tokbox.com">the OpenTok dashboard</a>. Do not publicly share
* your API secret. You will use it with the OpenTokSDK constructor (only on your web
* server) to create OpenTok sessions.
* <p>
* Be sure to include the entire OpenTok server SDK on your web server.
*/
public class OpenTok {

    private int apiKey;
    private String apiSecret;

    /**
     * Creates an OpenTokSDK object.
     *
     * @param apiKey Your OpenTok API key. (See the <a href="https://dashboard.tokbox.com">OpenTok dashboard</a>
     * page)
     * @param apiSecret Your OpenTok API secret. (See the <a href="https://dashboard.tokbox.com">OpenTok dashboard</a>
     * page)
     */
    public OpenTok(int apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret.trim();
        OpenTokHTTPClient.initialize(apiKey, apiSecret);
    }

    /**
     * Creates a token for connecting to an OpenTok session. In order to authenticate a user connecting to an OpenTok session
     * that user must pass an authentication token along with the API key.
     * The following Java code example shows how to obtain a token:
     * <p>
     * <pre>
     * import com.opentok.api.API_Config;
     * import com.opentok.api.OpenTokSDK;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         // Set the following constants with the API key and API secret
     *         // that you receive when you sign up to use the OpenTok API:
     *         OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);
     *
     *         //Generate a basic session. Or you could use an existing session ID.
     *         String sessionId = System.out.println(sdk.createSession());
     *
     *         String token = sdk.generateToken(sessionId);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * <p>
     * The following Java code example shows how to obtain a token that has a role of "subscriber" and that has
     * a connection metadata string:
     * <p>
     * <pre>
     * import com.opentok.api.API_Config;
     * import com.opentok.api.OpenTokSDK;
     * import com.opentok.api.constants.RoleConstants;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         // Set the following constants with the API key and API secret
     *         // that you receive when you sign up to use the OpenTok API:
     *         OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);
     *
     *         //Generate a basic session. Or you could use an existing session ID.
     *         String sessionId = System.out.println(sdk.createSession());
     *
     *         // Replace with meaningful metadata for the connection.
     *         String connectionMetadata = "username=Bob,userLevel=4";
     *
     *         // Use the RoleConstants value appropriate for the user.
     *         String role = RoleConstants.SUBSCRIBER;
     *
     *         // Generate a token.
     *         String token = sdk.generateToken(sessionId, RoleConstants.PUBLISHER, null, connectionMetadata);
     *         System.out.println(token);
     *     }
     * }
     * </pre>
     * <p>
     * For testing, you can also use the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>
     * page to generate test tokens.
     *
     * @param sessionId The session ID corresponding to the session to which the user will connect.
     *
     * @param role Each role defines a set of permissions granted to the token.
     * Valid values are defined in the RoleConstants class:
     *
     *   * `SUBSCRIBER` &mdash; A subscriber can only subscribe to streams.</li>
     *
     *   * `PUBLISHER` &mdash; A publisher can publish streams, subscribe to streams, and signal.
     *     (This is the default value if you do not specify a value for the `role` parameter.)</li>
     *
     *   * `MODERATOR` &mdash; In addition to the privileges granted to a publisher, a moderator
     *     can call the `forceUnpublish()` and `forceDisconnect()` method of the
     *     Session object.</li>
     *
     * @param expireTime The expiration time, in seconds, since the UNIX epoch. Pass in 0 to use
     * the default expiration time of 24 hours after the token creation time. The maximum expiration
     * time is 30 days after the creation time.
     *
     * @param connectionData A string containing metadata describing the end-user. For example, you can pass the
     * user ID, name, or other data describing the end-user. The length of the string is limited to 1000 characters.
     * This data cannot be updated once it is set.
     *
     * @return The token string.
     */
    public String generateToken(String sessionId, String role, long expireTime, String connectionData) throws OpenTokException {

        if(sessionId == null || sessionId == "") {
            throw new OpenTokInvalidArgumentException("Session not valid");
        }
        String decodedSessionId = "";
        try { 
            String subSessionId = sessionId.substring(2);
            for (int i = 0; i<3; i++){
                String newSessionId = subSessionId.concat(repeatString("=",i));
                decodedSessionId = new String(DatatypeConverter.parseBase64Binary(
                        newSessionId.replace('-', '+').replace('_', '/')), "ISO8859_1");
                if (decodedSessionId.contains("~")){ 
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new OpenTokSessionNotFoundException("Session not found");
        }

        if(!decodedSessionId.split("~")[1].equals(String.valueOf(apiKey))) {
            throw new OpenTokSessionNotFoundException("Session not found");
        }
        
        Session session = new Session(sessionId, apiKey, apiSecret);
        return session.generateToken(role, expireTime, connectionData);
    }

    

    /**
     * Generates the token for the given session. The role is set to publisher, the token expires in
     * 24 hours, and there is no connection data.
     *
     * @param sessionId The session ID.
     *
     * @see #generateToken(String sessionId, String role, long expireTime, String connectionData)
     */
    public String generateToken(String sessionId) throws OpenTokException {
        return this.generateToken(sessionId, RoleConstants.PUBLISHER, 0, null);
    }


    /**
     * Generates the token for the given session and with the specified role. The token expires in
     * 24 hours, and there is no connection data.
     *
     * @param sessionId The session ID.
     * @param role The role assigned to the token.
     *
     * @see #generateToken(String sessionId, String role, long expireTime, String connectionData)
     */
    public String generateToken(String sessionId, String role) throws OpenTokException {
        return this.generateToken(sessionId, role, 0, null);
    }

    /**
     * Generates the token for the given session, with the specified role and expiration time.
     * There is no connection data.
     *
     * @param sessionId The session ID.
     * @param role The role assigned to the token.
     * @param expireTime The expiration time, in seconds, since the UNIX epoch. Pass in 0 to use
     * the default expiration time of 24 hours after the token creation time. The maximum expiration
     * time is 30 days after the creation time.
     *
     * @see #generateToken(String sessionId, String role, long expireTime, String connectionData)
     */
    public String generateToken(String sessionId, String role, Long expireTime) throws OpenTokException {
        return this.generateToken(sessionId, role, expireTime, null);
    }

    /**
     * Creates an OpenTok session and returns the session ID, with the default properties. The
     * session uses the OpenTok media server. And the session uses the first client connecting
     * to determine the location of OpenTok server to use.
     *
     * @see #createSession(SessionProperties)
     */
    public Session createSession() throws OpenTokException {
        return createSession(null);
    }

    /**
     * Creates a new OpenTok session and returns the session ID, which uniquely identifies the session.
     * <p>
     * For example, when using the OpenTok JavaScript library,
     * use the session ID in JavaScript on the page that you serve to the client. The JavaScript will use this
     * value when calling the <a href="http://tokbox.com/opentok/libraries/client/js/reference/Session.html#connect">connect()</a>
     * method of the Session object (to connect a user to an OpenTok session).
     * <p>
     * OpenTok sessions do not expire. However, authentication tokens do expire (see the
     * {@link #generateToken(String, String, long, String)} method).
     * Also note that sessions cannot explicitly be destroyed.
     * <p>
     * A session ID string can be up to 255 characters long.
     * <p>
     * Calling this method results in an {@link com.opentok.exception.OpenTokException} in the event of an error. Check
     * the error message for details.
     * <p>
     * The following code creates an OpenTok server-enabled session:
     *
     * <pre>
     * import com.opentok.api.API_Config;
     * import com.opentok.api.OpenTokSDK;
     * import com.opentok.api.constants.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);
     *
     *         String sessionId = sdk.createSession();
     *         System.out.println(sessionId);
     *     }
     * }
     * </pre>
     *
     * The following code creates a peer-to-peer session:
     *
     * <pre>
     * import com.opentok.api.API_Config;
     * import com.opentok.api.OpenTokSDK;
     * import com.opentok.api.constants.SessionProperties;
     *
     * class Test {
     *     public static void main(String argv[]) throws OpenTokException {
     *         OpenTokSDK sdk = new OpenTokSDK(API_Config.API_KEY, API_Config.API_SECRET);
     *
     *         SessionProperties sp = new SessionProperties();
     *         sp.p2p_preference = "enabled";
     *
     *         String sessionId = sdk.createSession(null, sp);
     *         System.out.println(sessionId);
     *     }
     * }
     * </pre>
     *
     * You can also create a session using the <a href="http://www.tokbox.com/opentok/api/#session_id_production">OpenTok
     * REST API</a> or the <a href="https://dashboard.tokbox.com/projects">OpenTok dashboard</a>.
     *
     * @param properties Defines whether the session's streams will be transmitted directly between peers or
     * using the OpenTok media server. You can set the following possible values:
     * <p>
     * <ul>
     *   <li>
     *     "disabled" (the default) &mdash; The session's streams will all be relayed using the OpenTok media server.
     *     <br><br>
     *     <i>In OpenTok v2:</i> The <a href="http://www.tokbox.com/blog/mantis-next-generation-cloud-technology-for-webrtc/">OpenTok
     *     media server</a> provides benefits not available in peer-to-peer sessions. For example, the OpenTok media server can
     *     decrease bandwidth usage in multiparty sessions. Also, the OpenTok server can improve the quality of the user experience
     *     through <a href="http://www.tokbox.com/blog/quality-of-experience-and-traffic-shaping-the-next-step-with-mantis/">dynamic
     *     traffic shaping</a>. For information on pricing, see the <a href="http://www.tokbox.com/pricing">OpenTok pricing page</a>.
     *     <br><br>
     *   </li>
     *   <li>
     *     "enabled" &mdash; The session will attempt to transmit streams directly between clients.
     *     <br><br>
     *     <i>In OpenTok v1:</i> Peer-to-peer streaming decreases latency and improves quality. If peer-to-peer streaming
     *     fails (either when streams are initially published or during the course of a session), the session falls back to using
     *     the OpenTok media server to relaying streams. (Peer-to-peer streaming uses UDP, which may be blocked by a firewall.)
     *     For a session created with peer-to-peer streaming enabled, only two clients can connect to the session at a time.
     *     If an additional client attempts to connect, the client dispatches an exception event.
     *   </li>
     * </ul>
     *
     * @return A session ID for the new session. For example, when using the OpenTok JavaScript library, use this session ID
     * in JavaScript on the page that you serve to the client. The JavaScript will use this value when calling the
     * <code>connect()</code> method of the Session object (to connect a user to an OpenTok session).
     */
    public Session createSession(SessionProperties properties) throws OpenTokException {
        Map<String, String> params;
        if(null != properties) {
            params = properties.toMap();
        } else {
            params = new HashMap<String, String>();
        }
        
        TokBoxXML xmlResponse = new TokBoxXML(OpenTokHTTPClient.makePostRequest("/session/create", null, params, null)); 
                
        if(xmlResponse.hasElement("error", "Errors")) {
            throw new OpenTokRequestException(500, "Unable to create session");
        }
        return new Session(xmlResponse.getElementValue("session_id", "Session"), apiKey, apiSecret, properties);
    }

    private static String repeatString(String str, int times){
        StringBuilder ret = new StringBuilder();
        for(int i = 0;i < times;i++) ret.append(str);
        return ret.toString();
    }
    
    /**
     * Gets an {@link Archive} object for the given archive ID.
     *
     * @param archiveId The archive ID.
     * @return The {@link Archive} object.
     */
    public Archive getArchive(String archiveId) throws OpenTokException {
        ObjectMapper mapper = new ObjectMapper();
        String archive = OpenTokHTTPClient.makeGetRequest("/v2/partner/" + this.apiKey + "/archive/" + archiveId);
        try {
            return mapper.readValue(archive, Archive.class);
        } catch (Exception e) {
            throw new OpenTokRequestException(500, "Exception mapping json: " + e.getMessage());
        }
        
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key. This list is limited to 1000 archives
     * starting with the first archive recorded. For a specific range of archives, call
     * {@link #listArchives(int offset, int count)}.
     *
     * @return A List of {@link Archive} objects.
     */
    public List<Archive> listArchives() throws OpenTokException {
        return listArchives(0, 1000);
    }

    /**
     * Returns a List of {@link Archive} objects, representing archives that are both
     * both completed and in-progress, for your API key.
     *
     * @param offset The index offset of the first archive. 0 is offset of the most recently started archive.
     * 1 is the offset of the archive that started prior to the most recent archive.
     * @param count The number of archives to be returned. The maximum number of archives returned is 1000.
     * @return A List of {@link Archive} objects.
     */
    public List<Archive> listArchives(int offset, int count) throws OpenTokException {
        ObjectMapper mapper = new ObjectMapper();
        String archive = OpenTokHTTPClient.makeGetRequest("/v2/partner/" + this.apiKey + "/archive?offset=" + offset + "&count="
                + count);
        try {
            JsonNode node = mapper.readTree(archive);
            return mapper.readValue(node.get("items"), new TypeReference<List<Archive>>() {
            });
        } catch (Exception e) {
            throw new OpenTokRequestException(500, "Exception mapping json: " + e.getMessage());
        }
    }
    
    /**
     * Starts archiving an OpenTok 2.0 session.
     *
     * <p>
     * Clients must be actively connected to the OpenTok session for you to successfully start recording an archive.
     * <p>
     * You can only record one archive at a time for a given session. You can only record archives of OpenTok
     * server-enabled sessions; you cannot archive peer-to-peer sessions.
     *
     * @param sessionId The session ID of the OpenTok session to archive.
     * @param name The name of the archive. You can use this name to identify the archive. It is a property
     * of the Archive object, and it is a property of archive-related events in the OpenTok JavaScript SDK.
     *
     * @return The Archive object. This object includes properties defining the archive, including the archive ID.
     */
    public Archive startArchive(String sessionId, String name) throws OpenTokException {
        if (sessionId == null || sessionId == "") {
            throw new OpenTokInvalidArgumentException("Session not valid");
        }
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        String archive = OpenTokHTTPClient.makePostRequest("/v2/partner/" + this.apiKey + "/archive", headers, null,
                "{ \"sessionId\" : \"" + sessionId + "\", \"name\": \"" + name + "\" }");
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(archive, Archive.class);
        } catch (Exception e) {
            throw new OpenTokRequestException(500, "Exception mapping json: " + e.getMessage());
        }
    }

    /**
     * Stops an OpenTok archive that is being recorded.
     * <p>
     * Archives automatically stop recording after 90 minutes or when all clients have disconnected from the
     * session being archived.
     *
     * @param archiveId The archive ID of the archive you want to stop recording.
     * @return The Archive object corresponding to the archive being stopped.
     */
    public Archive stopArchive(String archiveId) throws OpenTokException {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "application/json");
        String archive = OpenTokHTTPClient.makePostRequest("/v2/partner/" + this.apiKey + "/archive/" + archiveId + "/stop", headers, null,
                "");
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(archive, Archive.class);
        } catch (Exception e) {
            throw new OpenTokRequestException(500, "Exception mapping json: " + e.getMessage());
        }
    }
    
    /**
     * Deletes an OpenTok archive.
     * <p>
     * You can only delete an archive which has a status of "available" or "uploaded". Deleting an archive
     * removes its record from the list of archives. For an "available" archive, it also removes the archive
     * file, making it unavailable for download.
     *
     * @param archiveId The archive ID of the archive you want to delete.
     */
    public void deleteArchive(String archiveId) throws OpenTokException {
        OpenTokHTTPClient.makeDeleteRequest("/v2/partner/" + this.apiKey + "/archive/" + archiveId);
    }
}
