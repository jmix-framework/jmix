/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package entity_states;

import io.jmix.core.EntityStates.PropertyLoadedState;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.impl.CachingLoadedPropertiesInfo;
import org.junit.jupiter.api.Test;
import test_support.app.entity.sales.Order;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachingLoadedPropertiesInfoTest {

    /**
     * Regression test for Jmix issue 5806: two threads checking properties of the same detached instance must not
     * fail with {@code ConcurrentModificationException} when the cache is empty.
     */
    @Test
    void isLoaded_otherThreadCachesPropertyDuringCheck_doesNotFail() throws Exception {
        Order order = new Order();
        CachingLoadedPropertiesInfo info = new CachingLoadedPropertiesInfo();

        CountDownLatch firstCheckStarted = new CountDownLatch(1);
        CountDownLatch secondPropertyCached = new CountDownLatch(1);
        PersistentAttributesLoadChecker checker = new TestLoadChecker() {
            @Override
            public boolean isLoaded(Object entity, String property) {
                if ("number".equals(property)) {
                    firstCheckStarted.countDown();
                    await(secondPropertyCached);
                }
                return true;
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> numberLoaded = executor.submit(() -> info.isLoaded(order, "number", checker));

            await(firstCheckStarted);
            assertTrue(info.isLoaded(order, "date", checker));
            secondPropertyCached.countDown();

            assertTrue(numberLoaded.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void isLoaded_detachedInstance_callsCheckerOncePerProperty() {
        Order order = new Order();
        CachingLoadedPropertiesInfo info = new CachingLoadedPropertiesInfo();

        AtomicInteger calls = new AtomicInteger();
        PersistentAttributesLoadChecker checker = new TestLoadChecker() {
            @Override
            public boolean isLoaded(Object entity, String property) {
                calls.incrementAndGet();
                return false;
            }
        };

        assertEquals(false, info.isLoaded(order, "number", checker));
        assertEquals(false, info.isLoaded(order, "number", checker));
        assertEquals(1, calls.get());
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private abstract static class TestLoadChecker implements PersistentAttributesLoadChecker {

        @Override
        public PropertyLoadedState isLoadedSafe(Object entity, String property) {
            return PropertyLoadedState.fromBoolean(isLoaded(entity, property));
        }
    }
}
