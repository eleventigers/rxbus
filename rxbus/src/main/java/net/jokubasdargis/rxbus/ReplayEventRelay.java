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

import com.jakewharton.rxrelay.BehaviorRelay;
import com.jakewharton.rxrelay.Relay;

import rx.Subscriber;

final class ReplayEventRelay<T> extends Relay<T, T> {

    public static <T> ReplayEventRelay<T> create() {
        return new ReplayEventRelay<>(new OnSubscribeFunc<T>(null));
    }

    public static <T> ReplayEventRelay<T> create(T event) {
        return new ReplayEventRelay<>(new OnSubscribeFunc<>(event));
    }

    private final Relay<T, T> wrappedRelay;

    private ReplayEventRelay(OnSubscribeFunc<T> onSubscribeFunc) {
        super(onSubscribeFunc);
        wrappedRelay = onSubscribeFunc.relay;
    }

    @Override
    public void call(T event) {
        wrappedRelay.call(event);
    }

    @Override
    public boolean hasObservers() {
        return wrappedRelay.hasObservers();
    }

    private static final class OnSubscribeFunc<T> implements OnSubscribe<T> {

        private final BehaviorRelay<T> relay;

        private OnSubscribeFunc(T event) {
            if (event == null) {
                relay = BehaviorRelay.create();
            } else {
                relay = BehaviorRelay.create(event);
            }
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            relay.subscribe(subscriber);
        }
    }
}
