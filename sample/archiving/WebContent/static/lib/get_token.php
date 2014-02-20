<?php

include 'config.php';
include 'lib/Opentok-PHP-SDK/OpenTokSDK.php';

$apiObj = new OpenTokSDK($config_api_key, $config_api_secret);
$token = $apiObj->generateToken($config_session_id);

?>
