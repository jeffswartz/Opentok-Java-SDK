package com.opentok.archivesample;

import com.opentok.api.OpenTok;
import com.opentok.exception.OpenTokException;


public class ArchiveHelper {
	
	private static final String apiKey = "100";
	private static final String apiSecret = "19f149fdf697474f915f13de40e0ad53";
	private static final String sessionId = "2_MX4xMDB-flR1ZSBOb3YgMTkgMTE6MDk6NTggUFNUIDIwMTN-MC4zNzQxNzIxNX4";
	final OpenTok api = new OpenTok( Integer.parseInt(apiKey),apiSecret );
	
	
	
	public ArchiveHelper(){
		
	}
	
	//This is a getter for the java sdk, different from getApiKey()
	public OpenTok getApi(){
		return new OpenTok( Integer.parseInt(getApiKey()),getApiSecret() );
		
	}
	
    
    public String getApiKey(){
    	return this.apiKey;
    }
    
    public String getApiSecret(){
    	return this.apiSecret;
    }
    
    public String getSessionId(){
    	return this.sessionId;
    }
		
    public String generateToken() throws OpenTokException{	
    	return getApi().generateToken(this.sessionId);
			
    }

}
