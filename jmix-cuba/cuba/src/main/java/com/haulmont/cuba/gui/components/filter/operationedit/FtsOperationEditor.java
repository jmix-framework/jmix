/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.gui.components.filter.operationedit;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.VBoxLayout;
import com.haulmont.cuba.core.global.filter.Op;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import java.util.List;

/**
 * FTS condition operation editor. Actually does nothing.
 */
public class FtsOperationEditor extends AbstractOperationEditor {

    public FtsOperationEditor(AbstractCondition condition) {
        super(condition);
    }

    @Override
    protected Component createComponent() {
        return AppBeans.get(ComponentsFactory.class).createComponent(VBoxLayout.NAME);
    }

    @Override
    public void setHideOperations(List<Op> hideOperations) {}
}
