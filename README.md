RxBus
=====
Event bus running on type safe RxJava queues

Download
--------
Gradle:
```groovy
compile 'net.jokubasdargis.rxbus:rxbus:2.0.0'

// Optional
compile 'net.jokubasdargis.rxbus:rxbus-android:2.0.0'
compile 'net.jokubasdargis.rxbus:rxbus-dispatcher:2.0.0'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

Usage
-----

**Basic use - event delivery transport tightly coupled by an event type**

* Create a default bus

  ```java
  Bus bus = RxBus.create();
  ```

  or if you are on Android:

  ```java
  Bus bus = RxAndroidBus.create();
  ```

* Prepare a Queue for an event type. You might want to maintain a collection of static queues as:

  ```java
  public final class Queues {
    public static final Queue<Event> TRACKING  = Queue.of(TrackingEvent.class).build();
  }
  ```

* Use a queue when subscribing

  ```java
  Subscription subscription = bus.subscribe(Queues.TRACKING, new Observer<Event> {
    ...
    @Override
    public void onNext(TrackingEvent event) {
      // do something with the event
    }
  })
  ```

* and publishing:

  ```java
  bus.publish(Queues.TRACKING, new TrackingEvent())
  ```


Origin
------
Adapted from Soundcloud's reactive bus appraoch as seen in [mttkay's](https://github.com/mttkay) [Reactive Soundcloud](https://speakerdeck.com/mttkay/reactive-soundcloud-tackling-complexity-in-large-applications) slides:

![bus](/assets/eventbus_1.jpg)

![bus](/assets/eventbus_2.jpg)

License
-------

    Copyright 2016 Jokubas Dargis

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
