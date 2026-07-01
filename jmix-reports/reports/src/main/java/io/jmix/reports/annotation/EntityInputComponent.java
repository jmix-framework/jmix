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

package io.jmix.reports.annotation;

/**
 * Determines which component is used to the entity input parameter in UI.
 *
 * @see EntityParameterDef
 * @see io.jmix.reports.entity.ParameterType#ENTITY
 */
public enum EntityInputComponent {

    /**
     * EntityComboBox component is used. Options are loaded with a JPQL query.
     */
    OPTION_LIST,

    /**
     * EntityPicker component is used, which opens a lookup view.
     */
    LOOKUP_VIEW
}
