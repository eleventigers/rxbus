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

import android.util.Log;
import android.util.SparseArray;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.Subject;

/**
 * Simple {@link Bus} implementation with {@link Queue} caching and logging specific
 * to the Android platform. By default subscriptions are observed on the main thread.
 */
public final class AndroidRxBus implements Bus {

    public static AndroidRxBus create() {
        return create(new AndroidQueueCache());
    }

    public static AndroidRxBus create(RxBus.QueueCache cache) {
        return create(cache, new RxBus.Logger() {
            @Override
            public void log(String message) {
                Log.d(TAG, message);
            }
        });
    }

    public static AndroidRxBus create(RxBus.Logger logger) {
        return create(new AndroidQueueCache(), logger);
    }

    public static AndroidRxBus create(RxBus.QueueCache cache, RxBus.Logger logger) {
        return new AndroidRxBus(cache, logger);
    }

    private static final String TAG = AndroidRxBus.class.getSimpleName();

    private final Bus bus;

    private AndroidRxBus(RxBus.QueueCache cache, RxBus.Logger logger) {
       bus = RxBus.create(cache, logger);
    }

    @Override
    public <T> void publish(Queue<T> queue, T event) {
        bus.publish(queue, event);
    }

    @Override
    public <T> Observable<T> asObservable(Queue<T> queue) {
        return bus.asObservable(queue);
    }

    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer) {
        return subscribe(queue, observer, AndroidSchedulers.mainThread());
    }

    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer, Scheduler scheduler) {
        return bus.subscribe(queue, observer, scheduler);
    }

    private static final class AndroidQueueCache implements RxBus.QueueCache {

        private final SparseArray<Subject<?, ?>> sparseArray = new SparseArray<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> Subject<T, T> get(Queue<T> queue) {
            return (Subject<T, T>) sparseArray.get(queue.getId());
        }

        @Override
        public <T> void put(Queue<T> queue, Subject<T, T> subject) {
            sparseArray.put(queue.getId(), subject);
        }
    }
}