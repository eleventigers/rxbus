/*
 * Copyright (c) 2015 Obvious Engineering.
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

package com.obviousengine.rxbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.Observer;
import rx.Subscription;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

public class RxBusTest {

    private final Queue<Event> events = Queue.of(Event.class).name("TestQueue").build();

    @Test
    public void queueAsSubject() throws Exception {
        Bus bus = RxBus.create();
        Subject<Event, Event> subject = bus.queue(events);
        assertNotNull(subject);
    }

    @Test
    public void publishEvent() throws Exception {
        Bus bus = RxBus.create();
        Observer<Event> observer = spy(new PrintingObserver<Event>());
        Subscription subscription = bus.subscribe(events, observer);
        Event event = Event.create();
        bus.publish(events, event);
        subscription.unsubscribe();
        verify(observer).onNext(event);
        verify(observer, never()).onCompleted();
    }

    @Test
    public void loggerLogs() throws Exception {
        RxBus.Logger logger = spy(new RxBus.Logger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        Bus bus = RxBus.create(logger);
        bus.publish(events, Event.create());
        verify(logger).log(anyString());
    }

    @Test
    public void oneQueueMultipleObservers() throws Exception {
        Bus bus = RxBus.create();
        CompositeSubscription subscriptions = new CompositeSubscription();
        List<Observer<Event>> observers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Observer<Event> observer = spy(new NoopObserver<Event>());
            observers.add(observer);
            subscriptions.add(bus.subscribe(events, observer));
        }
        Event event = Event.create();
        bus.publish(events, event);
        subscriptions.unsubscribe();
        for (Observer<Event> observer : observers) {
            verify(observer).onNext(event);
        }
    }

    @Test
    public void allEventsGoThrough() throws Exception {
        final int numEvents = 1000;
        Bus bus = RxBus.create();
        CountingObserver observer = new CountingObserver();
        Subscription subscription = bus.subscribe(events, observer);
        for (int i = 0; i < numEvents; i++) {
            bus.publish(events, Event.create());
        }
        subscription.unsubscribe();
        assertEquals(numEvents, observer.getCount());
    }

    static class PrintingObserver<V> implements Observer<V> {
        @Override
        public void onCompleted() {
            System.out.println("onComplete() called");
        }

        @Override
        public void onError(Throwable e) {
            System.out.println("onError() called with " + e);
        }

        @Override
        public void onNext(V v) {
            System.out.println("onNext() called with " + v);
        }
    }

    static class CountingObserver implements Observer<Event> {

        private final AtomicInteger count = new AtomicInteger();

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            throw new RuntimeException(e);
        }

        @Override
        public void onNext(Event event) {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }
    }

    static class NoopObserver<V> implements Observer<V> {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(V v) {
        }
    }
}
