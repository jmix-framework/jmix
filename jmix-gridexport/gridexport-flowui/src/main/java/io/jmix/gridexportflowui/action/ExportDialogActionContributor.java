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

package io.jmix.gridexportflowui.action;

import io.jmix.flowui.kit.action.Action;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Adds actions to the option dialog that {@link ExportAction} opens, e.g. to pass the grid rows somewhere else
 * than a file. Implement it as a Spring bean; contributors are asked in {@link org.springframework.core.Ordered}
 * / {@link org.springframework.core.annotation.Order} order every time the dialog opens, and their actions are
 * shown after the export mode actions and before Cancel.
 */
@NullMarked
public interface ExportDialogActionContributor {

    /**
     * Returns the actions to show in the export dialog that is about to open.
     *
     * @param context the export the dialog is opened for
     * @return actions to add, empty if this contributor has nothing to add for this export
     */
    List<Action> getActions(ExportDialogContext context);
}
