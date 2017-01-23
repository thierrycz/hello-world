<?php
    $curl = curl_init();
	
    //curl_setopt_array($curl, array(
    //    CURLOPT_URL => $_GET['get']
    //));
	$a = str_replace(' ','+',$_GET['get'] );
	//error_log($a);
	//print_r($a);
	curl_setopt($curl, CURLOPT_URL,$a);
	//curl_setopt($curl, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
	//curl_setopt($curl, CURLOPT_USERPWD, "admin:admin" );	
	
    $result=curl_exec($curl);
	curl_close($curl);
	//print_r( $result);
?>
