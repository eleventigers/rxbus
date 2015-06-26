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

import static org.junit.Assert.assertNotNull;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import rx.Observer;
import rx.subjects.Subject;

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
        bus.subscribe(events, observer);
        Event event = Event.create();
        bus.publish(events, event);
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
}
