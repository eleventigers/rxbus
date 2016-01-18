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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

public final class DefaultFlusherTest {

    private final Flushable flushable1 = mock(Flushable.class);
    private final Flushable flushable2 = mock(Flushable.class);
    private final TestScheduler scheduler = Schedulers.test();
    private final Flusher flusher = DefaultFlusher.create(scheduler, 1, TimeUnit.SECONDS);

    @Test
    public void singleSchedule() {
        flusher.schedule(flushable1);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        scheduler.triggerActions();

        verify(flushable1).flush();
    }

    @Test
    public void multipleSchedule() {
        flusher.schedule(flushable1);
        flusher.schedule(flushable2);

        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);

        verifyZeroInteractions(flushable1, flushable2);

        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        scheduler.triggerActions();

        verify(flushable1).flush();
        verify(flushable2).flush();

        verifyNoMoreInteractions(flushable1, flushable2);
    }

}