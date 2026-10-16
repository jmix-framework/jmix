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

package test_support;

import io.jmix.aitoolsflowui.generation.AiChatGenerationManager;
import org.jspecify.annotations.Nullable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A single worker thread that replaces the generation pool of an {@link AiChatGenerationManager} for one test, so
 * the test can wait for a worker that outlives its response, e.g. a model call that ignored a cancel.
 */
public class TestGenerationWorkers {

    protected final AiChatGenerationManager manager;
    @Nullable
    protected final Object managerExecutor;
    protected final ExecutorService workers = Executors.newSingleThreadExecutor();

    protected TestGenerationWorkers(AiChatGenerationManager manager) {
        this.manager = manager;
        this.managerExecutor = ReflectionTestUtils.getField(manager, "executor");
    }

    /**
     * Makes the manager run its responses on a new single worker until {@link #restore()}.
     *
     * @param manager manager whose pool to replace
     * @return the installed workers
     */
    public static TestGenerationWorkers install(AiChatGenerationManager manager) {
        TestGenerationWorkers testWorkers = new TestGenerationWorkers(manager);
        ReflectionTestUtils.setField(manager, "executor", testWorkers.workers);
        return testWorkers;
    }

    /**
     * Runs a task on the worker, e.g. to keep it busy so that the next response waits in the queue.
     *
     * @param task task to run
     */
    public void execute(Runnable task) {
        workers.execute(task);
    }

    /**
     * Waits until every task given to the worker so far has finished.
     *
     * @throws InterruptedException if interrupted while waiting
     * @throws AssertionError       if the tasks do not finish within the timeout
     */
    public void awaitIdle() throws InterruptedException {
        workers.shutdown();
        if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new AssertionError("Generation workers did not finish");
        }
    }

    /**
     * Stops the worker and gives the manager its own pool back.
     */
    public void restore() {
        workers.shutdownNow();
        ReflectionTestUtils.setField(manager, "executor", managerExecutor);
    }
}
