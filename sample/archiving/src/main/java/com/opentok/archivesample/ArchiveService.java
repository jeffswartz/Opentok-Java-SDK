package com.opentok.archivesample;


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.opentok.api.Archive;
import com.opentok.api.ArchiveList;
import com.opentok.exception.OpenTokException;


@Path("/jsonServices")
public class ArchiveService {

	private static String responseString;


	ArchiveHelper archiveHelper = new ArchiveHelper();  


	@POST
	@Path("/start/{sessionid}/{archivename}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject startArchiving(@PathParam ("sessionid") String sessionId, @PathParam("archivename") String archiveName) throws JSONException{

		Archive archive = null;

		try{
			archive = archiveHelper.getApi().startArchive(sessionId, archiveName);
		}
		catch (OpenTokException ote){

			switch(ote.getErrorCode()){
			case 400: responseString = ErrorResponse.INVALIDSESSION.getStatusCode();
			break;
			case 403: responseString = ErrorResponse.INVALIDKEY.getStatusCode();
			break;
			case 404: responseString = ErrorResponse.NOSESSION.getStatusCode();
			break;
			case 409: responseString = ErrorResponse.STARTRECORDING.getStatusCode();
			break;
			case 500: responseString = ErrorResponse.OTERROR.getStatusCode();
			break;
			}

			JSONObject errorObject = new JSONObject();
			errorObject.put("errorcode", String.valueOf(ote.getErrorCode()));
			errorObject.put("errormessage", responseString );

			return errorObject;
		}
		return new JSONObject(archive.toString());    
	}


	@POST
	//@Path("/start/{sessionid}/{archivename}/{apikey}/{apisecret}")
	@Path("/stop/{archiveid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONObject stopArchiving(@PathParam ("archiveid") String archiveId) throws JSONException{

		Archive archive = null;

		try{
			archive = archiveHelper.getApi().stopArchive(archiveId);
		}
		catch(OpenTokException ote){
			switch(ote.getErrorCode()){
			case 400: responseString = ErrorResponse.INVALIDSESSION.getStatusCode();
			break;
			case 403: responseString = ErrorResponse.INVALIDKEY.getStatusCode();
			break;
			case 409: responseString = ErrorResponse.STOPRECORDING.getStatusCode();
			break;
			case 500: responseString = ErrorResponse.OTERROR.getStatusCode();
			break;

			}

			JSONObject errorObject = new JSONObject();
			errorObject.put("errorcode", String.valueOf(ote.getErrorCode()));
			errorObject.put("errormessage", responseString);
			return errorObject;

		}

		return new JSONObject(archive.toString());

	}


	@DELETE
	@Path("/delete/{archiveid}" )
	public JSONObject deleteArchive(@PathParam ("archiveid") String archiveId) throws JSONException{
		
		JSONObject successResponse = new JSONObject(); 
		JSONObject errorObject = new JSONObject() ;
		try{
			archiveHelper.getApi().deleteArchive(archiveId);
		}
		catch(OpenTokException ote){ 
			switch(ote.getErrorCode()){
			case 403: responseString = "Invalid API_KEY or PARTNER_SECRET";
			break;
			case 500: responseString = "OpenTok Error";
			break;

			}
			errorObject.put("errorCode", String.valueOf(ote.getErrorCode()));
			errorObject.put("errorMessage", responseString);
			return errorObject;
		}
			
		successResponse.put("Message", "Archive Deleted");
		return successResponse;
		

	}

	
	@GET
	@Path("/listarchives")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listAllArchives(@QueryParam ("offset") @DefaultValue("0") String offset, @QueryParam("count") @DefaultValue("50") String count) throws JSONException{
		JSONObject errorObject = new JSONObject();
		ArchiveList archiveList = null;
		try{
			
			if(offset == null && count == null){
				archiveHelper.getApi().listArchives();
			}
			else{
				archiveList = archiveHelper.getApi().listArchives(Integer.parseInt(offset), Integer.parseInt(count));
				
			}

		}
		catch(OpenTokException ote){ 
			switch(ote.getErrorCode()){
			case 403: responseString = ErrorResponse.INVALIDKEY.getStatusCode();
			break;
			case 500: responseString = ErrorResponse.OTERROR.getStatusCode();
			break;
			}
			errorObject = new JSONObject();
			errorObject.put("errorcode", String.valueOf(ote.getErrorCode()));
			errorObject.put("errormessage", responseString);
			return errorObject;

		}
	
		return new JSONObject("{\"count\":" + archiveList.getCount() 
						+ ", \"items\":" +  archiveList.getItems().toString() + "}");

	}



	@GET
	@Path("/retrievecredentials")
	//@Produces(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getSessionIdAndToken() throws JSONException{

		JSONObject object = new JSONObject();
		JSONObject errorObject = new JSONObject();

		try {
			object.put("sessionId", archiveHelper.getSessionId());
			object.put("token", archiveHelper.generateToken());
			object.put("apiKey", archiveHelper.getApiKey());

		} 
		catch (OpenTokException ote){
			ote.printStackTrace();
			return errorObject.put("Error while generating token", ote.getMessage());
		}

		return object;

	}


}
