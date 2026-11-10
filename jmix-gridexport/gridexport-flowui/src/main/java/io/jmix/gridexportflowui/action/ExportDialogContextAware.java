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

import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * An action shown in the option dialog of {@link ExportAction} (see {@link ExportAction#setDialogActions(List)})
 * that needs to know the export the dialog is opened for, e.g. to read the rows the way the export reads them.
 */
@NullMarked
public interface ExportDialogContextAware {

    /**
     * Passes the export the dialog is opened for. Called before the dialog opens, on the action instance created
     * for that dialog.
     *
     * @param context the export the dialog is opened for
     */
    void setExportDialogContext(ExportDialogContext context);
}
