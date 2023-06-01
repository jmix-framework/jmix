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

package io.jmix.ui.action;

import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.meta.StudioAction;

/**
 * Standard action that changes enabled property depending on selection of a bound {@link ListComponent}.
 * <br>
 * You can use fluent API to create instances of ItemTrackingAction and assign handlers to them:
 * <pre>{@code
 *     Action action = actions.create(ItemTrackingAction.ID, "moveToTrash")
 *             .withCaption("Move to trash")
 *             .withIcon(JmixIcon.TRASH_O.source())
 *             .withHandler(event -> {
 *                 // action logic here
 *             });
 *     docsTable.addAction(action);
 * }</pre>
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Tracks the selected item from the bound ListComponent"
)
@ActionType(ItemTrackingAction.ID)
public class ItemTrackingAction extends SecuredListAction {

    public static final String ID = "itemTracking";

    public ItemTrackingAction() {
        this(ID);
    }

    public ItemTrackingAction(String id) {
        super(id);
    }
}
