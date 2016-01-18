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

public interface Dispatcher {

    <T> void publish(Queue<T> queue, T event);

    <T> void register(Queue<T> queue, Station<T> station);

    <T> void unregister(Station<T> station);
}
