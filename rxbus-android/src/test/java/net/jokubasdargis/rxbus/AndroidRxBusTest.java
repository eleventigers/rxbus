/*
 * Copyright (c) 2016 Jokubas Dargis.
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

package net.jokubasdargis.rxbus;

import com.jakewharton.rxrelay.Relay;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subscriptions.CompositeSubscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public final class AndroidRxBusTest {

    private final static RxBus.Logger SYSTEM_LOGGER = new RxBus.Logger() {
        @Override
        public void log(String message) {
            System.out.println(message);
        }
    };

    private final Queue<Event> events = Queue.of(Event.class).name("TestQueue").build();
    private final RxBus.QueueCache queueCache = new RxBus.DefaultQueueCache();
    private final TestScheduler testScheduler = Schedulers.test();

    @Test
    public void create() {
        Bus bus = AndroidRxBus.create();
        assertNotNull(bus);
    }

    @Test
    public void createWithLogger() {
        Bus bus = AndroidRxBus.create(SYSTEM_LOGGER);
        assertNotNull(bus);
    }

    @Test
    public void queueAsRelay() {
        Bus bus = AndroidRxBus.create(queueCache, SYSTEM_LOGGER);
        Relay<Event, Event> subject = bus.queue(events);
        assertNotNull(subject);
    }

    @Test
    public void publishEvent() {
        Bus bus = AndroidRxBus.create(queueCache, SYSTEM_LOGGER);
        Observer<Event> observer = spy(new PrintingObserver<Event>());
        Subscription subscription = bus.subscribe(events, observer, testScheduler);
        Event event = Event.create();
        bus.publish(events, event);
        testScheduler.triggerActions();
        subscription.unsubscribe();
        verify(observer).onNext(event);
        verify(observer, never()).onCompleted();
    }

    @Test
    public void oneQueueMultipleObservers() {
        Bus bus = AndroidRxBus.create(queueCache, SYSTEM_LOGGER);
        CompositeSubscription subscriptions = new CompositeSubscription();
        List<Observer<Event>> observers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Observer<Event> observer = spy(new NoopObserver<Event>());
            observers.add(observer);
            subscriptions.add(bus.subscribe(events, observer, testScheduler));
        }
        Event event = Event.create();
        bus.publish(events, event);
        testScheduler.triggerActions();
        subscriptions.unsubscribe();
        for (Observer<Event> observer : observers) {
            verify(observer).onNext(event);
        }
    }

    @Test
    public void allEventsGoThrough() {
        final int numEvents = 100;
        Bus bus = AndroidRxBus.create(queueCache, SYSTEM_LOGGER);
        CountingObserver observer = new CountingObserver();
        Subscription subscription = bus.queue(events)
                .onBackpressureBuffer()
                .observeOn(testScheduler)
                .subscribe(observer);
        for (int i = 0; i < numEvents; i++) {
            bus.publish(events, Event.create());
        }
        testScheduler.triggerActions();
        subscription.unsubscribe();
        assertEquals(numEvents, observer.getCount());
    }

    private static class PrintingObserver<V> implements Observer<V> {

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

    private static class CountingObserver implements Observer<Event> {

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

    private static class NoopObserver<V> implements Observer<V> {

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
