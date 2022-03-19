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

package com.haulmont.cuba.gui.components.table;

import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.screen.FrameOwner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @deprecated Use a {@link io.jmix.ui.component.table.LinkCellClickListener} instead
 */
@SuppressWarnings({"rawtypes", "ConstantConditions", "unchecked"})
@Deprecated
public class CubaLinkCellClickListener implements Consumer<io.jmix.ui.component.Table.Column.ClickEvent> {

    protected Table table;
    protected ApplicationContext applicationContext;

    public CubaLinkCellClickListener(Table table, ApplicationContext applicationContext) {
        this.table = table;
        this.applicationContext = applicationContext;
    }

    @Override
    public void accept(Table.Column.ClickEvent clickEvent) {
        if (!clickEvent.isText()) {
            return;
        }

        Table.Column column = (Table.Column) clickEvent.getSource();
        Entity rowItem = (Entity) clickEvent.getItem();

        if (column.getXmlDescriptor() != null) {
            String invokeMethodName = column.getXmlDescriptor().attributeValue("linkInvoke");
            if (StringUtils.isNotEmpty(invokeMethodName)) {
                callControllerInvoke(rowItem, column.getStringId(), invokeMethodName);

                return;
            }
        }

        Entity entity;
        Object value = EntityValues.getValueEx(rowItem, column.getStringId());

        if (value instanceof Entity) {
            entity = (Entity) value;
        } else {
            entity = rowItem;
        }

        WindowManager wm;
        Window window = (Window) CubaComponentsHelper.getWindowImplementation(table);
        if (window == null) {
            throw new IllegalStateException("Please specify Frame for Table");
        } else {
            wm = window.getWindowManager();
        }

        Messages messages = applicationContext.getBean(Messages.class);

        if (entity instanceof SoftDelete && ((SoftDelete) entity).isDeleted()) {
            wm.showNotification(messages.getMainMessage("OpenAction.objectIsDeleted"),
                    Frame.NotificationType.HUMANIZED);
            return;
        }

        if (window.getFrameOwner() instanceof LegacyFrame) {
            LegacyFrame frameOwner = (LegacyFrame) window.getFrameOwner();

            DataSupplier dataSupplier = frameOwner.getDsContext().getDataSupplier();
            entity = dataSupplier.reload(entity, View.MINIMAL);
        } else {
            DataManager dataManager = applicationContext.getBean(DataManager.class);
            entity = dataManager.reload(entity, View.MINIMAL);
        }

        WindowConfig windowConfig = applicationContext.getBean(WindowConfig.class);

        String windowAlias = null;
        if (column.getXmlDescriptor() != null) {
            windowAlias = column.getXmlDescriptor().attributeValue("linkScreen");
        }
        if (StringUtils.isEmpty(windowAlias)) {
            windowAlias = windowConfig.getEditorScreenId(
                    applicationContext.getBean(Metadata.class).getClass(entity));
        }

        WindowManager.OpenType screenOpenType = WindowManager.OpenType.THIS_TAB;
        if (column.getXmlDescriptor() != null) {
            String openTypeAttribute = column.getXmlDescriptor().attributeValue("linkScreenOpenType");
            if (StringUtils.isNotEmpty(openTypeAttribute)) {
                screenOpenType = WindowManager.OpenType.valueOf(openTypeAttribute);
            }
        }

        AbstractEditor editor = (AbstractEditor) wm.openEditor(
                windowConfig.getWindowInfo(windowAlias),
                entity,
                screenOpenType
        );
        editor.addCloseListener(actionId -> {
            // move focus to component
            table.focus();

            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                Entity editorItem = editor.getItem();

                handleEditorCommit(editorItem, rowItem, column.getStringId());
            }
        });
    }

    protected void handleEditorCommit(Entity editorItem, Entity rowItem, String columnId) {
        MetaClass metaClass = applicationContext.getBean(Metadata.class).getClass(rowItem);
        MetaPropertyPath mpp = metaClass.getPropertyPath(columnId);
        if (mpp == null) {
            throw new IllegalStateException(String.format("Unable to find meta property %s for class %s",
                    columnId, metaClass));
        }

        if (mpp.getRange().isClass()) {
            boolean modifiedInTable = false;
            boolean ownerDsModified = false;
            DatasourceImplementation ds = ((DatasourceImplementation) table.getDatasource());
            if (ds != null) {
                modifiedInTable = ds.getItemsToUpdate().contains(rowItem);
                ownerDsModified = ds.isModified();
            }

            EntityValues.setValueEx(rowItem, columnId, null);
            EntityValues.setValueEx(rowItem, columnId, editorItem);

            if (ds != null) {
                // restore modified for owner datasource
                // remove from items to update if it was not modified before setValue
                if (!modifiedInTable) {
                    ds.getItemsToUpdate().remove(rowItem);
                }
                ds.setModified(ownerDsModified);
            }
        } else {
            table.getItems().updateItem(editorItem);
        }
    }

    protected void callControllerInvoke(Entity rowItem, String columnId, String invokeMethodName) {
        FrameOwner controller = table.getFrame().getFrameOwner();
        if (controller instanceof LegacyFragmentAdapter) {
            controller = ((LegacyFragmentAdapter) controller).getRealScreen();
        }

        Method method;
        method = findLinkInvokeMethod(controller.getClass(), invokeMethodName);
        if (method != null) {
            try {
                method.invoke(controller, rowItem, columnId);
            } catch (Exception e) {
                throw new RuntimeException("Unable to cal linkInvoke method for table column", e);
            }
        } else {
            try {
                method = controller.getClass().getMethod(invokeMethodName);
                try {
                    method.invoke(controller);
                } catch (Exception e1) {
                    throw new RuntimeException("Unable to call linkInvoke method for table column", e1);
                }
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException(String.format("No suitable methods named %s for invoke",
                        invokeMethodName));
            }
        }
    }

    protected Method findLinkInvokeMethod(Class cls, String methodName) {
        Method exactMethod = MethodUtils.getAccessibleMethod(cls, methodName, Entity.class, String.class);
        if (exactMethod != null) {
            return exactMethod;
        }

        // search through all methods
        Method[] methods = cls.getMethods();
        for (Method availableMethod : methods) {
            if (availableMethod.getName().equals(methodName)) {
                if (availableMethod.getParameterCount() == 2
                        && Void.TYPE.equals(availableMethod.getReturnType())) {
                    if (Entity.class.isAssignableFrom(availableMethod.getParameterTypes()[0]) &&
                            String.class == availableMethod.getParameterTypes()[1]) {
                        // get accessible version of method
                        return MethodUtils.getAccessibleMethod(availableMethod);
                    }
                }
            }
        }
        return null;
    }
}
