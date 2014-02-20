package com.opentok.archivesample;

public enum ErrorResponse {
	INVALIDSESSION("Invalid SessionId or invalid action or no clients connected to OT session"), 
	INVALIDKEY("Invalid API_KEY or PARTNER_SECRET"), 
	NOSESSION("Session does not exist"), 
	STARTRECORDING("Session already being recorded or you are attempting to record p2p session"),
	OTERROR("OpenTok Error"),
	STOPRECORDING("Attempting to stop an archive not currently recorded");
 
	private String statusCode;
 
	private ErrorResponse(String s) {
		statusCode = s;
	}
 
	public String getStatusCode() {
		return statusCode;
	}
 
}
