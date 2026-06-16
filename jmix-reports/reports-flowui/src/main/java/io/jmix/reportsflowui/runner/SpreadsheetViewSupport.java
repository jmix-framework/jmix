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

package io.jmix.reportsflowui.runner;

import io.jmix.core.FileRef;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Optional contract for UI views to check spreadsheet rendering availability and open stored report outputs.
 * Implemented by the premium spreadsheet module and injected as an optional bean.
 */
@Internal
@NullMarked
public interface SpreadsheetViewSupport {

    /**
     * @return whether the given stored file can be opened in a spreadsheet viewer
     */
    boolean supportsFileRef(@Nullable FileRef fileRef);

    /**
     * Opens the stored file in the spreadsheet viewer relative to the given owner view.
     *
     * @return {@code true} if the file was successfully opened, {@code false} otherwise
     */
    boolean open(@Nullable View<?> owner, FileRef fileRef);
}
