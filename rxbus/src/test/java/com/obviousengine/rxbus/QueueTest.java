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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QueueTest {

    @Test
    public void buildQueueOfEventType() throws Exception {
        Queue<Event> queue = Queue.of(Event.class).build();
        assertTrue(Event.class.equals(queue.getEventType()));
    }

    @Test
    public void buildQueueWithName() throws Exception {
        Queue<Event> queue = Queue.of(Event.class)
                .name("TestQueue")
                .build();
        assertEquals("TestQueue", queue.getName());
    }

    @Test
    public void incrementingQueueId() throws Exception {
        Queue<Event> queue1 = Queue.of(Event.class).build();
        Queue<Event> queue2 = Queue.of(Event.class).build();
        assertTrue(queue1.getId() < queue2.getId());
    }
}
