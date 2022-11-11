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

package io.jmix.ui.exception;

import io.jmix.ui.*;

/**
 * Interface to be implemented by UI exception handlers defined.
 */
public interface UiExceptionHandler {

    /**
     * Handle an exception. Implementation class should either handle the exception and return true, or return false
     * to delegate execution to the next handler in the chain of responsibility.
     *
     * @param exception exception instance
     * @param context   UI context
     * @return true if the exception has been successfully handled, false if not
     */
    boolean handle(Throwable exception, UiContext context);

    /**
     * Exception handling context that provides UI infrastructure.
     */
    interface UiContext {
        /**
         * @return screens screens API
         */
        Screens getScreens();

        /**
         * @return dialogs API
         */
        Dialogs getDialogs();

        /**
         * @return notifications API
         */
        Notifications getNotifications();

        /**
         * @return web browser API
         */
        WebBrowserTools getWebBrowserTools();

        /**
         * @return fragments API
         */
        Fragments getFragments();
    }
}