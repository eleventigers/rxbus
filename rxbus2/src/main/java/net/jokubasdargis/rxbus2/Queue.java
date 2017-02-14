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

package net.jokubasdargis.rxbus2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A communication channel for specific event types.
 *
 * @param <T> Type of event this queue supports.
 */
@SuppressWarnings("WeakerAccess")
public final class Queue<T> {

    /**
     * Creates a builder of a {@link Queue} for the given event type.
     */
    public static <T> Builder<T> of(Class<T> eventTypeClass) {
        return new Builder<>(eventTypeClass);
    }

    /**
     * A builder to build customized {@link Queue}s.
     */
    public static final class Builder<T> {

        public Queue<T> build() {
            if (name == null) {
                name = eventType.getSimpleName() + "Queue";
            }

            return new Queue<>(name, eventType, replayLast, defaultEvent);
        }

        public Builder<T> name(String s) {
            name = s;
            return this;
        }

        public Builder<T> replay() {
            replayLast = true;
            return this;
        }

        public Builder replay(T obj) {
            replayLast = true;
            defaultEvent = obj;
            return this;
        }

        private T defaultEvent;
        private final Class<T> eventType;
        private String name;
        private boolean replayLast;

        Builder(Class<T> eventTypeClass) {
            eventType = eventTypeClass;
        }
    }

    private static final AtomicInteger RUNNING_ID = new AtomicInteger();

    private final Class<T> eventType;
    private final String name;

    private final int id;
    private final T defaultEvent;
    private final boolean replayLast;

    Queue(String s, Class<T> eventTypeClass, boolean flag, T event) {
        name = s;
        eventType = eventTypeClass;
        replayLast = flag;
        defaultEvent = event;
        id = RUNNING_ID.incrementAndGet();
    }

    public Class<T> getEventType() {
        return eventType;
    }

    public String getName() {
        return name;
    }

    int getId() {
        return id;
    }

    T getDefaultEvent() {
        return defaultEvent;
    }

    boolean isReplayLast() {
        return replayLast;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Queue) && ((Queue) obj).id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name + "[" + eventType.getCanonicalName() + "]";
    }
}