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
package com.haulmont.cuba.web.app.folders;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.core.app.FoldersService;
import com.haulmont.cuba.core.entity.AppFolder;
import com.haulmont.cuba.core.entity.Folder;
import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.widget.JmixSourceCodeEditor;
import io.jmix.ui.widget.addon.aceeditor.AceMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Deprecated
public class AppFolderEditWindow extends FolderEditWindow {

    protected JmixSourceCodeEditor visibilityScriptField = null;
    protected JmixSourceCodeEditor quantityScriptField = null;

    public static FolderEditWindow create(boolean isAppFolder, boolean adding,
                                          Folder folder, TablePresentations presentations, Runnable commitHandler) {
        CubaProperties cubaProperties = AppBeans.get(CubaProperties.class);
        String className = isAppFolder ? cubaProperties.getAppFolderEditWindowClassName()
                : cubaProperties.getFolderEditWindowClassName();

        if (className != null || !isAppFolder) {
            return new FolderEditWindow(adding, folder, presentations, commitHandler);
        } else {
            return new AppFolderEditWindow(adding, (AppFolder) folder, presentations, commitHandler);
        }
    }

    public AppFolderEditWindow(boolean adding, AppFolder folder, TablePresentations presentations, Runnable commitHandler) {
        super(adding, folder, presentations, commitHandler);
        if (!adding) {
            ThemeConstants theme = App.getInstance().getThemeConstants();
            setWidth(theme.get("cuba.web.AppFolderEditWindow.width"));

            layout.setWidth("100%");

            visibilityScriptField = new JmixSourceCodeEditor();
            visibilityScriptField.setMode(AceMode.groovy);
            visibilityScriptField.setWidth(100, Unit.PERCENTAGE);
            visibilityScriptField.setCaption(getMessage("folders.visibilityScript"));
            String vScript = StringUtils.trimToEmpty(folder.getVisibilityScript());
            visibilityScriptField.setValue(vScript);
            layout.addComponent(visibilityScriptField, 3);

            quantityScriptField = new JmixSourceCodeEditor();
            String qScript = StringUtils.trimToEmpty(folder.getQuantityScript());
            quantityScriptField.setValue(qScript);
            quantityScriptField.setMode(AceMode.groovy);
            quantityScriptField.setWidth(100, Unit.PERCENTAGE);
            quantityScriptField.setCaption(getMessage("folders.quantityScript"));
            layout.addComponent(quantityScriptField, 4);

            if (AppUI.getCurrent() != null && AppUI.getCurrent().isTestMode()) {
                this.setJTestId("appFolderEditWindow");

                visibilityScriptField.setJTestId("visibilityScriptField");
                quantityScriptField.setJTestId("quantityScriptField");
            }
        }
    }

    @Override
    protected void initButtonOkListener() {
        okBtn.addClickListener(e -> commit());
    }

    @Override
    protected void commit() {
        AppUI appUI = AppUI.getCurrent();
        if (appUI == null) {
            return;
        }
        Notifications notifications = appUI.getNotifications();

        AppFolder folder = (AppFolder) AppFolderEditWindow.this.folder;
        if (StringUtils.trimToNull(nameField.getValue()) == null) {
            String msg = messages.getMessage(messagesPack, "folders.folderEditWindow.emptyName");
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(msg)
                    .show();
            return;
        }
        folder.setName(nameField.getValue());
        folder.setTabName(tabNameField.getValue());

        if (sortOrderField.getValue() == null || "".equals(sortOrderField.getValue())) {
            folder.setSortOrder(null);
        } else {
            String value = sortOrderField.getValue();
            int sortOrder;
            try {
                sortOrder = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                String msg = messages.getMessage(messagesPack, "folders.folderEditWindow.invalidSortOrder");
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(msg)
                        .show();
                return;
            }
            folder.setSortOrder(sortOrder);
        }

        Object parent = parentSelect.getValue();
        if (parent instanceof Folder) {
            folder.setParent((Folder) parent);
        } else {
            folder.setParent(null);
        }

        if (visibilityScriptField != null) {
            String scriptText = visibilityScriptField.getValue();
            folder.setVisibilityScript(scriptText);
        }
        if (quantityScriptField != null) {
            String scriptText = quantityScriptField.getValue();
            folder.setQuantityScript(scriptText);
        }
        folder.setApplyDefault(Boolean.valueOf(applyDefaultCb.getValue().toString()));

        AppFolderEditWindow.this.commitHandler.run();

        forceClose();
    }

    @Override
    protected void fillParentSelect() {
        parentSelect.removeAllItems();

        String root = getMessage("folders.appFoldersRoot");
        parentSelect.addItem(root);
        parentSelect.setNullSelectionItemId(root);

        FoldersService service = AppBeans.get(FoldersService.NAME);
        List<AppFolder> list = service.loadAppFolders();
        for (AppFolder folder : list) {
            if (!folder.equals(this.folder)) {
                parentSelect.addItem(folder);
                parentSelect.setItemCaption(folder, getMessage(folder.getName()));
            }
        }
    }
}
