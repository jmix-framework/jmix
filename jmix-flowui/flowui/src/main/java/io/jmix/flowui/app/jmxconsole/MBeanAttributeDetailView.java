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

package io.jmix.flowui.app.jmxconsole;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "system/mbeanattr/:id", layout = DefaultMainViewParent.class)
@ViewController("ui_ManagedBeanAttribute.detail")
@ViewDescriptor("mbean-attribute-detail-view.xml")
@EditedEntityContainer("attrDc")
public class MBeanAttributeDetailView extends StandardDetailView<ManagedBeanAttribute> {
    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected AttributeComponentProvider attributeComponentProvider;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected Div valueContainerDiv;
    private AbstractField attributeField;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ManagedBeanAttribute managedBeanAttribute = getEditedEntity();

        jmxControl.loadAttributeValue(managedBeanAttribute);

        attributeField = attributeComponentProvider
                .withValue(managedBeanAttribute.getValue())
                .withType(managedBeanAttribute.getType())
                .withFixedSize(true)
                .requestFocus(true)
                .build();
        if (attributeField instanceof HasLabel) {
            ((HasLabel) attributeField).setLabel("Value");
        }
        valueContainerDiv.add(attributeField);
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (!assignValue()) {
            event.preventSave();
        }
    }

    protected boolean assignValue() {
        ManagedBeanAttribute managedBeanAttribute = getEditedEntity();

        Object oldValue = managedBeanAttribute.getValue();
        try {
            Object newValue = attributeField.getValue() != null ? attributeComponentProvider.getFieldConvertedValue(attributeField, false) : null;
            if (newValue != null) {
                if (!Objects.equals(managedBeanAttribute.getValue(), newValue)) {
                    managedBeanAttribute.setValue(newValue);
                    jmxControl.saveAttributeValue(managedBeanAttribute);
                }
                return true;
            }
        } catch (Exception e) {
            dialogs.createMessageDialog()
                    .withHeader(messageBundle.formatMessage("editAttribute.exception", managedBeanAttribute.getName()))
                    .withText(e.getClass().getCanonicalName() + " " + e.getMessage() + "\n")
                    .open();
            managedBeanAttribute.setValue(oldValue);
            return false;
        }
        notifications.create(messageBundle.getMessage("editAttribute.conversionError"))
                .withType(Notifications.Type.DEFAULT)
                .show();

        return false;
    }

    @Override
    public String getPageTitle() {
        if (getEditedEntity().getName() != null) {
            return messageBundle.formatMessage("editAttribute.caption.format", getEditedEntity().getName());
        } else {
            return super.getPageTitle();
        }
    }
}
