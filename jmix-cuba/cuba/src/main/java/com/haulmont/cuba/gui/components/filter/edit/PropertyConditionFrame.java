/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter.edit;

import com.haulmont.cuba.core.global.filter.Op;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.PropertyCondition;
import com.haulmont.cuba.gui.components.filter.operationedit.AbstractOperationEditor;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PropertyConditionFrame extends ConditionFrame<PropertyCondition> {

    @Autowired
    protected BoxLayout operationLayout;

    @Autowired
    protected TextField<String> caption;

    @Autowired
    protected TextField property;

    protected Component operationComponent;

    @WindowParam(name = "hideOperations")
    protected List<Op> hideOperations = Collections.emptyList();

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
    }

    @Override
    public void setCondition(PropertyCondition condition) {
        super.setCondition(condition);

        initOperationComponent(condition);

        caption.setValue(condition.getCaption());
        property.setValue(condition.getPropertyLocCaption());

        condition.addListener(new AbstractCondition.Listener() {
            @Override
            public void captionChanged() {
            }

            @Override
            public void paramChanged(Param oldParam, Param newParam) {
                Component oldDefaultValueComponent = defaultValueComponent;
                createDefaultValueComponent();
                if (defaultValueComponent != null && defaultValueComponent instanceof HasValue
                        && oldDefaultValueComponent != null && oldDefaultValueComponent instanceof HasValue) {
                    if (oldParam.getJavaClass().equals(newParam.getJavaClass())
                            && defaultValueComponent.getClass().equals(oldDefaultValueComponent.getClass())) {
                        ((HasValue) defaultValueComponent).setValue(((HasValue) oldDefaultValueComponent).getValue());
                    }
                }
            }
        });
    }

    protected void initOperationComponent(PropertyCondition condition) {
        if (operationComponent != null) {
            operationLayout.remove(operationComponent);
        }
        AbstractOperationEditor abstractOperationEditor = condition.createOperationEditor();
        abstractOperationEditor.setHideOperations(hideOperations);
        operationComponent = abstractOperationEditor.getComponent();

        operationLayout.add(operationComponent);
    }

    @Override
    public boolean commit() {
        if (!super.commit())
            return false;
        condition.setCaption(caption.getValue());
        return true;
    }
}
