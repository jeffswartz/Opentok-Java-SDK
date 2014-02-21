OpenTok 2.0 Archiving Sample App -- Java
========================================

A sample app showing use of the OpenTok 2.0 archiving API.

This sample app is for use with the OpenTok 2.0 Java SDK. 

## Prerequisites

1. Tomcat (<http://tomcat.apache.org/>).

   The OpenTok archiving API does not require Tomcat. However, this sample uses Tomcat as
   a web server and to make calls to the OpenTok Java library.

2. An OpenTok API key and secret (see <https://dashboard.tokbox.com>)

## Setup

Use Maven to install the archiving sample app. From this archiving directory, run:

    mvn clean install

This creates a JAXRS-ArchiveSample.war file in the target subdirectory. Deploy this file to your
Tomcat server (into the Tomcat webapps directory).

## To run the app for yourself

1. Open the http://localhost:8080/JAXRS-ArchiveSample/ page. (Replace localhost:8080 with the
   path to your Tomcat server.)

2. Click the "Host view" button. The Host View page publishes an audio-video stream to an
   OpenTok session. It also includes controls that cause the web server to start and stop archiving.

3. Click the Allow button to grant access to the camera and microphone.

4. Click the "Start archiving" button. The session starts recording to an archive. Note that the
   red archiving indicator is displayed in the video view.

5. Open the JAXRS-ArchiveSample in a new browser tab. (You may want to mute your computer speaker
   to prevent feedback. You are about to publish two audio-video streams from the same computer.)

6. Click the "Participant view" button. The page connects to the OpenTok session, displays
   the existing stream (from the Host View page).

7. Click the Allow button in the page to grant access to the camera and microphone. The page
   publishes a new stream to the session. Both streams are now being recorded to an archive.

8. On the Host View page, click the "Stop archiving" button.

9. Click the "past archives" link in the page. This page lists the archives that have been recorded.
   Note that it may take up to 2 minutes for the video file to become available (for a 90-minute 
   recording).

10. Click a listing for an available archive to download the MP4 file of the recording.

## Understanding the code

This sample app shows how to use the archiving API in the OpenTok 2.0 Java SDK.

### Starting an archive

The host-view.html file (in the views directory) includes a button for starting the archive.
When the user clicks this button, the page makes an Ajax call back to the server:

    $(".start").click(function(event){
      $.post(Config.startArchiveURL + "/" + session.sessionId + "/archive"
             + (new Date()).getTime());
    });
    

The startArchiving() method of the com.opentok.archivesample.ArchiveService class handles this
HTTP request:

    Archive archive = null;

    try {
        archive = archiveHelper.getApi().startArchive(sessionId, archiveName);
    }
    catch (OpenTokException ote) {
        // ..
    }
    
The getApi() method of the ArchiveHelper object returns an OpenTok object. The OpenTok class is
defined in the OpenTok Java SDK. The startArchive() method of the OpenTok object starts an archive,
and it takes two parameters:

* The sesssion ID of the OpenTok session to archive
* A name (which is optional and helps identify the archive)

You can only start recording an archive in a session that has active clients connected.

In the page on the web client, the Session object (in JavaScript) dispatches an archiveStarted
event. The page stores the archive ID (a unique identifier of the archive) in an archiveID variable:

    session.on('archiveStarted', function(event) {
      archiveID = event.id;
      console.log("ARCHIVE STARTED");
      $(".start").hide();
      $(".stop").show();
    });

### Stopping an archive

The host-view.html file includes a button for stopping the archive. When the user clicks this
button, the page makes an Ajax call back to the server:

    $(".stop").click(function(event){
      $.post(Config.stopArchiveURL + "/" + archiveID);
    });

The stopArchiving() method of the ArchiveService class handles this HTTP request:

    Archive archive = null;

    try {
        archive = archiveHelper.getApi().stopArchive(archiveId);
    }
    catch (OpenTokException ote) {
        // ..
    }

This code calls the stopArchive() method of the OpenTok object. This method stops an archive,
based on the archive ID.

In the page on the web client, the Session object (in JavaScript) dispatches an archiveStopped
event. The page stores the archive ID (a unique identifier of the archive) in an archiveID variable:

    session.on('archiveStopped', function(event) {
      archiveID = null;
      console.log("ARCHIVE STOPPED");
      $(".start").show();
      $(".stop").hide();
    });

### Listing archives

The listAllArchives() method of the ArchiveService class handles this HTTP request:

    @GET
    @Path("/listarchives")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject listAllArchives(@QueryParam ("offset") @DefaultValue("0") String offset, 
                                      @QueryParam("count") @DefaultValue("50") String count)
                                      throws JSONException {
        JSONObject errorObject = new JSONObject();
        ArchiveList archiveList = null;
        try {
            
            if (offset == null && count == null){
                archiveHelper.getApi().listArchives();
            }
            else{
                archiveList = archiveHelper.getApi().listArchives(Integer.parseInt(offset),
                                                                  Integer.parseInt(count));
            }
        }
        catch(OpenTokException ote) { 
            // ..
        }
    
        return new JSONObject("{\"count\":" + archiveList.getCount() 
                        + ", \"items\":" +  archiveList.getItems().toString() + "}");
    }

This code calls the listArchives() method or the listArchives(int offset, int count) method
of the OpenTok object. These methods list archives created for the API key. 
The listArchives(int offset, int count) method takes two parameters:

* offset -- This defines which archive starts the list.
* count -- This defines how many archives are listed

The method returns an ArchiveList object. The getItems() method of this object is a List of Archive
objects. The toString() method of the ArchiveList object converts the List of Archive objects to
a JSON string representing the List of Archive objects.

An Archive object represents an OpenTok archive, and includes the following properties:

* id -- The archive ID, which uniquely defines the archive.
* name -- The archive name.
* createdAt -- The timestamp for when the archive was created.
* duration -- The duration of the archive (in seconds).
* status -- The status of the archive. This can be one of the following:
  * "available" -- The archive is available for download from the OpenTok cloud.
  * "failed" -- The archive recording failed.
  * "started" -- The archive started and is in the process of being recorded.
  * "stopped" -- The archive stopped recording.
  * "uploaded" -- The archive is available for download from an S3 bucket you specified (see
    [Setting an archive upload target](../../../REST-API.md#set_upload_target)).
* url -- The URL of the download file for the recorded archive (if available) 

The client web page iterates through the JSON data, representing the list of archive objects,
and it displays the id, name, createdAt, duration, status, and url properties of the object:

    function displayArchives(data) {
      // clear out the table first
      $("table > tbody").html("");
      for (var i = 0; i < data.items.length; i++) {
        var item = data.items[i];
        var tr = $("<tr></tr>");
        tr.append("<td>" + (item.url && item.status == "available" ? "<a href='" + item.url + "'>" : "") + (item.name ? item.name : "Untitled") + (item.url && item.status == "available" ? "</a>" : "") + "</td>");
        tr.append("<td>" + dateString(item.createdAt) + " at " + timeString(item.createdAt) + "</td>");
        tr.append("<td>" + item.duration + " seconds</td>");
        tr.append("<td>" + item.status + "</td>");

        if (item.status == "available") {
          var deleteLink = $("<a href='#delete-" + item.id + "'>Delete</a>");
          (function(archiveId) {
            deleteLink.click(function() {
              deleteArchive(archiveId);
              return false;
            })
          })(item.id);
          var deleteTD = $("<td></td>");
          deleteTD.append(deleteLink);
          tr.append(deleteTD);
        } else {
          tr.append("<td></td>");
        }
        $("table > tbody").append(tr);
      }
    }

### Downloading archives

The url property of an Archive object is the download URL for the available archive. See the
previous section, which shows how this URL is added to the past-archives.html page as a download
link.

### Deleting archives

The past-archives.html file includes buttons for deleting archives. When the user clicks one of
these buttons, the page makes an Ajax call back to the server:

    function deleteArchive(archiveId) {
      $.ajax({
          url: Config.deleteArchiveURL + "/" + archiveId,
          type: "DELETE",
          success: function(result) {
              fetchArchives();
          }
      });
    }

The listAllArchives() method of the ArchiveService class handles this HTTP request:

    try{
        archiveHelper.getApi().deleteArchive(archiveId);
    }
    catch(OpenTokException ote){ 
        // ..
    }

This code calls the deleteArchive() method of the OpenTok object. This method deletes an archive,
based on the archive ID.

## Documentation

* [OpenTok OpenTok Java SDK documentation](../../README.md)
* [Archiving JavaScript API documentation](../../../JavaScript-API.md)

## More information

See the list of known issues in the main [README file](../../../README.md).

The OpenTok 2.0 archiving feature is currently in beta testing.

If you have questions or to provide feedback, please write <denis@tokbox.com>.
