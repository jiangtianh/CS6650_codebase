##### This project is a distributed microservice system for recording and querying skier lift data, deployed on AWS with load-balanced EC2 instances running multithreaded Java servlets. It uses Redis for caching, RabbitMQ for asynchronous message processing, and connection pools for efficiency, achieving high throughput through performance tuning.

Get architecture
![Get architecture](/doc/GET.jpg)

Post architecture 
![Post architecture](/doc/POST.jpg)

- Designed and implemented a distributed system to record and query skier lift data, deployed on AWS with a load balancer and four EC2 instances running multi-threaded Java servlets on Tomcat. 
- Utilized Redis for fast caching and data retrieval, ensuring low-latency responses for requests.
POST requests followed an eventual consistency model, publishing messages to RabbitMQ for asynchronous processing. - Multiple multi-threaded Java consumers processed messages from RabbitMQ with batch acknowledgement, enhancing reliability and reducing network RTT.
- Leveraged Jedis Pool and RabbitMQ Pool for efficient connection management across nodes.
- Fine-tuned the system performance by optimizing the number of nodes and threads for both servers and consumers based on throughput testing, balancing performance with cost-effectiveness.


A multi threaded test client was implemented to test the POST throughput of the system. The client will randomly generate inputs, and make POST requests to the service. The client has a circuit breaker pattern, and below are the results and rabbitMQ monitoring: 
 - 3800 open, 3300 close
    - ![rmq](/doc/3800Open,3300Close,RMQ.png)
    - ![throughput](/doc/3800Open,3300Close,throughput.png)

 - 4300 open, 3800 close
    - ![rmq](/doc/4300Open,3800Close,RMQ.png)
    - ![throughput](/doc/4300Open,3800Close,throughput.png)

 - 4800 open, 4300 close
    - ![rmq](/doc/4800Open,4300Close,RMQ.png)
    - ![throughput](/doc/4800Open,4300Close,throughput.png)


Below is the GET request throughput tested using Jmeter:
- ![getThroughput](/doc/GETthroughput.png)