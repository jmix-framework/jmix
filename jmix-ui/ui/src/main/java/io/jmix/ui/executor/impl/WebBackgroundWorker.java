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

package io.jmix.ui.executor.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.event.BackgroundTaskUnhandledExceptionEvent;
import io.jmix.ui.executor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Web implementation of {@link BackgroundWorker}
 */
@Component("ui_BackgroundWorker")
public class WebBackgroundWorker implements BackgroundWorker {

    private static final Logger log = LoggerFactory.getLogger(WebBackgroundWorker.class);

    private static final String THREAD_NAME_PREFIX = "BackgroundTask-";
    private static final Pattern THREAD_NAME_PATTERN = Pattern.compile("BackgroundTask-([0-9]+)");

    @Autowired
    protected WatchDog watchDog;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    protected TimeSource timeSource;

    protected UiBackgroundTaskProperties properties;

    protected ExecutorService executorService;

    public WebBackgroundWorker() {
    }

    @Autowired
    public void setProperties(UiBackgroundTaskProperties properties) {
        this.properties = properties;

        createThreadPoolExecutor();
    }

    protected void createThreadPoolExecutor() {
        if (executorService != null) {
            return;
        }

        this.executorService = new ThreadPoolExecutor(
                properties.getThreadsCount(),
                properties.getThreadsCount(),
                10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat(THREAD_NAME_PREFIX + "%d")
                        .build()
        );
        ((ThreadPoolExecutor)this.executorService).allowCoreThreadTimeOut(true);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdownNow();
    }

    @Override
    public <T, V> BackgroundTaskHandler<V> handle(BackgroundTask<T, V> task) {
        checkNotNull(task);
        checkUIAccess();

        App appInstance;
        try {
            appInstance = App.getInstance();
        } catch (IllegalStateException ex) {
            log.error("Couldn't handle task", ex);
            throw ex;
        }

        AppUI ui = AppUI.getCurrent();

        // create task executor
        WebTaskExecutor<T, V> taskExecutor = new WebTaskExecutor<>(ui, task);

        // add thread to taskSet
        appInstance.addBackgroundTask(taskExecutor.getFuture());

        // create task handler
        TaskHandlerImpl<T, V> taskHandler = new TaskHandlerImpl<>(
                getUIAccessor(), taskExecutor, watchDog, applicationEventPublisher, currentAuthentication.getUser(), timeSource);
        taskExecutor.setTaskHandler(taskHandler);

        return taskHandler;
    }

    @Override
    public UIAccessor getUIAccessor() {
        checkUIAccess();

        return new WebUIAccessor(UI.getCurrent());
    }

    @Override
    public void checkUIAccess() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();

        if (vaadinSession == null || !vaadinSession.hasLock()) {
            throw new IllegalConcurrentAccessException();
        }
    }

    private class WebTaskExecutor<T, V> implements TaskExecutor<T, V>, Callable<V> {

        private AppUI ui;

        private FutureTask<V> future;

        private BackgroundTask<T, V> runnableTask;
        private Runnable finalizer;

        private volatile boolean isClosed = false;
        private volatile boolean doneHandled = false;

        private Authentication authentication;
        private String username;

        private Map<String, Object> params;
        private TaskHandlerImpl<T, V> taskHandler;

        private WebTaskExecutor(AppUI ui, BackgroundTask<T, V> runnableTask) {
            this.runnableTask = runnableTask;
            this.ui = ui;

            this.params = runnableTask.getParams() != null ?
                    Collections.unmodifiableMap(runnableTask.getParams()) :
                    Collections.emptyMap();

            authentication = SecurityContextHelper.getAuthentication();

            this.username = currentAuthentication.getUser().getUsername();

            this.future = new FutureTask<V>(this) {
                @Override
                protected void done() {
                    Authentication previousAuth = SecurityContextHelper.getAuthentication();
                    SecurityContextHelper.setAuthentication(authentication);
                    try {
                        WebTaskExecutor.this.ui.access(() ->
                                handleDone()
                        );
                    } finally {
                        SecurityContextHelper.setAuthentication(previousAuth);
                    }
                }
            };
        }

        @Override
        public final V call() throws Exception {
            String threadName = Thread.currentThread().getName();
            Matcher matcher = THREAD_NAME_PATTERN.matcher(threadName);
            if (matcher.find()) {
                Thread.currentThread().setName(THREAD_NAME_PREFIX + matcher.group(1) + "-" + username);
            }

            SecurityContextHelper.setAuthentication(authentication);
            try {
                // do not run any activity if canceled before start
                return runnableTask.run(new TaskLifeCycle<T>() {
                    @SafeVarargs
                    @Override
                    public final void publish(T... changes) throws InterruptedException {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException("Task is interrupted and is trying to publish changes");
                        }

                        handleProgress(changes);
                    }

                    @Override
                    public boolean isInterrupted() {
                        return Thread.currentThread().isInterrupted();
                    }

                    @Override
                    public boolean isCancelled() {
                        return future.isCancelled();
                    }

                    @Override
                    public Map<String, Object> getParams() {
                        return params;
                    }
                });
            } finally {
                SecurityContextHelper.setAuthentication(null);
            }
        }

        @SafeVarargs
        @Override
        public final void handleProgress(T... changes) {
            if (changes != null) {
                ui.access(() ->
                        process(Arrays.asList(changes))
                );
            }
        }

        @ExecutedOnUIThread
        protected final void process(List<T> chunks) {
            runnableTask.progress(chunks);
            // Notify listeners
            for (BackgroundTask.ProgressListener<T, V> listener : runnableTask.getProgressListeners()) {
                listener.onProgress(chunks);
            }
        }

        @ExecutedOnUIThread
        protected final void handleDone() {
            if (isCancelled()) {
                // handle cancel from EDT before execution start
                log.trace("Done statement is not processed because it is canceled task");
                return;
            }

            if (isClosed) {
                log.trace("Done statement is not processed because it is already closed");
                return;
            }

            log.debug("Done task. User: {}", username);

            // do not allow to cancel task from done listeners and exception handler
            isClosed = true;

            unregister();

            // As "handleDone()" can be processed under BackgroundTask thread or under UI thread from which
            // the task starts, we should save previous security context (that can be null)
            // to restore it when "done()" is finished.
            Authentication previousAuth = SecurityContextHelper.getAuthentication();

            try {
                SecurityContextHelper.setAuthentication(authentication);

                V result = future.get();

                runnableTask.done(result);
                // Notify listeners
                for (BackgroundTask.ProgressListener<T, V> listener : runnableTask.getProgressListeners()) {
                    listener.onDone(result);
                }
            } catch (CancellationException e) {
                log.debug("Cancellation exception in background task", e);
            } catch (InterruptedException e) {
                log.debug("Interrupted exception in background task", e);
            } catch (ExecutionException e) {
                // do not call log.error, exception may be handled later
                log.debug("Exception in background task", e);
                if (!future.isCancelled()) {
                    boolean handled = false;

                    if (e.getCause() instanceof Exception) {
                        handled = runnableTask.handleException((Exception) e.getCause());
                    }

                    if (!handled) {
                        log.error("Unhandled exception in background task", e);
                        applicationEventPublisher.publishEvent(new BackgroundTaskUnhandledExceptionEvent(this, runnableTask, e));
                    }
                }
            } finally {
                SecurityContextHelper.setAuthentication(previousAuth);

                if (finalizer != null) {
                    finalizer.run();
                    finalizer = null;
                }

                doneHandled = true;
            }
        }

        @ExecutedOnUIThread
        @Override
        public final boolean cancelExecution() {
            if (isClosed) {
                return false;
            }

            unregister();

            log.debug("Cancel task. User: {}", username);

            boolean isCanceledNow = future.cancel(true);
            if (isCanceledNow) {
                log.trace("Task was cancelled. User: {}", username);
            } else {
                log.trace("Cancellation of task isn't processed. User: {}", username);
            }

            if (!doneHandled) {
                log.trace("Done was not handled. Return 'true' as canceled status. User: {}", username);

                this.isClosed = true;
                return true;
            }

            return isCanceledNow;
        }

        @ExecutedOnUIThread
        protected void unregister() {
            log.trace("Unregister task");

            ui.getApp().removeBackgroundTask(future);
            watchDog.removeTask(taskHandler);
        }

        @ExecutedOnUIThread
        @Nullable
        @Override
        public final V getResult() {
            V result;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException | CancellationException e) {
                log.debug("{} exception in background task", e.getClass().getName(), e);
                return null;
            }

            this.handleDone();

            return result;
        }

        @Override
        public final BackgroundTask<T, V> getTask() {
            return runnableTask;
        }

        @ExecutedOnUIThread
        @Override
        public final void startExecution() {
            // Start thread
            executorService.execute(() ->
                    future.run()
            );
        }

        @Override
        public final boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public final boolean isDone() {
            return future.isDone();
        }

        @Override
        public final boolean inProgress() {
            return !isClosed;
        }

        @ExecutedOnUIThread
        @Override
        public final void setFinalizer(Runnable finalizer) {
            this.finalizer = finalizer;
        }

        @Override
        public final Runnable getFinalizer() {
            return finalizer;
        }

        public void setTaskHandler(TaskHandlerImpl<T, V> taskHandler) {
            this.taskHandler = taskHandler;
        }

        public FutureTask<V> getFuture() {
            return future;
        }
    }

    private static class WebUIAccessor implements UIAccessor {
        private UI ui;

        public WebUIAccessor(UI ui) {
            this.ui = ui;
        }

        @Override
        public void access(Runnable runnable) {
            ui.access(runnable);
        }

        @Override
        public void accessSynchronously(Runnable runnable) {
            ui.accessSynchronously(runnable);
        }
    }
}