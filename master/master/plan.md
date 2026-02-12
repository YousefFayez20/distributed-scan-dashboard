1. determine the CIDR, PORTS, and interval, to be hardcoded in our app
CIDR -> (startIP,endIP), number of IPs = endIP-startIP + 1
chunk size = (totalIPs / active workers) the result to be ceiled
256 IPs - 3 active worker -> chunk size = 256/3 = 86
for each worker (i -> index):
    startWorkerIP = startIP + (i*chunk size)
    EndWorkerIP = min(startWorkerIP + i*chunk size,endIP )
2. construct workers table, which will have 
the Integer ID of the worker, name of the worker (sent by the worker), last seen timestamp
and the other database tables
3. make an IP utility functions
4. after the worker send their heartbeats
5. the UI will choose the workers to start scanning from the list of available workers
6. the UI will send the list of chosen workers
7. the dashboard will construct the assignment table
8. whenever a worker sends the heartbeat again he will receive the assignment