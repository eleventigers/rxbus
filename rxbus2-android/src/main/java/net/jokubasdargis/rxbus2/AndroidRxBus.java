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

import android.util.Log;
import android.util.SparseArray;
import com.jakewharton.rxrelay2.Relay;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Simple {@link Bus} implementation with {@link Queue} caching and logging specific
 * to the Android platform. By default subscriptions are observed on the main thread.
 */
@SuppressWarnings("WeakerAccess")
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
    public <T> Relay<T> queue(Queue<T> queue) {
        return bus.queue(queue);
    }

    @Override
    public <T> Disposable subscribe(Queue<T> queue, Consumer<T> observer) {
        return subscribe(queue, observer, AndroidSchedulers.mainThread());
    }

    @Override
    public <T> Disposable subscribe(Queue<T> queue, Consumer<T> observer, Scheduler scheduler) {
        return bus.subscribe(queue, observer, scheduler);
    }

    private static final class AndroidQueueCache implements RxBus.QueueCache {

        private final SparseArray<Relay<?>> sparseArray = new SparseArray<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> Relay<T> get(Queue<T> queue) {
            return (Relay<T>) sparseArray.get(queue.getId());
        }

        @Override
        public <T> void put(Queue<T> queue, Relay<T> relay) {
            sparseArray.put(queue.getId(), relay);
        }
    }
}