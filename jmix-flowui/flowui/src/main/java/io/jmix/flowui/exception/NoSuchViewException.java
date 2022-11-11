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
package io.jmix.flowui.exception;

import io.jmix.flowui.view.View;

/**
 * Raised on attempt to open an unknown view.
 */
public class NoSuchViewException extends RuntimeException {

    private final String viewId;

    public NoSuchViewException(String viewId) {
        super(String.format("%s '%s' is not defined", View.class.getSimpleName(), viewId));

        this.viewId = viewId;
    }

    public String getViewId() {
        return viewId;
    }
}