/*
 * Copyright (c) 2015 Obvious Engineering.
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

package com.obviousengine.rxbus;

import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subjects.Subject;

/**
 * Event notification system which enforces use of dedicated queues to perform type safe pub/sub.
 */
public interface Bus {

    /**
     * Subscribes observer to observe events from the given queue.
     */
    <T> Subscription subscribe(Queue<T> queue, Observer<T> observer);

    /**
     * Subscribes observer to observe events from the given queue on the given scheduler.
     */
    <T> Subscription subscribe(Queue<T> queue, Observer<T> observer, Scheduler scheduler);

    /**
     * Publishes event onto the given queue.
     */
    <T> void publish(Queue<T> queue, T event);

    /**
     * Converts the given {@link Queue} to a {@link Subject} to be used
     * outside the bus context (when combining Rx streams).
     */
    <T> Subject<T, T> queue(Queue<T> queue);
}
