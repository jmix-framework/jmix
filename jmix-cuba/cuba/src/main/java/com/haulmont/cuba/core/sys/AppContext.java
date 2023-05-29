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
package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import com.haulmont.cuba.core.global.Events;
import io.jmix.core.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nullable;

/**
 * System-level class with static methods providing access to some central application structures:
 * <ul>
 *     <li>Spring's {@link ApplicationContext}</li>
 *     <li>Spring's {@code Environment} properties</li>
 * </ul>
 * It also provides methods {@link #isStarted()} and {@link #isReady()} to check whether the app is fully initialized at the moment.
 */
public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    private static ApplicationContext context;

    private static volatile boolean started;
    private static volatile boolean listenersNotified;

    /**
     * INTERNAL.
     * Used by other framework classes to get access Spring's context.
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * @return all property names defined in the set of {@code app.properties} files
     */
    public static String[] getPropertyNames() {
        return getAppProperties().getPropertyNames();
    }

    /**
     * Get property value defined in the set of {@code app.properties} files.
     * @param key   property key
     * @return      property value or null if the key is not found
     */
    @Nullable
    public static String getProperty(String key) {
        return getAppProperties().getProperty(key);
    }

    /**
     * Set property value. The new value will be accessible at the runtime through {@link #getProperty(String)} and
     * {@link #getPropertyNames()}, but will not be saved in any {@code app.properties} file and will be lost
     * after the application restart.
     * @param key       property key
     * @param value     property value. If null, the property will be removed.
     */
    public static void setProperty(String key, @Nullable String value) {
        getAppProperties().setProperty(key, value);
    }

    /**
     * @return true if the application context is initialized
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * @return true if the application context is initialized and all listeners have been notified
     */
    public static boolean isReady() {
        return started && listenersNotified;
    }

    private static AppProperties getAppProperties() {
        return context.getBean(AppProperties.NAME, AppProperties.class);
    }

    /**
     * INTERNAL.
     * Contains methods for setting up AppContext internals.
     */
    @Internal
    public static class Internals {

        /**
         * Called by the framework to set Spring's context.
         *
         * @param applicationContext initialized Spring's context
         */
        public static void setApplicationContext(@Nullable ApplicationContext applicationContext) {
            setApplicationContext(applicationContext, true);
        }

        /**
         * Called by the framework to set Spring's context.
         *
         * @param applicationContext initialized Spring's context
         * @param publishEvent - fire AppContextInitializedEvent event if true
         */
        public static void setApplicationContext(@Nullable ApplicationContext applicationContext, boolean publishEvent) {
            AppBeans.setApplicationContext(applicationContext);
            context = applicationContext;
            if (publishEvent) {
                Events events = getApplicationContext().getBean(Events.NAME, Events.class);
                events.publish(new AppContextInitializedEvent(context));
            }
        }

        /**
         * Called by the framework after the application has been started and fully initialized.
         */
        public static void startContext() {
            started = true;

            Events events = getApplicationContext().getBean(Events.NAME, Events.class);
            events.publish(new AppContextStartedEvent(context));

            listenersNotified = true;
        }

        /**
         * Called by the framework right before the application shutdown.
         */
        public static void stopContext() {
            started = false;

            Events events = getApplicationContext().getBean(Events.NAME, Events.class);
            events.publish(new AppContextStoppedEvent(context));

            if (context instanceof ConfigurableApplicationContext) {
                ((ConfigurableApplicationContext) context).close();
            }
        }

        public static void onContextClosed(ApplicationContext applicationContext) {
            if (started && applicationContext == context) {
                Events events = getApplicationContext().getBean(Events.NAME, Events.class);
                events.publish(new AppContextStoppedEvent(context));

                started = false;
                listenersNotified = false;
                context = null;
            }
        }
    }

    public interface SecuredOperation<T> {
        T call();
    }
}