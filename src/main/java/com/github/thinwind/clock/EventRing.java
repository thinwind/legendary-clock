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

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import com.github.thinwind.lang.BitUtil;

/**
 *
 * 事件环
 * 
 * 仅支持单线程消费者
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-06-04  09:38
 *
 */
public final class EventRing<T> {

    //存储数据环的大小
    private final int ringSize;
    //环上每个槽位的容量
    // private final int slotSize;

    private final EventSlot<T>[] slots;

    private final BitSet posSet;

    private final AtomicInteger[] posBusyCounter;

    private int cursor = 0;

    @SuppressWarnings("all")
    public EventRing(int ringSize, int slotSize) {
        if (!BitUtil.powerOf2(ringSize)) {
            throw new IllegalArgumentException(
                    "Ring size must be a power of 2. But got [" + ringSize + "]");
        }
        this.ringSize = ringSize;
        slots = new EventSlot[slotSize];
        posSet = new BitSet(ringSize);
        posBusyCounter = new AtomicInteger[ringSize];
    }

    public void put(int pos, T t) {
        if (pos == 0) {
            pos = 1;
        }
        int index = -1;
        try {
            index = makeBusy(pos);
            slots[index].add(t);
        } finally {
            free(index);
        }
    }

    private void free(int index) {
        if (index < 0) {
            return;
        }
        if (posBusyCounter[index].decrementAndGet() <= 0) {
            posSet.clear(index);
        }
    }

    private int makeBusy(int pos) {
        int index = (cursor + pos) & (ringSize - 1);
        do {
            AtomicInteger counter = posBusyCounter[index];
            if (counter.getAndIncrement() == 1) {
                posSet.set(index);
            }
            if (index <= cursor) {
                if (counter.decrementAndGet() <= 0) {
                    posSet.clear(index);
                }
                index++;
            } else {
                return index;
            }
        } while (true);
    }

    public void comsume(Consumer<T> consumer) {
        EventSlot<T> current = getCurSlot();
        try {
            current.consume(consumer);
        } finally {
            cursor++;
        }
    }

    private EventSlot<T> getCurSlot() {
        while (posSet.get(cursor)) {
            Thread.yield();
        }
        return slots[cursor];
    }
}
