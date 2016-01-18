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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.obviousengine.rxbus.Bus;
import com.obviousengine.rxbus.Queue;
import com.obviousengine.rxbus.RxBus;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@SuppressWarnings("unchecked")
public final class DefaultDispatcherTest {

    private final EventA eventA = new EventA();
    private final Queue<EventA> queue = Queue.of(EventA.class).build();
    private final Bus bus = RxBus.create();
    private final Station<EventA> stationA1 = mock(Station.class);
    private final Station<EventA> stationA2 = mock(Station.class);
    private final Flusher flusher = mock(Flusher.class);
    private final ErrorListener errorListener = mock(ErrorListener.class);
    private final TestScheduler scheduler = Schedulers.test();
    private final Dispatcher dispatcher = DefaultDispatcher.create(
            bus, scheduler, flusher, errorListener);

    @Before
    public void setUp() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Station station = (Station) invocation.getArguments()[0];
                station.flush();
                return null;
            }
        }).when(flusher).schedule(any(Station.class));
    }

    @Test
    public void singleStationSingleEvent() {
        dispatcher.register(queue, stationA1);
        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();

        verify(stationA1).receive(eventA);
        verify(stationA1).flush();

        verifyNoMoreInteractions(stationA1);
    }

    @Test
    public void multipleStationSingleEvent() {
        dispatcher.register(queue, stationA1);
        dispatcher.register(queue, stationA2);
        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();

        verify(stationA1).receive(eventA);
        verify(stationA1).flush();
        verify(stationA2).receive(eventA);
        verify(stationA2).flush();

        verifyNoMoreInteractions(stationA1, stationA2);

    }

    @Test
    public void singleStationRegisterUnregister() {
        dispatcher.register(queue, stationA1);
        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();
        dispatcher.unregister(stationA1);

        verify(stationA1).receive(eventA);
        verify(stationA1).flush();

        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();

        verifyNoMoreInteractions(stationA1);
    }

    @Test
    public void singleStationFlushWhenReceiveThrows() {
        final Throwable error = new RuntimeException();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw error;
            }
        }).when(stationA1).receive(eventA);

        dispatcher.register(queue, stationA1);
        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();

        verify(stationA1).receive(eventA);
        verify(stationA1).flush();
        verify(errorListener).onError(error);

        verifyNoMoreInteractions(stationA1);
        verifyNoMoreInteractions(errorListener);
    }

    @Test
    public void singleStationReceiveAfterReceiveThrows() {
        final Throwable error = new RuntimeException();
        final AtomicBoolean thrown = new AtomicBoolean();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (thrown.compareAndSet(false, true)) {
                    throw error;
                } else {
                    return null;
                }
            }
        }).when(stationA1).receive(eventA);

        dispatcher.register(queue, stationA1);
        dispatcher.publish(queue, eventA);
        dispatcher.publish(queue, eventA);
        scheduler.triggerActions();

        verify(stationA1, times(2)).receive(eventA);
        verify(stationA1, times(2)).flush();
        verify(errorListener).onError(error);

        verifyNoMoreInteractions(stationA1);
        verifyNoMoreInteractions(errorListener);
    }

}
