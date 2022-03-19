/*
 * Copyright 2021 Haulmont.
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

package io.jmix.pivottable.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines value of PivotTable's {@code unusedPropertiesVertical} property.
 * <p>
 * Controls whether or not unused attributes are shown vertically
 * instead of the default which is horizontally. {@code true} means
 * always vertical, {@code false} means always horizontal. If set to
 * a number (as is the default) then if the attributes' names' combined
 * length in characters exceeds the number then the attributes will be shown vertically.
 */
public class UnusedPropertiesVertical extends AbstractPivotObject {
    private static final long serialVersionUID = 5909071295711863164L;

    private Integer intVal;

    private Boolean boolVal;

    public UnusedPropertiesVertical(Integer intVal) {
        this.intVal = intVal;
    }

    public UnusedPropertiesVertical(Boolean boolVal) {
        this.boolVal = boolVal;
    }

    /**
     * @return if the attributes' names' combined length in characters exceeds
     * this number then the attributes will be shown vertically
     */
    public Integer getIntVal() {
        return intVal;
    }

    /**
     * @return {@code true} means always vertical, {@code false} means always horizontal
     */
    public Boolean getBoolVal() {
        return boolVal;
    }

    public static UnusedPropertiesVertical valueOf(String value) {
        try {
            return new UnusedPropertiesVertical(Integer.valueOf(value));
        } catch (NumberFormatException ex) {
            return new UnusedPropertiesVertical(Boolean.valueOf(value));
        }
    }

    public static UnusedPropertiesVertical valueOf(int value) {
        return new UnusedPropertiesVertical(value);
    }

    public static UnusedPropertiesVertical valueOf(boolean value) {
        return new UnusedPropertiesVertical(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UnusedPropertiesVertical that = (UnusedPropertiesVertical) o;

        return (intVal != null ? intVal.equals(that.intVal) : that.intVal == null)
                && (boolVal != null ? boolVal.equals(that.boolVal) : that.boolVal == null);
    }

    @Override
    public int hashCode() {
        int result = intVal != null ? intVal.hashCode() : 0;
        result = 31 * result + (boolVal != null ? boolVal.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return intVal != null ? intVal.toString() : (boolVal != null ? boolVal.toString() : StringUtils.EMPTY);
    }
}
