/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import io.jmix.core.common.event.Subscription;

import java.util.function.Consumer;

/**
 * @deprecated Use {@link io.jmix.ui.component.UploadField} instead
 */
@Deprecated
public interface UploadField extends io.jmix.ui.component.UploadField {

    /**
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeFileUploadStartListener(Consumer<FileUploadStartEvent> listener);

    /**
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener);

    /**
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener);
}
