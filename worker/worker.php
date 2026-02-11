<?php
require 'vendor/autoload.php';
use GuzzleHttp\Client;

$client = new Client();
while (true) {
    $reponse = $client->get('http://localhost:8080/api/worker/heartbeat');
    if($response->getStatusCode() == 200){
      $body = $response->getBody()->getContents();
      $decode = json_decode($body, true);

      $startIp = $data['startIp'];
      $endIp   = $data['endIp'];
      //convert ip to long
      $startIP = ip2long($startIp);
      $endIP   = ip2long($endIp);
      $ports   = $data['ports'];
      $interval = $data['interval'];
      while($startIP <= $endIP){
        $tempArr = [];
           $startTime = time();
           while(time() - $startTime < $interval){
            $buffer[] = scan($startIP, $ports);
            $startIP++;
         }
       //post result to server
       $reponse = $client->post('http://localhost:8080/api/worker/results', [
        'json' => $buffer
       ]);
       if ($response->getStatusCode() !== 200) {
        throw new Exception("Dashboard failed batch");
    }          
      }
    }else{
       sleep(30); 
 }


}

?>