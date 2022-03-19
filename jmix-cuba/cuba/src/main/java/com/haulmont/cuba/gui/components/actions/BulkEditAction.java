/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.gui.components.actions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.BulkEditor;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.security.entity.ConstraintOperationType;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.security.EntityOp;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Component;
import io.jmix.ui.app.bulk.ColumnsMode;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import org.springframework.context.annotation.Scope;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;

/**
 * Action used in {@link BulkEditor} visual component.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in {@code web-spring.xml},
 * for example:
 * <pre>
 * &lt;bean id="cuba_BulkEditAction" class="com.company.sample.gui.MyBulkEditAction" scope="prototype"/&gt;
 * </pre>
 */
@org.springframework.stereotype.Component("cuba_BulkEditAction")
@Scope("prototype")
public class BulkEditAction extends ItemTrackingAction
        implements HasBeforeActionPerformedHandler {

    protected OpenType openType = OpenType.DIALOG;
    protected String exclude;
    protected List<String> includeProperties = Collections.emptyList();
    protected Map<String, Field.Validator> fieldValidators;
    protected List<Field.Validator> modelValidators;
    protected Boolean loadDynamicAttributes;
    protected Boolean useConfirmDialog;
    protected ColumnsMode columnsMode;

    protected BeforeActionPerformedHandler beforeActionPerformedHandler;

    /**
     * Creates an action with default id.
     *
     * @param target component containing this action
     */
    public static BulkEditAction create(ListComponent target) {
        return AppBeans.getPrototype("cuba_BulkEditAction", target);
    }

    public BulkEditAction(ListComponent target) {
        super(target, "bulkEdit");

        this.icon = AppBeans.get(Icons.class).get(JmixIcon.BULK_EDIT_ACTION);

        Messages messages = AppBeans.get(Messages.class);
        this.caption = messages.getMessage("actions.BulkEdit");
        this.constraintEntityOp = EntityOp.UPDATE;

        Security security = AppBeans.get(Security.class);
        if (!security.isSpecificPermitted(BulkEditor.PERMISSION)) {
            setVisible(false);
            setEnabled(false);
        }
    }

    public OpenType getOpenType() {
        return openType;
    }

    public void setOpenType(OpenType openType) {
        this.openType = openType;
    }

    public String getExcludePropertyRegex() {
        return exclude;
    }

    public void setExcludePropertyRegex(String exclude) {
        this.exclude = exclude;
    }

    public List<String> getIncludeProperties() {
        return includeProperties;
    }

    public void setIncludeProperties(List<String> includeProperties) {
        this.includeProperties = includeProperties;
    }

    public List<Field.Validator> getModelValidators() {
        return modelValidators;
    }

    public void setModelValidators(List<Field.Validator> modelValidators) {
        this.modelValidators = modelValidators;
    }

    public Map<String, Field.Validator> getFieldValidators() {
        return fieldValidators;
    }

    public void setFieldValidators(Map<String, Field.Validator> fieldValidators) {
        this.fieldValidators = fieldValidators;
    }

    public Boolean getLoadDynamicAttributes() {
        return loadDynamicAttributes;
    }

    public void setLoadDynamicAttributes(Boolean loadDynamicAttribute) {
        this.loadDynamicAttributes = loadDynamicAttribute;
    }

    public void setUseConfirmDialog(Boolean useConfirmDialog) {
        this.useConfirmDialog = useConfirmDialog;
    }

    public Boolean getUseConfirmDialog() {
        return useConfirmDialog;
    }

    public ColumnsMode getColumnsMode() {
        return columnsMode;
    }

    public void setColumnsMode(ColumnsMode columnsMode) {
        this.columnsMode = columnsMode;
    }

    @Override
    public void actionPerform(Component component) {
        if (beforeActionPerformedHandler != null
                && !beforeActionPerformedHandler.beforeActionPerformed()) {
            return;
        }

        Security security = AppBeans.get(Security.class);
        if (!security.isSpecificPermitted(BulkEditor.PERMISSION)) {
            Messages messages = AppBeans.get(Messages.class);

            Notifications notifications = getScreenContext(target.getFrame()).getNotifications();
            notifications.create(NotificationType.ERROR)
                    .withCaption(messages.getMessage("accessDenied.message"))
                    .show();
            return;
        }

        if (target.getSelected().isEmpty()) {
            Messages messages = AppBeans.get(Messages.class);

            Notifications notifications = getScreenContext(target.getFrame()).getNotifications();
            notifications.create(NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("actions.BulkEdit.emptySelection"))
                    .show();

            return;
        }

        OpenType openType = this.openType;

        if (openType.getOpenMode() == OpenMode.DIALOG) {
            ThemeConstantsManager themeManager = AppBeans.get(ThemeConstantsManager.class);
            ThemeConstants theme = themeManager.getConstants();

            openType = openType.copy()
                    .width(theme.get("cuba.gui.BulkEditAction.editorDialog.width"))
                    .height(theme.get("cuba.gui.BulkEditAction.editorDialog.height"))
                    .resizable(true);
        }

        Map<String, Object> params = ParamsMap.of()
                .pair("metaClass", target.getDatasource().getMetaClass())
                .pair("selected", target.getSelected())
                .pair("exclude", exclude)
                .pair("includeProperties", includeProperties != null ? includeProperties : Collections.EMPTY_LIST)
                .pair("fieldValidators", fieldValidators)
                .pair("modelValidators", modelValidators)
                .pair("loadDynamicAttributes", loadDynamicAttributes)
                .pair("useConfirmDialog", useConfirmDialog)
                .pair("columnsMode", columnsMode)
                .create();

        WindowManager wm = ((WindowManager) getScreenContext(target.getFrame()).getScreens());
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("bulkEditor");

        Window bulkEditor = wm.openWindow(windowInfo, openType, params);
        bulkEditor.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                target.getDatasource().refresh();
            }
            if (target instanceof Component.Focusable) {
                ((Component.Focusable) target).focus();
            }
        });
    }

    @Override
    public BeforeActionPerformedHandler getBeforeActionPerformedHandler() {
        return beforeActionPerformedHandler;
    }

    @Override
    public void setBeforeActionPerformedHandler(BeforeActionPerformedHandler handler) {
        beforeActionPerformedHandler = handler;
    }
}
