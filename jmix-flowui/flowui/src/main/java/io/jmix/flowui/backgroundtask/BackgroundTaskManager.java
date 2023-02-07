/*
 * Copyright 2019 Haulmont.
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

package io.jmix.flowui.backgroundtask;

import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class BackgroundTaskManager {
    private static final Logger log = LoggerFactory.getLogger(BackgroundTaskManager.class);

    private transient Set<Future<?>> taskSet;

    public BackgroundTaskManager() {
        taskSet = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    /**
     * Adds task to task set.
     *
     * @param task Task
     */
    public void addTask(Future<?> task) {
        taskSet.add(task);
    }

    /**
     * Stops manage of stopped task.
     *
     * @param task Task
     */
    public void removeTask(Future<?> task) {
        taskSet.remove(task);
    }

    /**
     * Interrupts all tasks.
     */
    public void cleanupTasks() {
        int count = 0;
        // Stop threads
        for (Future<?> taskThread : taskSet) {
            if (!taskThread.isDone()) {
                taskThread.cancel(true);
            }
            count++;
        }
        // Clean task set
        taskSet.clear();

        if (count > 0) {
            log.debug("Interrupted {} background tasks", count);
        }
    }

    /**
     * @return background task manager instance corresponding current {@link VaadinSession}. Can be invoked anywhere
     * in application code.
     * @throws IllegalStateException if no background task manager instance is bound to the current
     *                               {@link VaadinSession}
     */
    public static BackgroundTaskManager getInstance() {
        VaadinSession vSession = VaadinSession.getCurrent();
        if (vSession == null) {
            throw new IllegalStateException("No VaadinSession found");
        }
        if (!vSession.hasLock()) {
            throw new IllegalStateException("VaadinSession is not owned by the current thread");
        }
        BackgroundTaskManager backgroundTaskManager = vSession.getAttribute(BackgroundTaskManager.class);
        if (backgroundTaskManager == null) {
            throw new IllegalStateException("No BackgroundTaskManager is bound to the current VaadinSession");
        }
        return backgroundTaskManager;
    }

    /**
     * @return {@code true} if an {@link BackgroundTaskManager} instance is currently bound and can be safely obtained
     * by {@link #getInstance()}
     */
    public static boolean isBound() {
        VaadinSession vSession = VaadinSession.getCurrent();
        return vSession != null
                && vSession.hasLock()
                && vSession.getAttribute(BackgroundTaskManager.class) != null;
    }
}