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

import io.jmix.flowui.view.View;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Background task for execute by {@link BackgroundWorker}.
 * <br>
 * If the task is associated with a view through "view" constructor parameter, it will be canceled when
 * the view is closed.
 * <br>
 * If timeout passed to constructor is exceeded, the task is canceled by special {@link BackgroundTaskWatchDog} thread.
 * <br>
 * Simplest usage example:
 * <pre>
 *    BackgroundTask&lt;Integer, Void&gt; task = new BackgroundTask&lt;Integer, Void&gt;(10, this) {
 *        public Void run(TaskLifeCycle&lt;Integer&gt; taskLifeCycle) throws Exception {
 *            for (int i = 0; i &lt; 5; i++) {
 *                TimeUnit.SECONDS.sleep(1);
 *            }
 *            return null;
 *        }
 *    };
 *    BackgroundTaskHandler taskHandler = backgroundWorker.handle(task);
 *    taskHandler.execute();
 * </pre>
 *
 * @param <T> task progress measurement unit
 * @param <V> result type
 */
@SuppressWarnings("unused")
public abstract class BackgroundTask<T, V> {

    private final View<?> view;

    private final long timeoutMilliseconds;

    private final List<ProgressListener<T, V>> progressListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Creates a task with timeout.
     *
     * @param timeout  timeout
     * @param timeUnit timeout time unit
     * @param view     owner view
     */
    protected BackgroundTask(long timeout, TimeUnit timeUnit, View<?> view) {
        this.view = view;
        this.timeoutMilliseconds = timeUnit.toMillis(timeout);
    }

    /**
     * Creates a task with timeout.
     * <p>
     * The task will not be associated with any {@link View}.
     *
     * @param timeout  timeout
     * @param timeUnit timeout time unit
     */
    protected BackgroundTask(long timeout, TimeUnit timeUnit) {
        this.view = null;
        this.timeoutMilliseconds = timeUnit.toMillis(timeout);
    }

    /**
     * Creates a task with timeout in default {@link TimeUnit#SECONDS} unit.
     * <br>
     * The task will not be associated with any {@link View}.
     *
     * @param timeoutSeconds timeout in seconds
     */
    protected BackgroundTask(long timeoutSeconds) {
        this.view = null;
        this.timeoutMilliseconds = TimeUnit.SECONDS.toMillis(timeoutSeconds);
    }

    /**
     * Create a task with timeout in default {@link TimeUnit#SECONDS} unit.
     *
     * @param timeoutSeconds timeout in seconds
     * @param view           owner view
     */
    protected BackgroundTask(long timeoutSeconds, View<?> view) {
        this.view = view;
        this.timeoutMilliseconds = TimeUnit.SECONDS.toMillis(timeoutSeconds);
    }

    /**
     * Main method that performs a task.
     * <br> Called by the execution environment in a separate working thread.
     *
     * <br> Implementation of this method should support interruption:
     * <ul>
     *     <li>In long loops check {@link TaskLifeCycle#isInterrupted()} and return if it is true</li>
     *     <li>Don't swallow {@link InterruptedException} - return from the method or don't catch it at all</li>
     * </ul>
     *
     * @param taskLifeCycle lifecycle object that allows the main method to interact with the execution environment
     * @return task result
     * @throws Exception exception in working thread
     */
    public abstract V run(TaskLifeCycle<T> taskLifeCycle) throws Exception;

    /**
     * Called by the execution environment in UI thread when the task is completed.
     *
     * @param result result of execution returned by {@link #run(TaskLifeCycle)} method
     */
    public void done(V result) {
    }

    /**
     * Called by the execution environment in UI thread if the task is canceled by
     * {@link BackgroundTaskHandler#cancel()} invocation.
     * <br>
     * This method is not called in case of timeout expiration or owner view closing.
     */
    public void canceled() {
    }

    /**
     * Called by the execution environment in UI thread if the task timeout is exceeded.
     *
     * @return true if this method implementation actually handles this event. Used for chaining handlers.
     */
    public boolean handleTimeoutException() {
        return false;
    }

    /**
     * Called by the execution environment in UI thread if the task {@link #run(TaskLifeCycle)} method raised an
     * exception.
     *
     * @param ex exception
     * @return true if this method implementation actually handles the exception. Used for chaining handlers.
     */
    public boolean handleException(Exception ex) {
        return false;
    }

    /**
     * Called by the execution environment in UI thread on progress change.
     *
     * @param changes list of changes since previous invocation
     */
    public void progress(List<T> changes) {
    }

    /**
     * Called by the execution environment in UI thread to prepare some execution parameters. These parameters can be
     * requested by the working thread inside the {@link #run(TaskLifeCycle)} method by calling
     * {@link TaskLifeCycle#getParams()}.
     *
     * @return parameters map or null if parameters are not needed
     */
    @Nullable
    public Map<String, Object> getParams() {
        return null;
    }

    /**
     * @return owner view
     */
    @Nullable
    public final View<?> getOwnerView() {
        return view;
    }

    /**
     * @return timeout in ms
     */
    public final long getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    /**
     * @return timeout in sec
     */
    public final long getTimeoutSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(timeoutMilliseconds);
    }

    /**
     * Add additional progress listener.
     *
     * @param progressListener listener
     */
    public final void addProgressListener(ProgressListener<T, V> progressListener) {
        if (!progressListeners.contains(progressListener))
            progressListeners.add(progressListener);
    }

    /**
     * Additional progress listeners.
     *
     * @return copy of the progress listeners collection
     */
    public final List<ProgressListener<T, V>> getProgressListeners() {
        return new ArrayList<>(progressListeners);
    }

    /**
     * Removes a progress listener.
     *
     * @param progressListener listener
     */
    public final void removeProgressListener(ProgressListener<T, V> progressListener) {
        progressListeners.remove(progressListener);
    }

    /**
     * Listener of the task life cycle events, complementary to the tasks own methods:
     * {@link BackgroundTask#progress(List)}, {@link BackgroundTask#done(Object)},
     * {@link BackgroundTask#canceled()}.
     *
     * @param <T> progress measurement unit
     * @param <V> result type
     */
    public interface ProgressListener<T, V> {

        /**
         * Called by the execution environment in UI thread on progress change.
         *
         * @param changes list of changes since previous invocation
         */
        void onProgress(List<T> changes);

        /**
         * Called by the execution environment in UI thread when the task is completed.
         *
         * @param result result of execution returned by {@link #run(TaskLifeCycle)} method
         */
        void onDone(V result);

        /**
         * Called by the execution environment in UI thread if the task is canceled.
         */
        void onCancel();
    }

    public static class ProgressListenerAdapter<T, V> implements ProgressListener<T, V> {

        @Override
        public void onProgress(List<T> changes) {
        }

        @Override
        public void onDone(V result) {
        }

        @Override
        public void onCancel() {
        }
    }
}