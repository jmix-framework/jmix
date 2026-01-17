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

package io.jmix.flowui.kit.component.loginform;

/**
 * POJO class that is supposed to be used as JSON item in {@link EnhancedLoginForm}.
 */
public class LocaleItem {

    String label;
    String value;

    public LocaleItem() {
    }

    public LocaleItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Returns the value associated with this instance.
     *
     * @return the string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value associated with this instance.
     *
     * @param value the new value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the label associated with this instance.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label for this object. The label represents a descriptive
     * text or title associated with the object.
     *
     * @param label the label text to set, must not be null
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
