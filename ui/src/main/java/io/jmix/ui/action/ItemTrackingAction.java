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

import io.jmix.core.JmixEntity;
import io.jmix.ui.action.list.SecuredListAction;
import io.jmix.ui.component.ListComponent;

import javax.annotation.Nullable;

/**
 * Standard action that changes enabled property depending on selection of a bound {@link ListComponent}.
 * <br>
 * You can use fluent API to create instances of ItemTrackingAction and assign handlers to them:
 * <pre>{@code
 *     Action action = actions.create(ItemTrackingAction.class, "moveToTrash")
 *             .withCaption("Move to trash")
 *             .withIcon("icons/trash.png")
 *             .withHandler(event -> {
 *                 // action logic here
 *             });
 *     docsTable.addAction(action);
 * }</pre>
 */
@ActionType(ItemTrackingAction.ID)
public class ItemTrackingAction extends SecuredListAction {

    public static final String ID = "itemTracking";

    public ItemTrackingAction() {
        this(ID);
    }

    public ItemTrackingAction(String id) {
        this(null, id);
    }

    public ItemTrackingAction(@Nullable ListComponent target, String id) {
        super(id);

        this.target = target;
    }

    @Override
    protected boolean isApplicable() {
        return target != null
                && target.getSingleSelected() != null;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null) {
            return false;
        }

        JmixEntity singleSelected = target.getSingleSelected();
        if (singleSelected == null) {
            return false;
        }

        //TODO: access manager
//        if (constraintOperationType != null) {
//            boolean isPermitted;
//            if (constraintCode != null) {
//                isPermitted = security.isPermitted(singleSelected, constraintCode);
//            } else {
//                isPermitted = security.isPermitted(singleSelected, constraintOperationType);
//            }
//            if (!isPermitted) {
//                return false;
//            }
//        }

        return true;
    }
}
