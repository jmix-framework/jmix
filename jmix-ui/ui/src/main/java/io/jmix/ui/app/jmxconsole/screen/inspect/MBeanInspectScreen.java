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

package io.jmix.ui.app.jmxconsole.screen.inspect;


import io.jmix.core.LoadContext;
import io.jmix.ui.Fragments;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanAttribute;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.ui.app.jmxconsole.screen.inspect.attribute.MBeanAttributeEditor;
import io.jmix.ui.app.jmxconsole.screen.inspect.operation.MBeanOperationFragment;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static io.jmix.ui.app.jmxconsole.AttributeHelper.convertTypeToReadableName;


@UiController("ui_MBeanInspectScreen")
@UiDescriptor("mbean-inspect-screen.xml")
@EditedEntityContainer("mbeanDc")
public class MBeanInspectScreen extends StandardEditor<ManagedBeanInfo> {

    @Autowired
    protected Table<ManagedBeanAttribute> attributesTable;

    @Autowired
    @Qualifier("attributesTable.edit")
    protected Action editAttributeAction;

    @Autowired
    protected JmxControl jmxControl;

    @Autowired
    protected BoxLayout operations;

    @Autowired
    protected CollectionContainer<ManagedBeanAttribute> attrDc;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected CollectionLoader<ManagedBeanAttribute> attrLoader;

    @Autowired
    protected ThemeConstants themeConstants;

    @Subscribe
    public void beforeShowEvent(BeforeShowEvent beforeShowEvent) {
        attributesTable.setItemClickAction(editAttributeAction);

        attrLoader.load();

        if (CollectionUtils.isEmpty(attrDc.getItems())) {
            attributesTable.setHeight(themeConstants.get("jmix.ui.jmxconsole.mbean-inspect.attributesTable.noAttributes.height")); // reduce its height if no attributes
        }

        initOperationsLayout();

        if (getEditedEntity().getObjectName() != null) {
            getWindow().setCaption(messageBundle.formatMessage("caption.format", getEditedEntity().getObjectName()));
        }
    }

    @Install(to = "attributesTable.type", subject = "columnGenerator")
    protected Label<String> attributesTableTypeValueGenerator(ManagedBeanAttribute attribute) {
        Label<String> label = uiComponents.create(Label.NAME);
        label.setValue(convertTypeToReadableName(attribute.getType()));
        return label;
    }

    @Install(to = "attrLoader", target = Target.DATA_LOADER)
    protected List<ManagedBeanAttribute> attrDlLoadDelegate(LoadContext<ManagedBeanAttribute> loadContext) {
        jmxControl.loadAttributes(getEditedEntity());
        return getEditedEntity().getAttributes();
    }

    @Subscribe("closeBtn")
    protected void close(Button.ClickEvent clickEvent) {
        closeWithDiscard();
    }

    @Override
    protected void preventUnsavedChanges(BeforeCloseEvent event) {
    }

    @Install(to = "attributesTable.edit", subject = "enabledRule")
    private boolean attributesTableEditEnabledRule() {
        ManagedBeanAttribute mba = attributesTable.getSingleSelected();
        return mba != null && mba.getWriteable();
    }

    @Subscribe("attributesTable.edit")
    public void editAttribute(Action.ActionPerformedEvent event) {
        ManagedBeanAttribute mba = attributesTable.getSingleSelected();
        if (mba == null) {
            return;
        }
        if (!mba.getWriteable()) {
            return;
        }

        StandardEditor<ManagedBeanAttribute> w = screenBuilders.editor(ManagedBeanAttribute.class, this)
                .withScreenClass(MBeanAttributeEditor.class)
                .withOpenMode(OpenMode.DIALOG)
                .editEntity(mba)
                .build();
        w.addAfterCloseListener(afterCloseEvent -> reloadAttribute(w.getEditedEntity()));
        w.show();
    }

    @Subscribe("attributesTable.refresh")
    public void reloadAttributes(Action.ActionPerformedEvent event) {
        attrLoader.load();
    }

    protected void reloadAttribute(ManagedBeanAttribute attribute) {
        jmxControl.loadAttributeValue(attribute);
        attrDc.replaceItem(attribute);
    }

    protected void initOperationsLayout() {
        ManagedBeanInfo mbean = getEditedEntity();
        if (CollectionUtils.isEmpty(mbean.getOperations())) {
            Label<String> lbl = uiComponents.create(Label.TYPE_DEFAULT);
            lbl.setValue(messageBundle.getMessage("mbean.operations.none"));
            operations.add(lbl);
        } else {
            mbean.getOperations().forEach(managedBeanOperation -> {
                MBeanOperationFragment operationFragment = fragments.create(this, MBeanOperationFragment.class);
                operationFragment.setOperation(managedBeanOperation);
                Fragment fragment = operationFragment.getFragment();
                operations.add(fragment);
            });
        }
    }
}