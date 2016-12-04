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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.Subject;

/**
 * A basic implementation of {@link Bus} with {@link Queue} caching and logging capabilities.
 */
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
     * Classes implementing this interface provide a cache of {@link Subject}s associated to
     * {@link Queue}s used by the {@link RxBus}.
     */
    public interface QueueCache {
        <T> Subject<T, T> get(Queue<T> queue);

        <T> void put(Queue<T> queue, Subject<T, T> subject);
    }

    private final QueueCache cache;
    private final Logger logger;
    private final Map<Integer, List<Reference<Observer<?>>>> loggedObservers;

    private RxBus(QueueCache cache, Logger logger) {
        if (cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
        this.cache = cache;
        this.logger = logger;
        this.loggedObservers = this.logger != null
                ? new HashMap<Integer, List<Reference<Observer<?>>>>() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void publish(Queue<T> queue, T event) {
        if (shouldLog()) {
            logEvent(queue, event);
        }
        queue(queue).onNext(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Observable<T> asObservable(Queue<T> queue) {
        return queue(queue).asObservable();
    }

    /**
     * {@inheritDoc}
     */
    private  <T> Subject<T, T> queue(Queue<T> queue) {
        Subject<T, T> subject = cache.get(queue);
        if (subject == null) {
            if (queue.getDefaultEvent() != null) {
                subject = ReplayEventSubject.create(queue.getDefaultEvent());
            } else if (queue.isReplayLast()) {
                subject = ReplayEventSubject.create();
            } else {
                subject = DefaultEventSubject.create();
            }
            cache.put(queue, subject);
        }
        return subject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer) {
        return subscribe(queue, observer, Schedulers.immediate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer, Scheduler scheduler) {
        if (shouldLog()) {
            registerObserver(queue, observer);
        }
        return queue(queue).observeOn(scheduler).subscribe(observer);
    }

    private boolean shouldLog() {
        return logger != null && loggedObservers != null;
    }

    @SuppressWarnings("unchecked")
    private <T> void registerObserver(Queue<T> queue, Observer<T> observer) {
        List observers = (List) loggedObservers.get(queue.getId());
        if (observers == null) {
            observers = new LinkedList<>();
            loggedObservers.put(queue.getId(), observers);
        }
        observers.add(new WeakReference<>(observer));
    }

    private void logEvent(Queue queue, Object obj) {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("Publishing to ").append(queue.getName());
        stringbuilder.append(" [").append(obj).append("]\n");
        List list = loggedObservers.get(queue.getId());
        if (list != null && !list.isEmpty()) {
            stringbuilder.append("Delivering to: \n");
            Iterator iterator = list.iterator();
            do {
                if (!iterator.hasNext()) {
                    break;
                }
                Observer observer = (Observer) ((Reference) iterator.next()).get();
                if (observer != null) {
                    stringbuilder
                            .append("-> ")
                            .append(observer.getClass().getCanonicalName())
                            .append('\n');
                }
            } while (true);
        } else {
            stringbuilder.append("No observers found.");
        }
        logger.log(stringbuilder.toString());
    }

    private static final class DefaultQueueCache implements QueueCache {

        private final Map<Queue<?>, Subject<?, ?>> map
                = Collections.synchronizedMap(new WeakHashMap<Queue<?>, Subject<?, ?>>());

        @Override
        @SuppressWarnings("unchecked")
        public <T> Subject<T, T> get(Queue<T> queue) {
            return (Subject<T, T>) map.get(queue);
        }

        @Override
        public <T> void put(Queue<T> queue, Subject<T, T> subject) {
            map.put(queue, subject);
        }
    }
}
