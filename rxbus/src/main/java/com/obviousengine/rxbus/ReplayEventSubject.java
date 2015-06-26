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

import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

final class ReplayEventSubject<T> extends Subject<T, T> {

    public static <T> ReplayEventSubject<T> create() {
        return new ReplayEventSubject<>(new OnSubscribeFunc<T>(null));
    }

    public static <T> ReplayEventSubject<T> create(T event) {
        return new ReplayEventSubject<>(new OnSubscribeFunc<>(event));
    }

    private final Subject<T, T> wrappedSubject;

    private ReplayEventSubject(OnSubscribeFunc<T> onSubscribeFunc) {
        super(onSubscribeFunc);
        wrappedSubject = onSubscribeFunc.subject;
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onNext(T event) {
        wrappedSubject.onNext(event);
    }

    @Override
    public boolean hasObservers() {
        return wrappedSubject.hasObservers();
    }

    private static final class OnSubscribeFunc<T> implements OnSubscribe<T> {

        private final BehaviorSubject<T> subject;

        private OnSubscribeFunc(T event) {
            if (event == null) {
                subject = BehaviorSubject.create();
            } else {
                subject = BehaviorSubject.create(event);
            }
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            subject.subscribe(subscriber);
        }
    }
}