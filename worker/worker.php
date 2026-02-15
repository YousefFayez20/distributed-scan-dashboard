<?php
require 'vendor/autoload.php';
use GuzzleHttp\Client;

$client = new Client(
    [
        'base_uri' => 'http://localhost:8080'
    ]
);

function scan($ipLong, $ports) {

    $results = [];

    foreach ($ports as $port) {

        usleep(2000);

        $results[] = [
            'ip' => long2ip($ipLong),
            'port' => $port,
            'status' => (rand(0, 1) ? 'OPEN' : 'CLOSED')
        ];
    }

    return $results;   
}



$workerName = $argv[1] ?? 'Worker-1';
while (true) {
    $response = $client->post('/api/worker/heartbeat',
        [
            'json' => [
                'workerName' => $workerName
            ]
        ]
    );
    if($response->getStatusCode() == 200){
      $body = $response->getBody()->getContents();
      $data = json_decode($body, true);
      //check if there is an assignment
      if(!isset($data['assignmentId']) || !is_array($data)){
        sleep(30);
        continue;
      }


      
      //convert ip to long
      $startIP = ip2long($data['startIp']);
      $endIP   = ip2long($data['endIp']);
      $ports   = $data['ports'];
      $interval = $data['interval'];
      $assignmentId = $data['assignmentId'];


       $currentIp = $startIP;


      while($currentIp <= $endIP){
        $buffer = [];
           $startTime = time();
           while(time() - $startTime < $interval && $currentIp <= $endIP){
           
           $buffer = array_merge($buffer, scan($currentIp, $ports));
            $currentIp++;
         }
         if($currentIp > $endIP){
            $isFinished = true;
         }else{
            $isFinished = false;
         }
         $payload = [
            'workerName' => $workerName,
            'assignmentId' => $assignmentId,
            'isFinished' => $isFinished,
            'data'=> $buffer
         ];
       //post result to server
       $response2 = $client->post('/api/worker/results', [
        'json' => $payload
       ]);
       if ($response2->getStatusCode() !== 200) {
        throw new Exception("Dashboard failed batch");
    }          
      }
    }else{
       sleep(30); 
 }


}

?>