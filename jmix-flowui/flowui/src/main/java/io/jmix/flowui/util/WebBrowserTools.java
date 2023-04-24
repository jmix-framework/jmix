/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.util;

import io.jmix.flowui.view.View;

public final class WebBrowserTools {

    public static final String BEFORE_UNLOAD_LISTENER = "jmixBeforeUnloadListener";

    private WebBrowserTools() {
    }

    public static void preventBrowserTabClosing(View<?> view) {
        view.getElement().executeJs(
                "window.addEventListener('beforeunload', " + BEFORE_UNLOAD_LISTENER + ", {capture: true})"
        );
    }

    public static void allowBrowserTabClosing(View<?> view) {
        view.getElement().executeJs(
                "window.removeEventListener('beforeunload', " + BEFORE_UNLOAD_LISTENER + ", {capture: true})"
        );
    }
}
