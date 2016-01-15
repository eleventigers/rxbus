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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.obviousengine.rxbus.Bus;
import com.obviousengine.rxbus.RxBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@SuppressWarnings("unchecked")
public final class DefaultBusStationTest {

    private final EventA eventA = new EventA();
    private final Bus bus = RxBus.create();
    private final Sink<EventA> sinkA1 = mock(Sink.class);
    private final Sink<EventA> sinkA2 = mock(Sink.class);
    private final Flusher flusher = mock(Flusher.class);
    private final TestScheduler scheduler = Schedulers.test();
    private final BusStation busStation = DefaultBusStation.create(bus, scheduler, flusher);

    @Before
    public void setUp() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Sink sink = (Sink) invocation.getArguments()[0];
                sink.flush();
                return null;
            }
        }).when(flusher).schedule(any(Sink.class));
    }

    @Test
    public void singleSinkSingleEvent() {
        busStation.register(EventA.class, sinkA1);
        busStation.publish(eventA);
        scheduler.triggerActions();

        verify(sinkA1).receive(eventA);
        verify(sinkA1).flush();

        verifyNoMoreInteractions(sinkA1);
    }

    @Test
    public void multipleSinkSingleEvent() {
        busStation.register(EventA.class, sinkA1);
        busStation.register(EventA.class, sinkA2);
        busStation.publish(eventA);
        scheduler.triggerActions();

        verify(sinkA1).receive(eventA);
        verify(sinkA1).flush();
        verify(sinkA2).receive(eventA);
        verify(sinkA2).flush();

        verifyNoMoreInteractions(sinkA1, sinkA2);

    }

    @Test
    public void singleSinkRegisterUnregister() {
        busStation.register(EventA.class, sinkA1);
        busStation.publish(eventA);
        scheduler.triggerActions();
        busStation.unregister(sinkA1);

        verify(sinkA1).receive(eventA);
        verify(sinkA1).flush();

        busStation.publish(eventA);
        scheduler.triggerActions();

        verifyNoMoreInteractions(sinkA1);
    }

}
