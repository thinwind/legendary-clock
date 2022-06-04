/*
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.thinwind.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * TODO EventSlot说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-06-04  09:42
 *
 */
public final class EventSlot<T> {

    private final int size;

    private final Object[] bucket;

    private final AtomicInteger cursor = new AtomicInteger(0);

    public EventSlot(int size) {
        this.size = size;
        bucket = new Object[size];
    }

    public void add(T t) {
        int pos = cursor.getAndIncrement();
        if (pos > size - 1) {
            throw new IllegalStateException("EventSlot does not have any room for new element.");
        }
        bucket[pos] = t;
    }

    @SuppressWarnings("all")
    public void consume(Consumer<T> consumer) {
        try{
            for (int i = 0; i < cursor.get(); i++) {
                consumer.accept((T)bucket[i]);
            }
        }finally{
            cursor.set(0);
        }
    }
}
