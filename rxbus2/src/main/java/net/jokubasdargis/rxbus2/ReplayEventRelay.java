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


import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;


import io.reactivex.Observer;


final class ReplayEventRelay<T> extends Relay< T> {

    public static <T> ReplayEventRelay<T> create() {
        return new ReplayEventRelay<>();
    }

    public static <T> ReplayEventRelay<T> create(T event) {
        return new ReplayEventRelay<>(event);
    }

    private final Relay< T> wrappedRelay;

    private ReplayEventRelay() {

        wrappedRelay = BehaviorRelay.create();
    }

    private ReplayEventRelay(T event) {

        wrappedRelay = BehaviorRelay.createDefault(event);
    }

    @Override
    public void accept(T event) {
        wrappedRelay.accept(event);
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        wrappedRelay.subscribeWith(observer);
    }

    @Override
    public boolean hasObservers() {
        return wrappedRelay.hasObservers();
    }





}
