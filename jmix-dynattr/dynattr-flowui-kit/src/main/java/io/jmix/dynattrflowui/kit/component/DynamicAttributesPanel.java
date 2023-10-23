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

package io.jmix.dynattrflowui.kit.component;

import io.jmix.flowui.model.InstanceContainer;

public interface DynamicAttributesPanel {

    void setInstanceContainer(InstanceContainer<Object> container);

    /**
     * Sets the number of columns. If {@code null} value is passed, columns count will be determined
     * based on the {@code rows} parameter.
     *
     * @param cols positive integer or {@code null}
     */
    void setColumnsCount(Integer cols);

    /**
     * Sets the number of rows. This parameter will only be taken into account if {@code cols == null}.
     *
     * @param rows positive integer or {@code null}
     */
    void setRowsCount(Integer rows);

    /**
     * Sets the width of the fields. This parameter is used if some dynamic attribute does not have own width value.
     *
     * @param fieldWidth width of the fields
     */
    void setFieldWidth(String fieldWidth);


    /**
     * Sets the width of the fields caption. {@code fieldCaptionWidth} with '%' unit is unsupported.
     *
     * @param fieldCaptionWidth width of the fields caption
     */
    void setFieldCaptionWidth(String fieldCaptionWidth);

    /**
     * Sets visibility of the {@code CategoryField} component.
     *
     * @param visible visibility flag
     */
    void setCategoryFieldVisible(boolean visible);

    /**
     * Returns is all fields in form valid (all category fields inside panel)
     *
     * @return is all fields in form valid
     */

    boolean isValid();
}
