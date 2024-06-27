package io.jmix.flowui.asynctask;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The class provides methods for executing asynchronous tasks from UI views. The class may be used when, in the UI
 * view, you need to execute some long-running task in a separate thread, for example, loading data from a database, and
 * when the task is completed (data is loaded), update the UI using the returned data.
 * <p>
 * Use {@link UiAsyncTasks} when you don't need to handle asynchronous task progress or display a modal dialog with a
 * progress indicator. Otherwise, consider using {@link io.jmix.flowui.backgroundtask.BackgroundTaskManager} and
 * {@link io.jmix.flowui.backgroundtask.BackgroundTask}.
 * <p>
 * The class wraps actions that perform tasks (for example, loading data) into a special wrapper
 * ({@link DelegatingSecuritySupplier} or {@link DelegatingSecurityRunnable}). These wrappers set the original
 * {@link org.springframework.security.core.context.SecurityContext} before executing the action. After that, all
 * services invoked in the asynchronous task will be executed with the permissions of the current user.
 * <p>
 * Actions that process the result can update Vaadin UI components because they are wrapped by the
 * {@link UI#access(Command)} method.
 * <p>
 * Usage example:
 * <pre>{@code
 * uiAsyncTasks.supplyAsyncBuilder(() -> customerService.loadCustomers())
 *      .withResultHandler(customers -> {
 *          customersDc.getMutableItems().addAll(customers);
 *          notifications.create("Customers loaded: " + customers.size()).show();
 *      })
 *      .supplyAsync();}</pre>
 * <p>
 * By default, asynchronous tasks are executed with the default timeout configured by the
 * {@link UiAsyncTaskProperties#defaultTimeoutSec}. After this timeout, the {@link TimeoutException} is thrown.
 *
 * @see UiAsyncTaskProperties
 * @see #supplyAsyncBuilder(Supplier)
 * @see #runAsyncBuilder(Runnable)
 */
@Component("flowui_UiAsyncTasks")
public class UiAsyncTasks {

    private static final Logger log = LoggerFactory.getLogger(UiAsyncTasks.class);

    private static final String THREAD_NAME_PREFIX = "UiAsyncTask-";

    private ExecutorService executorService;

    private Function<Throwable, Void> defaultExceptionHandler;

    private final UiAsyncTaskProperties uiAsyncTaskProperties;

    public UiAsyncTasks(UiAsyncTaskProperties uiAsyncTaskProperties) {
        this.uiAsyncTaskProperties = uiAsyncTaskProperties;
    }

    @PostConstruct
    protected void onInit() {
        executorService = createExecutorService();
        defaultExceptionHandler = createDefaultExceptionHandler();
    }

    @PreDestroy
    protected void preDestroy() {
        executorService.shutdownNow();
    }

    protected ExecutorService createExecutorService() {
        int maximumPoolSize = uiAsyncTaskProperties.getExecutorService().getMaximumPoolSize();
        executorService = new ThreadPoolExecutor(
                maximumPoolSize,
                maximumPoolSize,
                10L, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat(THREAD_NAME_PREFIX + "%d")
                        .build()
        );
        ((ThreadPoolExecutor) executorService).allowCoreThreadTimeOut(true);
        return executorService;
    }

    /**
     * Creates a builder for an asynchronous task that returns a result. The task is executed on a separate thread. The
     * result can be handled with a {@link Consumer} that is set using the
     * {@link SupplyAsyncBuilder#withResultHandler(Consumer)} or left unhandled to get the result using the
     * {@link CompletableFuture} API.
     *
     * @param asyncTask the task to execute
     * @param <T>       the result type
     * @return a builder for an asynchronous task that returns a result
     */
    public <T> SupplyAsyncBuilder<T> supplyAsyncBuilder(Supplier<T> asyncTask) {
        return new SupplyAsyncBuilder<>(asyncTask);
    }

    /**
     * Creates a builder for an asynchronous task that does not return a result. The task is executed on a separate
     * thread. The action that must be performed after the asynchronous task is completed may be set using the
     * {@link RunAsyncBuilder#withResultHandler(Runnable) method.
     *
     * @param asyncTask the task to execute
     * @return a builder for an asynchronous task that does not return a result
     */
    public RunAsyncBuilder runAsyncBuilder(Runnable asyncTask) {
        return new RunAsyncBuilder(asyncTask);
    }

    /**
     * Abstract base class for asynchronous task builders.
     */
    protected abstract class AbstractAsyncTaskBuilder {
        protected Consumer<Throwable> exceptionHandler;
        protected UI ui;
        protected int timeout;
        protected TimeUnit timeoutUnit;

        public AbstractAsyncTaskBuilder() {
            this.ui = UI.getCurrent();
        }

        protected void configureTimeout(CompletableFuture<Void> resultCompletableFuture) {
            if (timeout > 0 && timeoutUnit != null) {
                resultCompletableFuture.orTimeout(timeout, timeoutUnit);
            } else {
                int defaultTimeoutSec = uiAsyncTaskProperties.getDefaultTimeoutSec();
                if (defaultTimeoutSec > 0) {
                    resultCompletableFuture.orTimeout(defaultTimeoutSec, TimeUnit.SECONDS);
                }
            }
        }

        protected void configureExceptionHandler(CompletableFuture<Void> completableFuture) {
            if (exceptionHandler != null) {
                completableFuture.exceptionally(throwable -> {
                    ui.access(() -> exceptionHandler.accept(throwable));
                    return null;
                });
            } else {
                completableFuture.exceptionally(defaultExceptionHandler);
            }
        }
    }

    public class SupplyAsyncBuilder<T> extends AbstractAsyncTaskBuilder {
        private Supplier<T> asyncTask;
        private Consumer<? super T> resultHandler;

        public SupplyAsyncBuilder(Supplier<T> asyncTask) {
            super();
            this.asyncTask = asyncTask;
        }

        /**
         * Sets a handler that is called when the asynchronous task completes successfully. The handler can safely
         * update Vaadin UI components.
         */
        public SupplyAsyncBuilder<T> withResultHandler(Consumer<? super T> resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        /**
         * Sets a handler that is called when the asynchronous task throws an exception. The handler can safely update
         * Vaadin UI components. If not set, then the default exception handler will be used.
         */
        public SupplyAsyncBuilder<T> withExceptionHandler(Consumer<Throwable> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * Sets the timeout for the asynchronous task. If the task is not completed within the specified timeout, a
         * {@link TimeoutException} is thrown.
         */
        public SupplyAsyncBuilder<T> withTimeout(int timeout, TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        /**
         * Configures and returns a {@link CompletableFuture} that asynchronously executes the task.
         */
        public CompletableFuture<Void> supplyAsync() {
            DelegatingSecuritySupplier<T> wrappedSupplier = new DelegatingSecuritySupplier<>(asyncTask);
            CompletableFuture<T> future = CompletableFuture.supplyAsync(wrappedSupplier, executorService);
            CompletableFuture<Void> resultCompletableFuture;

            if (resultHandler != null) {
                resultCompletableFuture = future.thenAccept(data -> ui.access(() -> resultHandler.accept(data)));
            } else {
                resultCompletableFuture = future.thenAccept(t -> {
                });
            }

            configureExceptionHandler(resultCompletableFuture);
            configureTimeout(resultCompletableFuture);

            return resultCompletableFuture;
        }
    }

    public class RunAsyncBuilder extends AbstractAsyncTaskBuilder {

        private Runnable asyncTask;
        private Runnable resultHandler;

        public RunAsyncBuilder(Runnable asyncTask) {
            super();
            this.asyncTask = asyncTask;
        }

        /**
         * Sets a handler that is called when the asynchronous task completes successfully. The handler can safely
         * update Vaadin UI components.
         */
        public RunAsyncBuilder withResultHandler(Runnable resultHandler) {
            this.resultHandler = resultHandler;
            return this;
        }

        /**
         * Sets a handler that is called when the asynchronous task throws an exception. The handler can safely update
         * Vaadin UI components. If not set, then the default exception handler will be used.
         */
        public RunAsyncBuilder withExceptionHandler(Consumer<Throwable> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * Sets the timeout for the asynchronous task. If the task is not completed within the specified timeout, a
         * {@link TimeoutException} is thrown.
         */
        public RunAsyncBuilder withTimeout(int timeout, TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        /**
         * Configures and returns a {@link CompletableFuture} that asynchronously executes the task.
         */
        public CompletableFuture<Void> runAsync() {
            DelegatingSecurityRunnable wrappedRunnable = new DelegatingSecurityRunnable(asyncTask);
            CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(wrappedRunnable, executorService);
            if (resultHandler != null) {
                completableFuture = completableFuture.thenRun(() -> ui.access(resultHandler::run));
            }
            configureExceptionHandler(completableFuture);
            configureTimeout(completableFuture);
            return completableFuture;
        }
    }

    protected Function<Throwable, Void> createDefaultExceptionHandler() {
        return throwable -> {
            if (throwable instanceof TimeoutException) {
                log.error("UI async task finished on timeout");
            } else {
                log.error("UI async task error", throwable);
            }
            return null;
        };
    }

    public void setDefaultExceptionHandler(Function<Throwable, Void> defaultExceptionHandler) {
        this.defaultExceptionHandler = defaultExceptionHandler;
    }
}
