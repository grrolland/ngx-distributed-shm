[![Build Status](https://travis-ci.org/grrolland/ngx-distributed-shm.svg?branch=master)](https://travis-ci.org/grrolland/ngx-distributed-shm)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.flutech:ngx-distributed-shm)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
[![Technical debt ratio](https://sonarcloud.io/api/project_badges/measure?project=com.flutech%3Angx-distributed-shm&metric=sqale_index)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.flutech%3Angx-distributed-shm&metric=coverage)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
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
 
 ***-Dngx-distributed-shm.port=port***
 **default :** *4321* 
 
 Startup the storage and bind the protocol port on <port>. 
 
 This command startup the storage on the port 40000 and the 127.0.0.1 address : 
 
```
  java -Dngx-distributed-shm.port=40000 -jar ngx-distributed-shm.jar
```
 
 ***-Dngx-distributed-shm.bind_address=address***
**default :** *127.0.0.1*

Startup the storage and bind the protocol on address <address>.
 
 This command startup the storage on the 192.168.0.1 address : 
 
```
  java -Dngx-distributed-shm.bind_address=192.168.0.1 -jar ngx-distributed-shm.jar
```
  
## Protocol

The protocol is a test protocol inspired by the memcached protocol.

There is two part in the protocol request : the command line part and the optional data part.

### Command line part

The command line part has the following format :

```
COMMAND ARG1 ARG2 ARGN\r\n
```

### Data part

The data part is a bytes stream wich the length is specified in the command part.

For exemple the second argument of the SET command set the length of the data to send : 

```
SET key 0 4\r\n
1234
``` 

This command set the key "key" at the value "1234"

### Response

The response protocol is fairly simple. When the the command is executed the response have the folowing format : 

```
\r\nRESPONSE response_arg\r\n
```

The following terms are used in the response :

***DONE*** 

The command is successfully executed. There is no argument.

***ERROR*** 

There is an error in the command. the argument is the error message.

### Error Messages
***malformed_request***

The command request is malformed. There is an error in the request.
 
***not_found***

The key involved in the command is not found. 

### Commands

***GET \<key\>***

**with data:** *no* 

Get the value of the key in the storage. This operation is atomic.
 
```
GET key\r\n
```

***SET \<key\> \<expiration\> \<length\>***

**with data:** *yes*

Set a value of length \<length\> for the key \<key\> in the storage with the expiration time in second \<expiration\>. This operation is atomic.

When \<expiration\> is 0, the key don't expire.
 
```
SET key 10 4\r\n
1234
```

***TOUCH \<key\> \<expiration\>***

**with data:** *no*

Set the expiration time in second \<expiration\> for the key \<key\>. This operation is atomic.

When \<expiration\> is 0, the key don't expire.
 
```
TOUCH key 10\r\n
```

***INCR \<key\> \<value\> \<init\>***

**with data:** *no*

Increment the value of the key \<key\> with \<value\> if the key exists and represent an integer.

If the value is not an integer, this operation has no effect.

If the key don't exist or is expired, this operation create the key and init the value to  \<value\>+\<init\>.
 
This operation is atomic.

```
INCR key -1 0\r\n
```
