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

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import rx.Scheduler;
import rx.functions.Action0;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

final class DefaultFlusher implements Flusher {

    private final Scheduler scheduler;
    private final long flushDelayNanos;
    private final Deque<Flushable> flushables = new ConcurrentLinkedDeque<>();
    private final FlushAction flushAction = new FlushAction();
    private final SerialSubscription subscription = new SerialSubscription();

    static Flusher create(Scheduler scheduler, long flushDelay, TimeUnit flushDelayUnit) {
        return new DefaultFlusher(scheduler, flushDelay, flushDelayUnit);
    }

    private DefaultFlusher(Scheduler scheduler, long flushDelay, TimeUnit flushDelayUnit) {
        this.scheduler = scheduler;
        this.flushDelayNanos = flushDelayUnit.toNanos(flushDelay);
        resetSubscription();
    }

    @Override
    public void schedule(Flushable flushable) {
        flushables.add(flushable);
        scheduleFlush();
    }

    private void scheduleFlush() {
        if (subscription.get() == Subscriptions.unsubscribed()) {
            subscription.set(scheduler.createWorker().schedule(
                    flushAction, flushDelayNanos, TimeUnit.NANOSECONDS));
        }
    }

    private void resetSubscription() {
        subscription.set(Subscriptions.unsubscribed());
    }

    private class FlushAction implements Action0 {

        @Override
        public void call() {
            while (!flushables.isEmpty()) {
                Flushable flushable = flushables.remove();
                flushable.flush();
                resetSubscription();
            }
        }
    }
}
