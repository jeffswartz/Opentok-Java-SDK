<?php
 
//Generic OpenTok exception. Read the message to get more details
class OpenTokArchivingException extends Exception { };
//OpenTok exception related to authentication. Most likely an issue with your API key or secret
class OpenTokArchivingAuthException extends OpenTokArchivingException { };
//OpenTok exception related to the HTTP request. Most likely due to a server error. (HTTP 500 error)
class OpenTokArchivingRequestException extends OpenTokArchivingException { };

class OpenTokArchivingRequestOptions {
  
  private $value;
  private $mode;
  
  function __construct($mode, $value) {
    $this->mode = $mode;
    $this->value = $value;
  }
  
  function dataString() {
    if($this->mode == 'json') {
      return json_encode($this->value);
    } elseif ($this->mode == 'form') {
      return $this->value;
    }
  }
  
  function contentType() {
    if($this->mode == 'json') {
      return "application/json";
    } elseif ($this->mode == 'form') {
      return "application/x-www-form-urlencoded";
    }
  }
  
}

class OpenTokArchivingInterface {
 
  function __construct($apiKey, $apiSecret, $endpoint = "https://api.opentok.com") {
    $this->apiKey = $apiKey;
    $this->apiSecret = $apiSecret;
    $this->endpoint = $endpoint . "/v2/partner/" . $apiKey;
  }
  
  protected function request($method, $url, $opts = null) {
    $url = $this->endpoint . $url;

    if(($method == 'PUT' || $method == 'POST') && $opts) {
      $bodyFormat = $opts->contentType();
      $dataString = $opts->dataString();
    }
    
    $authString = "X-TB-PARTNER-AUTH: $this->apiKey:$this->apiSecret";

    if (function_exists("file_get_contents")) {
      $http = array(
        'method' => $method
      );
      $headers = array($authString);
      
      if($method == "POST" || $method == "PUT") {
        $headers[1] = "Content-type: " . $bodyFormat;
        $headers[2] = "Content-Length: " . strlen($dataString);
        $http["content"] = $dataString;
      }
      $http["header"] = $headers;
      $context_source = array ('http' =>$http);
      $context = stream_context_create($context_source);
            
      $res = @file_get_contents( $url ,false, $context);

      $statusarr = explode(" ", $http_response_header[0]);
      $status = $statusarr[1];
      $headers = array();
      
      foreach($http_response_header as $header) {
        if(strpos($header, "HTTP/") !== 0) {
          $split = strpos($header, ":");
          $key = strtolower(substr($header, 0, $split));
          $val = trim(substr($header, $split + 1));
          $headers[$key] = $val;
        }
      }
      
      $response = (object)array(
        "status" => $status
      );
      
      if(strtolower($headers["content-type"]) == "application/json") {
        $response->body = json_decode($res);
      } else {
        $response->body = $res;
      }
      
    } else{
      throw new OpenTokArchivingRequestException("Your PHP installion doesn't support file_get_contents. Please enable it so that you can make API calls.");
    }
        
    return $response;
  }
  
  function get($url, $opts = null) {
    return $this->request("GET", $url, $opts);
  }
  
  function post($url, $opts = null) {
    return $this->request("POST", $url, $opts);
  }
  
  function put($url, $opts = null) {
    return $this->request("PUT", $url, $opts);
  }

  function delete($url, $opts = null) {
    return $this->request("DELETE", $url, $opts);
  }

  function startArchivingSession($session, $name) {
    $startArchive = array(
      "action" => "start",
      "sessionId" => $session,
      "name" => $name
    );
    return $this->post("/archive",  new OpenTokArchivingRequestOptions("json", $startArchive));
  }

  function stopArchivingSession($archiveID) {
    $stopArchive = array(
      "action" => "stop"
    );
    return $this->post("/archive/".$archiveID,  new OpenTokArchivingRequestOptions("json", $stopArchive));
  }
  
  function deleteArchive($archiveID) {
    return $this->delete("/archive/".$archiveID);
  }
  
  function getArchive($archiveID) {
    return $this->get("/archive/".$archiveID);
  }
  
  function getArchives($offset, $count) {
    return $this->get("/archive?offset=" . $offset . "&count=" . $count);
  }
  
}

?>
