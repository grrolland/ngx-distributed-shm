[![Build Status](https://travis-ci.org/grrolland/ngx-distributed-shm.svg?branch=master)](https://travis-ci.org/grrolland/ngx-distributed-shm)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.flutech%3Angx-distributed-shm&metric=alert_status)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
[![Technical debt ratio](https://sonarcloud.io/api/project_badges/measure?project=com.flutech%3Angx-distributed-shm&metric=sqale_index)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.flutech%3Angx-distributed-shm&metric=coverage)](https://sonarcloud.io/dashboard/index/com.flutech:ngx-distributed-shm)
# ngx-distributed-shm

This projet is memcached like server based on Hazelcast and Vertx. The goals of the project is to build an easy-to-use distributed memory storage with the nginx shared memory semantic for use with lua nginx plugin.

The semantic of the protocol is the same as the [lua.shared](https://github.com/openresty/lua-nginx-module#ngxshareddict) semantic.

## Status

Production Ready since 06/2018.

## Use cases

This project was succesfully used to store rate limiting counter across a cluster of an Nginx based API Gateway in a french banking company. 

This project was succesfully used to distribute OpenID Connect Replying Party (based on zmartzone/lua-resty-openidc
) web session with the library bungle/lua-resty-session in a french banking company.

## Principle Schema

    -------------------    -------------------    -------------------
    |                 |    |                 |    |                 |
    | Nginx/Openresty |    | Nginx/Openresty |    | Nginx/Openresty |
    |        1        |    |        2        |    |        3        |
    -------------------    -------------------    -------------------
             | (127.0.0.1:4321)     | (127.0.0.1:4321)     | (127.0.0.1:4321)
    -------------------    -------------------    -------------------
    |                 |    |                 |    |                 |
    |       DSHM      |    |       DSHM      |    |       DSHM      |
    |        1        |    |        2        |    |        3        |
    -------------------    -------------------    -------------------
             |                      |                       |
             |                Data Replication              |
             |______________________|_______________________|

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
  - DELETE : delete a key from the sorage
  - INCR  : increment the value for a key
 
 In a clustered deployement (2 or more instances), a client need to connect to only one instance to see all the storage. The goal is to provide a near storage associated with an nginx instance.
 
 ## Startup
 
 This command startup the storage on default port (4321), with default replication options (UDP multicast) :
 
 ```
 java -jar ngx-distributed-shm.jar
 ```
 
 Or : 
 ``` 
 java -cp ngx-distributed-shm.jar com.flutech.hcshm.Main
 ```
 
 To startup with a configuration directory ./conf (with hazelcast.xml and logback.xml) use :
 
 ``` 
 java -cp ngx-distributed-shm.jar:./conf com.flutech.hcshm.Main
 ```
 
 The dist/bin directroy contains startup and shutdown scripts.
 
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

### Configure the hazelcast IMDG map for replication 

The hazelcast IMDG is configured with a configuration file which must be present in the classpath. The file must be named hazelcast.xml.

This is an example of this file :

```
<hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.9.xsd"
           xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <group>
        <name>ngx-dshm</name>
        <password>FIXME</password>
    </group>
    <network>
        <port auto-increment="false">5701</port>
        <join>
            <multicast enabled="false" />
            <tcp-ip enabled="true">
                <interface>10.0.x.y</interface>
                <member-list>
                    <member>10.0.x.y:5701</member>
                    <member>10.0.x.z:5701</member>
                </member-list>
            </tcp-ip>
            <aws enabled="false" />
        </join>
    </network>
    <map name="default">
        <in-memory-format>BINARY</in-memory-format>
        <backup-count>1</backup-count>
        <async-backup-count>0</async-backup-count>
        <time-to-live-seconds>0</time-to-live-seconds>
        <max-idle-seconds>0</max-idle-seconds>
        <eviction-policy>NONE</eviction-policy>
        <max-size policy="PER_NODE">0</max-size>
        <eviction-percentage>25</eviction-percentage>
        <min-eviction-check-millis>100</min-eviction-check-millis>
        <merge-policy>com.hazelcast.map.merge.PutIfAbsentMapMergePolicy</merge-policy>
        <cache-deserialized-values>INDEX-ONLY</cache-deserialized-values>
    </map>
</hazelcast>
```

The reference documentation for this configuration is here : https://docs.hazelcast.org/docs/3.12.1/manual/html-single/index.html#tcp-ip-element

This configuration works well for a two menber cluster of the distributed shared memory.

## Logging

The dist/conf directory contains an exemple logback.xml which control logging. The example file is the following : 

```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <contextName>ngx-dshm</contextName>
    <jmxConfigurator/>

    <appender name="FILE-GLOBAL" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${ngx-distributed-shm.log_dir}/ngx-dshm.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${ngx-distributed-shm.log_dir}/ngx-dshm-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS,Europe/Paris} [%thread] %-5level %logger{36} - %msg %xEx{20}%n</pattern>
        </encoder>
    </appender>

    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE-GLOBAL" />
    </appender>


    <root level="info">
        <appender-ref ref="ASYNC"/>
    </root>

</configuration>
```

Starting the dshm with the ***-Dngx-distributed-shm.log_dir=log_dir*** permit to choose the logging directory.

  
## Protocol

The protocol is a test protocol inspired by the memcached protocol.

There is two part in the protocol request : the command line part and the optional data part.

### Command line part

The command line part has the following format :

```
COMMAND ARG1 ARG2 ARGN\r\n
```

The ***ARG1*** is alway the ***key***

### Anatomy of a key : region cache support

The distributed shared memory support region partionning. The format of the key control the region where will be located the value.

This key ***key1*** will be located in the region ***region1*** : 

```
region1::key1
```

This key ***key1*** will be located in the default region  : 

```
key1
```

This permit to control the hazelast map where will be stored the key/value.

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
When the key does not exist the command does nothing.

```
TOUCH key 10\r\n
```

***DELETE \<key\>***

**with data:** *no*

Delete the key \<key\> from the storage. This operation is atomic.
When the key does not exist the command does nothing.
 
```
DELETE key\r\n
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

***FLUSHALL [region]***

**with data:** *no*

Remove all the key from the region. The region is optionnal. Without region parameter, the default region is flush.
 
This operation is atomic.

```
FLUSHALL\r\n
```

Or :

```
FLUSHALL region1\r\n
```

***QUIT***

**with data:** *no*

Close the connection with the server.

```
QUIT\r\n
```

## LUA libraries support

The lua libraries (lua/dshm.lua) is used to pilot the shared memory. The librarie should be installed in the resty disrectory of the openresty distribution.

This is an exemple to use it :

```
local dshm = require "resty.dshm"

local store = dshm:new()

store:connect("127.0.0.1", 4321)

store:set("key", 10)
local value = store:get("key")
store:incr("key")
store:delete("key")
```

### Resty Session support

This module could be used to activate session replication with the excellent lua library Resty Session (https://github.com/bungle/lua-resty-session)

To use it, copy the lua extention in your resty/session/storage directory and use this type of configuration in your nginx.conf : 

```
set $session_storage           dshm;
set $session_serializer        json;
set $session_encoder           base64;
set $session_cookie_persistent off;
set $session_cookie_renew      600;
set $session_cookie_samesite   Lax;
set $session_secret a_secret_string;
```

The session_storage parameter control the storage module to be used.



