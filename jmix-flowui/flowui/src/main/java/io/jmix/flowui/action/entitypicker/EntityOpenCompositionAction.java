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

package io.jmix.flowui.action.entitypicker;


import io.jmix.flowui.action.ActionType;

/**
 * Opens a one-to-one composition entity using the entity detail view.
 *
 * @param <E> entity type
 */
@ActionType(EntityOpenCompositionAction.ID)
public class EntityOpenCompositionAction<E> extends EntityOpenAction<E> {

    public static final String ID = "entity_openComposition";

    public EntityOpenCompositionAction() {
        super(ID);
    }

    public EntityOpenCompositionAction(String id) {
        super(id);
    }

    @Override
    protected boolean isEmpty() {
        return false;
    }
}
