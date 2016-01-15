/*
 * Copyright (c) 2016 Obvious Engineering.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obviousengine.rxbus.station;

import com.obviousengine.rxbus.Bus;
import com.obviousengine.rxbus.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;

final class DefaultBusStation implements BusStation {

    private final ConcurrentHashMap<Sink<?>, Subscription> subscriptions =
            new ConcurrentHashMap<>(8);
    private final Map<Class<?>, Queue<?>> queues = new ConcurrentHashMap<>(4);

    private final Bus bus;
    private final Scheduler busScheduler;
    private final Flusher flusher;

    public static BusStation create(Bus mainBus, Scheduler scheduler, Flusher flusher) {
        return new DefaultBusStation(mainBus, scheduler, flusher);
    }

    @Override
    public <T> void publish(T event) {
        bus.publish(queue(event.getClass()), event);
    }

    @Override
    public <T> void register(Class<T> eventClass, Sink<T> sink) {
        synchronized (subscriptions) {
            if (!subscriptions.containsKey(sink)) {
                Queue<T> queue = queue(eventClass);
                subscriptions.put(sink, bus.subscribe(
                        queue, new SinkSubscriber<>(sink, flusher), busScheduler));
            }
        }
    }

    @Override
    public <T> void unregister(Sink<T> sink) {
        Subscription subscription = subscriptions.remove(sink);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Queue<T> queue(Class<?> eventClass) {
        Queue<T> queue = (Queue<T>) queues.get(eventClass);

        if (queue == null) {
            synchronized (queues) {
                queue = (Queue<T>) queues.get(eventClass);
                if (queue == null) {
                    queue = Queue.of((Class<T>) eventClass).build();
                    queues.put(eventClass, queue);
                }
            }
        }

        return queue;
    }

    private static class SinkSubscriber<T> extends Subscriber<T> {

        private final Sink<T> sink;
        private final Flusher flusher;

        SinkSubscriber(Sink<T> sink, Flusher flusher) {
            this.sink = sink;
            this.flusher = flusher;
        }

        @Override
        public void onCompleted() {
            // no-op
        }

        @Override
        public void onError(Throwable e) {
            // no-op
        }

        @Override
        public void onNext(T t) {
            try {
                sink.receive(t);
            } catch (Throwable e) {
                //TODO(eleventigers): handle error
                throw e;
            }

            flusher.schedule(sink);
        }
    }

    private DefaultBusStation(Bus bus, Scheduler busScheduler, Flusher flusher) {
        this.bus = bus;
        this.busScheduler = busScheduler;
        this.flusher = flusher;
    }
}
