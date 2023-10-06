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

package io.jmix.jmxconsole.view;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.*;
import io.jmix.jmxconsole.AttributeComponentProvider;
import io.jmix.jmxconsole.JmxControl;
import io.jmix.jmxconsole.model.ManagedBeanAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "jmxconsole/mbeanattr/:id", layout = DefaultMainViewParent.class)
@ViewController("jmxcon_ManagedBeanAttribute.detail")
@ViewDescriptor("mbean-attribute-detail-view.xml")
@EditedEntityContainer("attrDc")
@DialogMode(width = "35em", resizable = true)
public class MBeanAttributeDetailView extends StandardDetailView<ManagedBeanAttribute> {
    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected AttributeComponentProvider attributeComponentProvider;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;

    protected AbstractField<?, ?> attributeField;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ManagedBeanAttribute managedBeanAttribute = getEditedEntity();

        jmxControl.loadAttributeValue(managedBeanAttribute);

        attributeField = attributeComponentProvider.builder()
                .withValue(managedBeanAttribute.getValue())
                .withType(managedBeanAttribute.getType())
                .withWidth("100%")
                .withMaxWidth("35em")
                .build();

        if (attributeField instanceof HasLabel hasLabelComponent) {
            hasLabelComponent.setLabel("Value");
        }

        getContent().addComponentAsFirst(attributeField);
    }

    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) {
        if (valueNotAssigned()) {
            event.preventSave();
        }
    }

    protected boolean valueNotAssigned() {
        ManagedBeanAttribute managedBeanAttribute = getEditedEntity();

        Object oldValue = managedBeanAttribute.getValue();
        try {
            Object newValue = attributeField.getValue() != null ?
                    attributeComponentProvider.getFieldConvertedValue(attributeField, managedBeanAttribute.getType(),
                            false)
                    : null;
            if (newValue != null) {
                if (!Objects.equals(managedBeanAttribute.getValue(), newValue)) {
                    managedBeanAttribute.setValue(newValue);
                    jmxControl.saveAttributeValue(managedBeanAttribute);
                }
                return false;
            }
        } catch (Exception e) {
            notifications.create(
                            messageBundle.formatMessage("editAttribute.exception", managedBeanAttribute.getName()),
                            e.getClass().getCanonicalName() + " " + e.getMessage() + "\n")
                    .withType(Notifications.Type.ERROR)
                    .show();
            managedBeanAttribute.setValue(oldValue);
            return true;
        }
        notifications.create(messageBundle.getMessage("editAttribute.conversionError"))
                .withType(Notifications.Type.ERROR)
                .show();

        return true;
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
