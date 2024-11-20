/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.testassist.vaadin;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.SpringVaadinSession;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TestVaadinSession extends SpringVaadinSession {

    public TestVaadinSession(VaadinService service) {
        super(service);
    }

    @Override
    public boolean hasLock() {
        return true;
    }

    @Override
    public Lock getLockInstance() {
        return new TestLock();
    }

    @Override
    public void lock() {
        // do nothing
    }

    @Override
    public void unlock() {
        // do nothing
    }

    protected static class TestLock implements Lock {

        @Override
        public void lock() {
            // do nothing
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            // do nothing
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void unlock() {
            // do nothing
        }

        @Override
        public Condition newCondition() {
            return new TestCondition();
        }
    }

    protected static class TestCondition implements Condition {

        @Override
        public void await() throws InterruptedException {
            // do nothing
        }

        @Override
        public void awaitUninterruptibly() {
            // do nothing
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return false;
        }

        @Override
        public void signal() {
            // do nothing
        }

        @Override
        public void signalAll() {
            // do nothing
        }
    }
}
