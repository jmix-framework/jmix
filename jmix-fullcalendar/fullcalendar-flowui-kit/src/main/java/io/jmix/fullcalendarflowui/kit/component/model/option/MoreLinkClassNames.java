/*
 * Copyright 2024 Haulmont.
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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MoreLinkClassNames implements Serializable {

    protected List<String> classNames;

    protected boolean function;

    public MoreLinkClassNames() {
        this.classNames = Collections.emptyList();
        this.function = false;
    }

    public MoreLinkClassNames(List<String> classNames, boolean function) {
        this.classNames = classNames == null ? Collections.emptyList() : classNames;
        this.function = function;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public boolean isFunction() {
        return function;
    }

    public void setFunction(boolean function) {
        this.function = function;
    }
}
