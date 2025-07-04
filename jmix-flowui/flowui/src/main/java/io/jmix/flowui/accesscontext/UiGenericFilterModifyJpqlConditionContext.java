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

package io.jmix.flowui.accesscontext;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * Defines an access context for modifying JPQL conditions in a generic filter UI.
 * This context is used to evaluate permissions for operations related to updating
 * JPQL conditions within the application's generic filter functionality.
 */
public class UiGenericFilterModifyJpqlConditionContext extends SpecificOperationAccessContext {

    public static final String NAME = "ui.genericfilter.modifyJpqlCondition";

    public UiGenericFilterModifyJpqlConditionContext() {
        super(NAME);
    }
}
