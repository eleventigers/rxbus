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

package com.obviousengine.rxbus.dispatcher;

import com.obviousengine.rxbus.Bus;
import com.obviousengine.rxbus.Queue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;

final class DefaultDispatcher implements Dispatcher {

    private final Map<Station<?>, Subscription> subscriptions = new ConcurrentHashMap<>(8);

    private final Bus bus;
    private final Scheduler busScheduler;
    private final Flusher flusher;
    private final ErrorListener errorListener;

    public static Dispatcher create(Bus bus, Scheduler scheduler, Flusher flusher,
                                    ErrorListener errorListener) {
        return new DefaultDispatcher(bus, scheduler, flusher, errorListener);
    }

    @Override
    public <T> void publish(Queue<T> queue, T event) {
        bus.publish(queue, event);
    }

    @Override
    public <T> void register(Queue<T> queue, Station<T> station) {
        synchronized (subscriptions) {
            if (!subscriptions.containsKey(station)) {
                subscriptions.put(station, bus.subscribe(
                        queue, new StationSubscriber<>(station, flusher, errorListener), busScheduler));
            }
        }
    }

    @Override
    public <T> void unregister(Station<T> station) {
        Subscription subscription = subscriptions.remove(station);
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private static class StationSubscriber<T> extends Subscriber<T> {

        private final Station<T> station;
        private final Flusher flusher;
        private final ErrorListener errorListener;

        StationSubscriber(Station<T> station, Flusher flusher, ErrorListener errorListener) {
            this.station = station;
            this.flusher = flusher;
            this.errorListener = errorListener;
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
                station.receive(t);
            } catch (Throwable throwable) {
                // We want to continue using this subscriber even if the receiver throws
                // so we just pass the error to a dedicated handler
                errorListener.onError(throwable);
            } finally {
                flusher.schedule(station);
            }
        }

        @Override
        public String toString() {
            return "StationSubscriber{" +
                    "station=" + station +
                    '}';
        }
    }

    private DefaultDispatcher(Bus bus, Scheduler busScheduler, Flusher flusher,
                              ErrorListener errorListener) {
        this.bus = bus;
        this.busScheduler = busScheduler;
        this.flusher = flusher;
        this.errorListener = errorListener;
    }
}
