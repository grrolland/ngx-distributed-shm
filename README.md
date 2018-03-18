[![Build Status](https://travis-ci.org/grrolland/lua-ngx-distributed-shm.svg?branch=master)](https://travis-ci.org/grrolland/lua-ngx-distributed-shm)

# ngx-distributed-shm

This projet is memcached like server based on Hazelcast and Vertx. The goals of the project is to build an easy-to-use distributed memory storage with the nginx shared memory semantic for use with lua nginx plugin.

The semantic of the protocol is the same as the [lua.shared](https://github.com/openresty/lua-nginx-module#ngxshareddict) semantic.

## Dependencies

**ngx-distributed-shm** depends on followings libraries :

 - [Hazelcast IMDG](https://hazelcast.org/) for implementing the distributed storage
 - [Vertx](http://vertx.io/) for implementing the communication protocol
 
 The dependencies above are automatically included in the distribution jar with maven shade plugin.
 
 ## Installation
 
 You need a JVM (Java 8 at least) to run the distributed storage. 
 
 Simply get the distribution jar and copy it on your filesystem.
 
 ## How it works
 
 When the storage startup, it creates an hazelcast instance and a vertx connector to communicate with it. When you start a second instance, it joins the first with the hazelcast protocol.
 
 The protocol expose commands to interact with the distributed storage : 
  - SET : set a value in the storage
  - GET : get a value from the storage
  - TOUCH : update the ttl of a key
  - INCR  : increment the value for a key
 
 In a clustered deployement (2 or more instances), a client need to connect to only one instance to see all the storage. The goal is to provide a near storage associated with an nginx instance.
 
 ## Startup
 
 This command startup the storage on default port (4321), with default replication options (UDP multicast) :
 
 ```
 java -jar ngx-distributed-shm.jar
 ```
 
 ## Startup Options
 
 ***-Dngx-distributed-shm.port=<port>***
 
 Startup the storage and bind the protocol port on <port>. Notice that the storage bind only on the localhost address.
 
 This command startup the storage on the port 40000 and the 127.0.0.1 address : 
 
 ```
  java -Dngx-distributed-shm.port=40000 -jar ngx-distributed-shm.jar
  ```
  

