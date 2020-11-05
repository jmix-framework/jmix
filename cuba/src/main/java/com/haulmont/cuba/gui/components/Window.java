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

import com.haulmont.cuba.gui.WindowContext;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import io.jmix.ui.screen.StandardCloseAction;
import io.jmix.ui.util.OperationResult;

import javax.annotation.Nullable;

@Deprecated
public interface Window extends io.jmix.ui.component.Window, Frame {

    @Override
    WindowContext getContext();

    /**
     * Close the screen.
     * <br> If the window has uncommitted changes in its {@link com.haulmont.cuba.gui.data.DsContext},
     * and force=false, the confirmation dialog will be shown.
     *
     * @param actionId action ID that will be propagated to attached {@link CloseListener}s.
     *                 Use {@link #COMMIT_ACTION_ID} if some changes have just been committed, or
     *                 {@link #CLOSE_ACTION_ID} otherwise. These constants are recognized by various mechanisms of the
     *                 framework.
     * @param force    if true, no confirmation dialog will be shown even if the screen has uncommitted changes
     */
    @Deprecated
    default boolean close(String actionId, boolean force) {
        OperationResult result = getFrameOwner().close(new StandardCloseAction(actionId, !force));
        return result.getStatus() == OperationResult.Status.SUCCESS;
    }

    interface HasFoldersPane {
        @Nullable
        FoldersPane getFoldersPane();
    }
}
