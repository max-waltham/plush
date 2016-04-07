# plush - push notifications for mobile apps

**plush** is a scalable Web application for sending push notifications to mobile apps. It consists of two parts:
* An **admin Web interface** for managing users, apps, devices, and analytics.
* An **API** consumed by both mobile devices (for registering themselves) and third-party components for sending push notifications.

## Features

* Support for iOS notifications using Apple's APNS service.
* Support for Android notifications using Google's GCM service.
* Additional mobile platforms can easily be added.
* Manage multiple users & multiple apps from the same plush instance.
* Highly-scalable and fast delivery of notifications, thanks to the underlying technologies: Scala, Play, Akka, and Redis. On modern hardware with a good amount of memory and decent bandwidth, it can handle thousands of apps, millions of mobile devices, tens of thousands of notifications pushed per app per second. More precise benchmarks will be posted here in the future.
* Analytics such as the number of notifications sent by hour/day/etc.

## How does it work?

* An application is created on plush, which gives the necessary auth infos to use the API.
* For sending iOS notifications, the Apple certificate needs to be uploaded (in PKCS#12 format) and its password set.
* For sending Android notifications, the GCM API Key needs to be set.
* Mobile devices register themselves using the API.
* Push notifications can be sent either from the Web admin interface or from the API. They can target some or all (broadcast) devices associated with an app.

## Architecture

The Web admin interface & API are implemented on top of the Play framework, as standard actions.

The delivery of push notifications is handled by Akka actors. For iOS notifications, multiple persistent connections per app will be established with Apple's servers to distribute the load among them.

Android notifications are sent in parallel, a pool of workers are used to send the requests to the GCM servers. If the payload is identical for multiple recipients (e.g. for a broadcast notification), they are sent in batch of 1000 maximum in each GCM request.

Redis is used as the persistence layer, in which users, apps, device tokens, analytics are stored. This implies having enough RAM for storing all data. Redis persists its data to disk, so there is no (or very few) loss in case of failure. Redis persistence strategy can be tuned to meet your needs, and is a compromise between performance and data safety. You can read more about it on the Redis section about [persistence](http://redis.io/topics/persistence).

## How to use?

### Starting the application server

In short:

	$ sbt clean compile stage
	$ target/start

As plush is a standard Play application, you can find detailed informations on how to run it on the [Play website](http://www.playframework.org/documentation/2.0.4/Production).

To run/debug the app in localhost, be sure to properly set redis env variables:
(redis env set to `<project_dir>/conf/application.conf`)

### Using the Web admin interface

Open your favorite browser and head to `http://<server_ip>:9000/users/add` to create a user.

### Using the API

You need to create an app in the admin for all authentication keys/secrets to be generated. API calls for registering devices are authenticated using the app's key & secret. API calls for sending notifications need to use the master secret.
**IMPORTANT: all mobile devices need to have the application key & secret embed, so that they can successfully register with the server. DO NOT include the master secret in your mobile applications, as it is not secure to do so.**

Example uses of API calls using `curl` follow:

#### Registering an iOS device

	$ curl -X PUT -u "<app_key>:<app_secret>" http://<server_ip>:9000/api/device_tokens/<device_token>

#### Registering an Android device

	$ curl -X PUT -u "<app_key>:<app_secret>" http://<server_ip>:9000/api/registrations/<registration_id>

#### Unregistering an iOS device

	$ curl -X DELETE -u "<app_key>:<app_secret>" http://<server_ip>:9000/api/device_tokens/<device_token>

#### Unregistering an Android device

	$ curl -X DELETE -u "<app_key>:<app_secret>" http://<server_ip>:9000/api/registrations/<registration_id>

#### Sending a notification to iOS devices

	$ curl -X POST -u "<app_key>:<app_master_secret>" -H "Content-Type: application/json" --data '{"device_tokens": ["<device_token>", …], "aps": {"alert": "hello world!"}}' http://<server_ip>:9000/api/push

#### Sending a notification to Android devices

	$ curl -X POST -u "<app_key>:<app_master_secret>" -H "Content-Type: application/json" --data '{"registration_ids": ["<registration_id>", …], "data": {"a_key": "hello world!"}}' http://<server_ip>:9000/api/push

#### Sending a broadcast notification to iOS devices

	$ curl -X POST -u "<app_key>:<app_master_secret>" -H "Content-Type: application/json" --data '{"aps": {"alert": "hello world!"}}' http://<server_ip>:9000/api/push/broadcast

#### Sending a broadcast notification to Android devices

	$ curl -X POST -u "<app_key>:<app_master_secret>" -H "Content-Type: application/json" --data '{"data": {"a_key": "hello world!"}}' http://<server_ip>:9000/api/push/broadcast

## Dependencies

* **sbt** is the only required dependency that will bootstrap the whole app and its dependencies.
* **scala-redis** for accessing the Redis key-value store.
* **java-apns** is currently used for connecting to the APNS servers and sending notifications. It is planned to use pure Akka actors and non-blocking I/O for delivering iOS notifications.

## Project status

This project is still under active development. Basic features are already working, but lots of work and testing needs to be done before it is production-ready.

## TODO

Lots:
* Analytics.
* Connecting to the APNS feedback service.
* Pool of GCM workers.
* Better home page with a quick overview of all apps.
* Implement the Akka supervision strategies.
* Factor out and clean up the Redis related code.
* Layout improvements in the Web admin interface.
* Device alias and tags to ease building a recipients list. Send notifications by device(e) and/or alias(es) and/or tag(s).
* Payload size validation.
* Better documentation.
* Large-scale performance simulations.
* APNS certificate validation and expiry management.

## License

Copyright 2012 Philippe Jayet

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
