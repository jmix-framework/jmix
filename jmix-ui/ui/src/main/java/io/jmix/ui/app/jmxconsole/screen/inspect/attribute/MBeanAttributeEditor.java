/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.app.jmxconsole.screen.inspect.attribute;


import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.ui.component.GridLayout;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;


@UiController("ui_MBeanAttribute.edit")
@UiDescriptor("mbean-attribute-edit.xml")
@EditedEntityContainer("attrDc")
public class MBeanAttributeEditor extends StandardEditor<ManagedBeanAttribute> {

    protected AttributeComponentProvider valueHolder;

    @Autowired
    protected JmxControl jmxControl;

    @Autowired
    protected GridLayout valueContainer;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected ObjectProvider<AttributeComponentProvider> attributeComponentProviders;

    @Subscribe
    protected void beforeShow(BeforeShowEvent beforeShowEvent) {
        ManagedBeanAttribute mba = getEditedEntity();

        jmxControl.loadAttributeValue(mba);

        valueHolder = attributeComponentProviders.getObject()
                .withFrame(getWindow().getFrame())
                .withValue(mba.getValue())
                .withType(mba.getType())
                .withFixedSize(true)
                .requestFocus(true)
                .build();

        valueContainer.add(valueHolder.getComponent(), 1, 0);

        if (mba.getName() != null) {
            getWindow().setCaption(messageBundle.formatMessage("editAttribute.caption.format", mba.getName()));
        }
    }


    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (!assignValue()) {
            event.preventCommit();
        }
    }

    protected boolean assignValue() {
        ManagedBeanAttribute mba = getEditedEntity();

        Object oldValue = mba.getValue();
        try {
            Object newValue = valueHolder != null ? valueHolder.getAttributeValue(false) : null;
            if (newValue != null) {
                if (!Objects.equals(mba.getValue(), newValue)) {
                    mba.setValue(newValue);
                    jmxControl.saveAttributeValue(mba);
                }
                return true;
            }
        } catch (Exception e) {
            String width = themeConstants.get("jmix.ui.jmxconsole.MBeanAttributeEdit.messageDialog.width");

            dialogs.createMessageDialog()
                    .withCaption(messageBundle.formatMessage("editAttribute.exception", mba.getName()))
                    .withMessage(e.getClass().getCanonicalName() + " " + e.getMessage() + "\n")
                    .withWidth(width)
                    .show();
            mba.setValue(oldValue);
            return false;
        }
        notifications.create()
                .withCaption(messageBundle.getMessage("editAttribute.conversionError"))
                .withType(Notifications.NotificationType.HUMANIZED)
                .show();

        return false;
    }
}