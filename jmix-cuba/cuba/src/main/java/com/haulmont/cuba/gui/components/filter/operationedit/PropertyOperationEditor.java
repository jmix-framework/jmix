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
package com.haulmont.cuba.gui.components.filter.operationedit;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.PopupButton;
import com.haulmont.cuba.core.global.filter.Op;
import com.haulmont.cuba.core.global.filter.OpManager;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;

import java.util.List;

/**
 * Operation editor for PropertyCondition. Displays popupButton component for selecting an operation.
 */
public class PropertyOperationEditor extends AbstractOperationEditor {

    protected ComponentsFactory componentsFactory;
    protected PopupButton popupButton;

    public PropertyOperationEditor(AbstractCondition condition) {
        super(condition);
    }

    @Override
    protected Component createComponent() {
        OpManager opManager = AppBeans.get(OpManager.class);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);

        componentsFactory = AppBeans.get(ComponentsFactory.class);
        popupButton = componentsFactory.createComponent(PopupButton.class);

        MetaClass metaClass = condition.getEntityMetaClass();
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(condition.getName());
        if (propertyPath == null) {
            throw new IllegalStateException(String.format("Unable to find property '%s' in entity %s",
                    condition.getName(), metaClass));
        }
        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(propertyPath);
        for (Op op : opManager.availableOps(propertyMetaClass, propertyPath.getMetaProperty())) {
            OperatorChangeAction operatorChangeAction = new OperatorChangeAction(op);
            popupButton.addAction(operatorChangeAction);
        }

        Messages messages = AppBeans.get(Messages.class);
        popupButton.setCaption(messages.getMessage(Op.class, "Op." + condition.getOperator().name()));
        popupButton.setStyleName("condition-operation-button");

        return popupButton;
    }

    @Override
    public void setHideOperations(List<Op> hideOperations) {
        for (Op op : hideOperations) {
            popupButton.removeAction(op.getId());
        }
    }

    protected class OperatorChangeAction extends AbstractAction {
        protected Op op;

        public OperatorChangeAction(Op op) {
            super(op.toString());
            this.op = op;
        }

        @Override
        public void actionPerform(Component component) {
            (PropertyOperationEditor.this.condition).setOperator(op);
            popupButton.setCaption(getCaption());
        }

        @Override
        public String getCaption() {
            Messages messages = AppBeans.get(Messages.class);
            return messages.getMessage(Op.class, "Op." + op.name());
        }
    }
}
