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


import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;



/**
 * A basic implementation of {@link Bus} with {@link net.jokubasdargis.rxbus2.Queue} caching and logging capabilities.
 */
@SuppressWarnings("WeakerAccess")
public final class RxBus implements Bus {

    /**
     * Creates a new instance of {@link Bus} configured with a default {@link QueueCache}.
     */
    public static Bus create() {
        return create(new DefaultQueueCache());
    }

    /**
     * Creates a new instance of {@link Bus} configured with the given {@link QueueCache}.
     */
    public static Bus create(QueueCache cache) {
        return create(cache, null);
    }

    /**
     * Creates a new instance of {@link Bus} configured with the given {@link Logger}.
     */
    public static Bus create(Logger logger) {
        return create(new DefaultQueueCache(), logger);
    }

    /**
     * Creates a new instance of {@link Bus} configured with the given {@link QueueCache}
     * and {@link Logger}.
     */
    public static Bus create(QueueCache cache, Logger logger) {
        return new RxBus(cache, logger);
    }

    /**
     * Classes implementing this interface log debug messages from the {@link RxBus}.
     */
    public interface Logger {
        void log(String message);
    }

    /**
     * Classes implementing this interface provide a cache of {@link Relay}s associated to
     * {@link net.jokubasdargis.rxbus2.Queue}s used by the {@link RxBus}.
     */
    public interface QueueCache {
        <T> Relay<T> get(net.jokubasdargis.rxbus2.Queue<T> queue);

        <T> void put(net.jokubasdargis.rxbus2.Queue<T> queue, Relay< T> relay);
    }

    private final QueueCache cache;
    private final Logger logger;
    private final Map<Integer, List<Reference<Consumer<?>>>> loggedObservers;

    private RxBus(QueueCache cache, Logger logger) {
        if (cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
        this.cache = cache;
        this.logger = logger;
        this.loggedObservers = this.logger != null
                ? new HashMap<Integer, List<Reference<Consumer<?>>>>() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void publish(net.jokubasdargis.rxbus2.Queue<T> queue, T event) {
        if (shouldLog()) {
            logEvent(queue, event);
        }
        queue(queue).accept(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Relay< T> queue(net.jokubasdargis.rxbus2.Queue<T> queue) {
        Relay< T> relay = cache.get(queue);
        if (relay == null) {
            if (queue.getDefaultEvent() != null) {
                relay = net.jokubasdargis.rxbus2.ReplayEventRelay.create(queue.getDefaultEvent());
            } else if (queue.isReplayLast()) {
                relay = net.jokubasdargis.rxbus2.ReplayEventRelay.create(queue.getDefaultEvent());
            } else {
                relay = net.jokubasdargis.rxbus2.DefaultEventRelay.create();
            }
            cache.put(queue, relay);
        }
        return relay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Disposable subscribe(net.jokubasdargis.rxbus2.Queue<T> queue, Consumer<T> observer) {
        return subscribe(queue, observer, Schedulers.trampoline());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Disposable subscribe(net.jokubasdargis.rxbus2.Queue<T> queue, Consumer<T> observer, Scheduler scheduler) {
        if (shouldLog()) {
            registerObserver(queue, observer);
        }
        return queue(queue).observeOn(scheduler).subscribe(observer);
    }

    private boolean shouldLog() {
        return logger != null && loggedObservers != null;
    }

    @SuppressWarnings("unchecked")
    private <T> void registerObserver(net.jokubasdargis.rxbus2.Queue<T> queue, Consumer<T> observer) {
        List observers = (List) loggedObservers.get(queue.getId());
        if (observers == null) {
            observers = new LinkedList<>();
            loggedObservers.put(queue.getId(), observers);
        }
        observers.add(new WeakReference<>(observer));
    }

    private void logEvent(net.jokubasdargis.rxbus2.Queue queue, Object obj) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Publishing to ").append(queue.getName());
        stringBuilder.append(" [").append(obj).append("]\n");
        List list = loggedObservers.get(queue.getId());
        if (list != null && !list.isEmpty()) {
            stringBuilder.append("Delivering to: \n");
            Iterator iterator = list.iterator();
            do {
                if (!iterator.hasNext()) {
                    break;
                }
                Consumer observer = (Consumer) ((Reference) iterator.next()).get();
                if (observer != null) {
                    stringBuilder
                            .append("-> ")
                            .append(observer.getClass().getCanonicalName())
                            .append('\n');
                }
            } while (true);
        } else {
            stringBuilder.append("No observers found.");
        }
        logger.log(stringBuilder.toString());
    }

    static final class DefaultQueueCache implements QueueCache {

        private final Map<net.jokubasdargis.rxbus2.Queue<?>, Relay< ?>> map
                = Collections.synchronizedMap(new WeakHashMap<net.jokubasdargis.rxbus2.Queue<?>, Relay< ?>>());

        @Override
        @SuppressWarnings("unchecked")
        public <T> Relay< T> get(net.jokubasdargis.rxbus2.Queue<T> queue) {
            return (Relay<T>) map.get(queue);
        }

        @Override
        public <T> void put(net.jokubasdargis.rxbus2.Queue<T> queue, Relay< T> relay) {
            map.put(queue, relay);
        }
    }
}
