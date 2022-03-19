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
import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.entity.Folder;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.SearchFolder;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.ComboBox;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.impl.ButtonImpl;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.presentation.model.TablePresentation;
import io.jmix.ui.widget.JmixButton;
import io.jmix.ui.widget.JmixCheckBox;
import io.jmix.ui.widget.JmixWindow;
import io.jmix.ui.widget.ShortcutListenerDelegate;
import io.jmix.uidata.entity.UiTablePresentation;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

@Deprecated
public class FolderEditWindow extends JmixWindow {

    protected Folder folder;
    protected String messagesPack;
    protected TextField nameField;
    protected TextField tabNameField;
    protected ComboBox parentSelect;
    protected TextField sortOrderField;
    protected ComboBox presentation;
    protected CheckBox globalCb;
    protected CheckBox applyDefaultCb;
    protected VerticalLayout layout;
    protected Button okBtn;
    protected Button cancelBtn;
    protected TextField selectedPresentationField;

    protected Messages messages = AppBeans.get(Messages.class);
    protected UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);
    protected Security security = AppBeans.get(Security.NAME);
    protected IconResolver iconResolver = AppBeans.get(IconResolver.class);
    protected CubaProperties cubaProperties = AppBeans.get(CubaProperties.class);

    protected Runnable commitHandler;

    public FolderEditWindow(boolean adding, Folder folder, TablePresentations presentations, Runnable commitHandler) {
        this.folder = folder;
        this.commitHandler = commitHandler;

        setCaption(adding ? getMessage("folders.folderEditWindow.adding") : getMessage("folders.folderEditWindow"));

        setWidthUndefined();
        setResizable(false);

        addAction(new ShortcutListenerDelegate("commit", KeyCode.ENTER, new int[]{ModifierKey.CTRL})
                .withHandler((sender, target) ->
                        commit()
                ));

        layout = new VerticalLayout();
        layout.setWidthUndefined();
        layout.setSpacing(true);
        layout.setMargin(false);

        setContent(layout);
        setModal(true);
        center();

        nameField = new TextField();
        nameField.setRequiredIndicatorVisible(true);
        nameField.setCaption(getMessage("folders.folderEditWindow.nameField"));
        nameField.setWidth("250px");
        nameField.setValue(folder.getName());
        nameField.focus();
        layout.addComponent(nameField);

        tabNameField = new TextField();
        tabNameField.setCaption(getMessage("folders.folderEditWindow.tabNameField"));
        tabNameField.setWidth("250px");
        tabNameField.setValue(StringUtils.trimToEmpty(folder.getTabName()));
        layout.addComponent(tabNameField);

        parentSelect = new ComboBox();
        parentSelect.setCaption(getMessage("folders.folderEditWindow.parentSelect"));
        parentSelect.setWidth("250px");
        parentSelect.setNullSelectionAllowed(true);
        fillParentSelect();
        parentSelect.setValue(folder.getParent());
        layout.addComponent(parentSelect);

        if (folder instanceof SearchFolder) {
            TablePresentation tablePresentation = null;
            if (((SearchFolder) folder).getPresentationId() != null) {
                tablePresentation = presentations.getPresentation(((SearchFolder) folder).getPresentationId());
            }

            if (presentations != null) {
                presentation = new ComboBox();
                presentation.setCaption(getMessage("folders.folderEditWindow.presentation"));
                presentation.setWidth("250px");
                presentation.setNullSelectionAllowed(true);
                fillPresentations(presentations);
                presentation.setValue(tablePresentation);
                layout.addComponent(presentation);
            } else if (tablePresentation != null) {
                selectedPresentationField = new TextField();
                selectedPresentationField.setWidth("250px");
                selectedPresentationField.setCaption(getMessage("folders.folderEditWindow.presentation"));
                selectedPresentationField.setValue(tablePresentation.getName());
                selectedPresentationField.setEnabled(false);
                layout.addComponent(selectedPresentationField);
            }
        }

        sortOrderField = new TextField();
        sortOrderField.setCaption(getMessage("folders.folderEditWindow.sortOrder"));
        sortOrderField.setWidth("250px");
        sortOrderField.setValue(folder.getSortOrder() == null ? "" : folder.getSortOrder().toString());
        layout.addComponent(sortOrderField);

        if (security.isSpecificPermitted("cuba.gui.searchFolder.global")
                && folder instanceof SearchFolder
                && BooleanUtils.isNotTrue(((SearchFolder) folder).getIsSet())) {
            globalCb = new JmixCheckBox(getMessage("folders.folderEditWindow.global"));
            globalCb.setValue(((SearchFolder) folder).getUsername() == null);
            layout.addComponent(globalCb);
        }

        applyDefaultCb = new JmixCheckBox(getMessage("folders.folderEditWindow.applyDefault"));
        applyDefaultCb.setValue(BooleanUtils.isTrue(((AbstractSearchFolder) folder).getApplyDefault()));
        applyDefaultCb.setVisible(cubaProperties.isGenericFilterManualApplyRequired()
                && folder instanceof SearchFolder
                && BooleanUtils.isNotTrue(((SearchFolder) folder).getIsSet()));
        layout.addComponent(applyDefaultCb);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setMargin(new MarginInfo(true, false, false, false));
        buttonsLayout.setSpacing(true);
        layout.addComponent(buttonsLayout);

        okBtn = new JmixButton(getMessage("actions.Ok"));
        okBtn.setIcon(iconResolver.getIconResource("icons/ok.png"));
        okBtn.addStyleName(ButtonImpl.ICON_STYLE);

        initButtonOkListener();
        buttonsLayout.addComponent(okBtn);

        cancelBtn = new JmixButton(getMessage("actions.Cancel"));
        cancelBtn.setIcon(iconResolver.getIconResource("icons/cancel.png"));
        cancelBtn.addClickListener(event ->
                forceClose()
        );

        buttonsLayout.addComponent(cancelBtn);

        if (AppUI.getCurrent().isTestMode()) {
            setJTestId("folderEditWindow");

            nameField.setJTestId("nameField");
            tabNameField.setJTestId("tabNameField");
            parentSelect.setJTestId("parentSelect");
            if (presentation != null) {
                presentation.setJTestId("presentationSelect");
            }
            sortOrderField.setJTestId("sortOrderField");
            if (selectedPresentationField != null) {
                selectedPresentationField.setJTestId("selectedPresentationField");
            }
            if (globalCb != null) {
                globalCb.setJTestId("globalCb");
            }
            applyDefaultCb.setJTestId("applyDefaultCb");
            okBtn.setJTestId("okBtn");
            cancelBtn.setJTestId("cancelBtn");
        }
    }

    protected void initButtonOkListener() {
        okBtn.addClickListener(event ->
                commit()
        );
    }

    protected void commit() {
        AppUI appUI = AppUI.getCurrent();
        if (appUI == null) {
            return;
        }

        Notifications notifications = appUI.getNotifications();
        SearchFolder folder = (SearchFolder) FolderEditWindow.this.folder;
        if (StringUtils.trimToNull(nameField.getValue()) == null) {
            String msg = messages.getMainMessage("folders.folderEditWindow.emptyName");
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
                String msg = messages.getMainMessage("folders.folderEditWindow.invalidSortOrder");
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(msg)
                        .show();
                return;
            }
            folder.setSortOrder(sortOrder);
        }

        Object parent = parentSelect.getValue();
        if (parent instanceof Folder)
            folder.setParent((Folder) parent);
        else
            folder.setParent(null);

        folder.setApplyDefault(Boolean.valueOf(applyDefaultCb.getValue().toString()));
        if (globalCb != null) {
            if (BooleanUtils.isTrue(globalCb.getValue())) {
                folder.setUsername(null);
            } else {
                // todo user substitution
                // folder.setUser(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
                folder.setUsername(userSessionSource.getUserSession().getUser().getUsername());
            }
        } else {
            // todo user substitution
            // folder.setUser(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
            folder.setUsername(userSessionSource.getUserSession().getUser().getUsername());
        }

        if (presentation != null && presentation.getValue() != null) {
            folder.setPresentationId(((UiTablePresentation) presentation.getValue()).getId());
        }

        FolderEditWindow.this.commitHandler.run();

        forceClose();
    }

    protected void fillParentSelect() {
        parentSelect.removeAllItems();

        String root = getMessage("folders.searchFoldersRoot");
        parentSelect.addItem(root);
        parentSelect.setNullSelectionItemId(root);

        FoldersService service = AppBeans.get(FoldersService.NAME);
        List<SearchFolder> list = service.loadSearchFolders();
        for (SearchFolder folder : list) {
            if (!folder.equals(this.folder)) {
                parentSelect.addItem(folder);
                parentSelect.setItemCaption(folder, folder.getCaption());
            }
        }
    }

    protected void fillPresentations(TablePresentations presentations) {
        presentation.removeAllItems();

        Collection<Object> availablePresentationIds = presentations.getPresentationIds();
        for (Object pId : availablePresentationIds) {
            TablePresentation p = presentations.getPresentation(pId);
            presentation.addItem(p);
            presentation.setItemCaption(p, presentations.getCaption(pId));
        }
    }

    protected String getMessage(String key) {
        return messages.getMainMessage(key);
    }
}
