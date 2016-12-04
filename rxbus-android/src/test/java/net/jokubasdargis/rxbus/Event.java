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

final class Event {

    static Event create() {
        return new Event(System.nanoTime());
    }

    static Event create(long timestamp) {
        return new Event(timestamp);
    }

    private final long timestampNanos;

    private Event(long timestamp) {
        this.timestampNanos = timestamp;
    }

    public long getTimestamp(TimeUnit unit) {
        return unit.convert(timestampNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public String toString() {
        return "Event{"
               + "timestamp=" + timestampNanos
               + '}';
    }
}
