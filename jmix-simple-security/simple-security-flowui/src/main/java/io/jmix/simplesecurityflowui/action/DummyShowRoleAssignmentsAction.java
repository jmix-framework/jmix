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

package io.jmix.simplesecurityflowui.action;

import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;

/**
 * Action is used in auto-generated user list view. It has the same id as the "ShowRoleAssignmentsAction" from the
 * regular security module. The current action is always hidden and its only purpose is to make sure the auto-generated
 * users list view is opened without any errors. When the simple-security add-on is replaced with the regular security,
 * then the actual role assignment action will be transparently used instead of this one.
 */
@ActionType(DummyShowRoleAssignmentsAction.ID)
public class DummyShowRoleAssignmentsAction<E> extends ListDataComponentAction<DummyShowRoleAssignmentsAction<E>, E> {

    public static final String ID = "sec_showRoleAssignments";

    public DummyShowRoleAssignmentsAction() {
        this(ID);
    }

    public DummyShowRoleAssignmentsAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();
        setVisible(false);
    }

    @Override
    public void execute() {
    }
}
