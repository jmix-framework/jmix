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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.Window;
import io.jmix.ui.component.ActionOwner;
import io.jmix.ui.component.Frame;
import io.jmix.ui.xml.layout.loader.ActionOwnerAssignActionPostInitTask;

/**
 * @deprecated Use {@link ActionOwnerAssignActionPostInitTask} instead
 */
@Deprecated
public class CubaActionOwnerAssignActionPostInitTask extends ActionOwnerAssignActionPostInitTask {
    public CubaActionOwnerAssignActionPostInitTask(ActionOwner component, String actionId, Frame frame) {
        super(component, actionId, frame);
    }

    @Override
    protected String getExceptionMessage(String id) {
        String message = super.getExceptionMessage(id);
        if (Window.Editor.WINDOW_COMMIT.equals(id) || Window.Editor.WINDOW_COMMIT_AND_CLOSE.equals(id)) {
            message += ". This may happen if you are opening an AbstractEditor-based screen by openWindow() method, " +
                    "for example from the main menu. Use openEditor() method or give the screen a name ended " +
                    "with '.edit' to open it as editor from the main menu.";
        }
        return message;
    }
}
