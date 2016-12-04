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

import java.util.concurrent.TimeUnit;
import rx.Scheduler;
import rx.schedulers.Schedulers;

@SuppressWarnings("WeakerAccess")
public final class RxBusDispatcher {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private static final long DEFAULT_FLUSH_DELAY_MIN = 2;

        private Bus bus;
        private Scheduler busScheduler;
        private Scheduler flushScheduler;
        private long flushDelay;
        private TimeUnit flushDelayTimeUnit;
        private ErrorListener errorListener;

        Builder() {
        }

        public Builder bus(Bus bus) {
            if (bus == null) {
                throw new NullPointerException("bus == null");
            }
            this.bus = bus;
            return this;
        }

        public Builder busScheduler(Scheduler scheduler) {
            if (scheduler == null) {
                throw new NullPointerException("scheduler == null");
            }
            this.busScheduler = scheduler;
            return this;
        }

        public Builder flushScheduler(Scheduler scheduler) {
            if (scheduler == null) {
                throw new NullPointerException("scheduler == null");
            }
            this.flushScheduler = scheduler;
            return this;
        }

        public Builder flushDelay(long flushDelay, TimeUnit timeUnit) {
            if (timeUnit == null) {
                throw new NullPointerException("timeUnit == null");
            }
            this.flushDelay = flushDelay;
            this.flushDelayTimeUnit = timeUnit;
            return this;
        }

        public Builder errorListener(ErrorListener errorListener) {
            if (errorListener == null) {
                throw new NullPointerException("errorListener == null");
            }
            this.errorListener = errorListener;
            return this;
        }

        public Dispatcher build() {
            if (bus == null) {
                bus = RxBus.create();
            }

            if (busScheduler == null) {
                busScheduler = Schedulers.immediate();
            }

            if (flushScheduler == null) {
                flushScheduler = Schedulers.io();
            }

            if (flushDelayTimeUnit == null) {
                flushDelay = DEFAULT_FLUSH_DELAY_MIN;
                flushDelayTimeUnit = TimeUnit.MINUTES;
            }

            if (errorListener == null) {
                errorListener = ErrorListener.NOOP;
            }

            return DefaultDispatcher.create(bus, busScheduler,
                                            DefaultFlusher.create(flushScheduler, flushDelay,
                                                                  flushDelayTimeUnit),
                                            errorListener);
        }
    }

    private RxBusDispatcher() {
        throw new AssertionError("No instances");
    }
}
