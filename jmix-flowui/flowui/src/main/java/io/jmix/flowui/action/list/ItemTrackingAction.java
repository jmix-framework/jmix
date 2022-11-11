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

package io.jmix.flowui.action.list;

import io.jmix.flowui.action.ActionType;

@ActionType(ItemTrackingAction.ID)
public class ItemTrackingAction<E> extends SecuredListDataComponentAction<ItemTrackingAction<E>, E> {

    public static final String ID = "itemTracking";

    public ItemTrackingAction() {
        this(ID);
    }

    public ItemTrackingAction(String id) {
        super(id);
    }

    @Override
    public void execute() {
        // do nothing
    }
}
