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

package io.jmix.imapui.screen.mailbox;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.imap.AvailableBeansProvider;
import io.jmix.imap.entity.*;
import io.jmix.imap.exception.ImapException;
import io.jmix.imapui.screen.folder.event.ImapEventHandlersFragment;
import io.jmix.imapui.screen.mailbox.helper.FolderRefresher;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@UiController("imap_MailBox.edit")
@UiDescriptor("imap-mail-box-edit.xml")
@EditedEntityContainer("mailBoxDc")
public class ImapMailBoxEdit extends StandardEditor<ImapMailBox> {

    private final static Logger log = LoggerFactory.getLogger(ImapMailBoxEdit.class);

    @Autowired
    protected InstanceContainer<ImapMailBox> mailBoxDc;

    @Autowired
    protected AvailableBeansProvider availableBeansProvider;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected CheckBox useProxyChkBox;

    @Autowired
    protected Form proxyParams;

    @Autowired
    protected TextField<String> proxyHostField;

    @Autowired
    protected TextField<Integer> proxyPortField;

    @Autowired
    protected CheckBox webProxyChkBox;

    @Autowired
    protected CheckBox useTrashFolderChkBox;

    @Autowired
    protected CheckBox useCustomEventsGeneratorChkBox;

    @Autowired
    protected ComboBox<String> customEventsGeneratorClassLookup;

    @Autowired
    protected TextField<String> jmixFlagTextField;

    @Autowired
    protected EntityPicker<ImapFolder> trashFolderEntityPicker;

    @Autowired
    protected Button checkConnectionBtn;

    @Autowired
    protected EntityStates entityStates;

    protected boolean connectionEstablished = true;

    @Autowired
    protected FileStorageUploadField mailBoxRootCertificateField;

    @Autowired
    protected TreeTable<ImapFolder> foldersTable;

    @Autowired
    protected FolderRefresher folderRefresher;

    @Autowired
    protected CollectionContainer<ImapFolder> foldersDc;

    @Autowired
    protected CollectionContainer<ImapFolderEvent> eventsDc;

    @Autowired
    protected UiComponents componentsFactory;

    @Autowired
    protected CollectionContainer<ImapEventHandler> handlersDc;

    @Autowired
    protected SplitPanel foldersPane;

    @Autowired
    protected DataLoader mailBoxDl;

    @Autowired
    protected DataContext dataContext;

    @Autowired
    protected ImapEventHandlersFragment handlersFragment;

    protected boolean rootCertificateUploaded = false;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage fileStorage;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        mailBoxDl.load();

        ImapMailBox mailBox = getEditedEntity();

        mailBoxRootCertificateField.setVisible(mailBox.getSecureMode() != null);

        setEventGeneratorControls();
        useProxyChkBox.setValue(mailBox.getProxy() != null);
        setProxyParamsVisible(mailBox.getProxy() != null);

        if (entityStates.isNew(mailBox)) {
            foldersPane.setVisible(false);
            setEnableForButtons(false);
        } else {
            checkConnectionBtn.setVisible(false);
            setEnableForButtons(true);
        }

        handlersFragment.refresh();
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        setTrashFolderControls();
    }

    @Subscribe("mailBoxRootCertificateField")
    protected void onMailBoxRootCertificateFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        rootCertificateUploaded = true;
    }

    @Subscribe("checkConnectionBtn")
    public void checkConnection(Button.ClickEvent clickEvent) {
        setEnableForButtons(false);
        try {
            refreshFolders();
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("connectionSucceed"))
                    .show();
        } catch (ImapException e) {
            log.error("Connection Error", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messageBundle.getMessage("connectionFailed"))
                    .withDescription(e.getMessage())
                    .show();
        }
    }

    @Install(to = "trashFolderEntityPicker.entity_lookup", subject = "screenOptionsSupplier")
    protected ScreenOptions trashFolderEntityPickerLookupScreenOptionsSupplier() {
        return new MapScreenOptions(ParamsMap.of("mailBox", getEditedEntity()));
    }

    @Subscribe
    protected void onInitEntity(InitEntityEvent<ImapMailBox> event) {
        ImapMailBox item = event.getEntity();
        item.setAuthenticationMethod(ImapAuthenticationMethod.SIMPLE);
        item.setAuthentication(dataContext.create(ImapSimpleAuthentication.class));
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (!connectionEstablished) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("saveWithoutConnectionWarning"))
                    .show();
            event.preventCommit();
        }
        if (rootCertificateUploaded) {
            String fileName = mailBoxRootCertificateField.getFileName();
            if (StringUtils.isNotEmpty(fileName)) {
                if (fileStorage == null) {
                    fileStorage = fileStorageLocator.getDefault();
                }
                FileRef fileRef = fileStorage.saveStream(fileName, mailBoxRootCertificateField.getFileContent());
                getEditedEntity().setRootCertificate(fileRef);
            }
        }
    }

    @Subscribe(id = "mailBoxDc", target = Target.DATA_CONTAINER)
    protected void onMailBoxDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ImapMailBox> event) {
        if (Objects.equals("secureMode", event.getProperty())) {
            mailBoxRootCertificateField.setVisible(event.getValue() != null);
        }
    }

    @Subscribe(id = "eventsDc", target = Target.DATA_CONTAINER)
    protected void onEventsDcItemChangeEvent(InstanceContainer.ItemChangeEvent<ImapFolderEvent> event) {
        handlersFragment.refresh();
    }

    @Subscribe("useTrashFolderChkBox")
    public void useTrashFolderChkBoxValueChanged(HasValue.ValueChangeEvent<Boolean> event) {
        boolean visible = Boolean.TRUE.equals(event.getValue());
        log.debug("Set visibility of trash folder controls for {} to {}", getEditedEntity(), visible);
        trashFolderEntityPicker.setRequired(visible);
        trashFolderEntityPicker.setVisible(visible);

        if (!visible) {
            getEditedEntity().setTrashFolder(null);
            trashFolderEntityPicker.setValue(null);
        }
    }

    @Subscribe("useCustomEventsGeneratorChkBox")
    public void useCustomEventsGeneratorChkBoxValueChanged(HasValue.ValueChangeEvent<Boolean> event) {
        boolean visible = Boolean.TRUE.equals(event.getValue());

        log.debug("Set visibility of custom event generator controls for {} to {}", getEditedEntity(), visible);
        customEventsGeneratorClassLookup.setRequired(visible);
        customEventsGeneratorClassLookup.setVisible(visible);

        if (!visible) {
            getEditedEntity().setEventsGeneratorClass(null);
        }
    }

    @Subscribe("useProxyChkBox")
    public void useProxyCheckBoxValueChanged(HasValue.ValueChangeEvent<Boolean> event) {
        boolean visible = Boolean.TRUE.equals(event.getValue());
        log.debug("Set visibility of proxy folder controls for {} to {}", getEditedEntity(), visible);

        ImapProxy proxy = visible ? metadata.create(ImapProxy.class) : null;
        getEditedEntity().setProxy(proxy);

        setProxyParamsVisible(visible);
    }

    protected void setTrashFolderControls() {
        String trashFolderName = getEditedEntity().getTrashFolderName();
        boolean visible = trashFolderName != null;

        log.debug("Set visibility of trash folder controls for {} to {}", getEditedEntity(), visible);
        trashFolderEntityPicker.setRequired(visible);
        if (visible) {
            trashFolderEntityPicker.setValue(getEditedEntity().getFolders().stream()
                    .filter(f -> f.getName().equals(trashFolderName)).findFirst().orElse(null)
            );
        }
        trashFolderEntityPicker.setVisible(visible);
        useTrashFolderChkBox.setValue(visible);
    }

    protected void setEventGeneratorControls() {
        Map<String, String> availableEventsGenerators = availableBeansProvider.getEventsGenerators();
        String eventsGeneratorClass = getEditedEntity().getEventsGeneratorClass();
        boolean visible = eventsGeneratorClass != null
                && availableEventsGenerators.values().stream().anyMatch(clz -> clz.equals(eventsGeneratorClass));
        log.debug("Set visibility of custom event generator controls for {} to {}", getEditedEntity(), visible);
        customEventsGeneratorClassLookup.setRequired(visible);
        customEventsGeneratorClassLookup.setVisible(visible);
        useCustomEventsGeneratorChkBox.setValue(visible);

        if (eventsGeneratorClass != null && !visible) {
            log.warn("No such bean {} for event generator interface, discard it", eventsGeneratorClass);
            useCustomEventsGeneratorChkBox.setEditable(false);
            useCustomEventsGeneratorChkBox.setEnabled(false);
            getEditedEntity().setEventsGeneratorClass(null);
        } else {
            customEventsGeneratorClassLookup.setOptionsMap(availableEventsGenerators);
        }
    }


    protected void setProxyParamsVisible(boolean visible) {
        proxyHostField.setRequired(visible);
        proxyPortField.setRequired(visible);
        webProxyChkBox.setVisible(visible);
        proxyParams.setVisible(visible);
        proxyParams.getParent().setVisible(visible);
    }

    protected void setEnableForButtons(boolean enable) {
        connectionEstablished = enable;
        trashFolderEntityPicker.setEnabled(enable);
        ImapMailBox mailBox = getEditedEntity();
        boolean supportsFlag = mailBox.getFlagsSupported().equals(Boolean.TRUE);
        jmixFlagTextField.setVisible(supportsFlag);
        jmixFlagTextField.setRequired(supportsFlag);
        if (!supportsFlag) {
            mailBox.setJmixFlag(null);
        }
    }

    @Install(to = "foldersTable.enabled", subject = "columnGenerator")
    public CheckBox foldersTableEnabledColumnGenerator(ImapFolder folder) {
        CheckBox checkBox = componentsFactory.create(CheckBox.class);
        checkBox.setValueSource(new ContainerValueSource(foldersTable.getInstanceContainer(folder), "enabled"));
        checkBox.setEditable(Boolean.TRUE.equals(folder.getCanHoldMessages() && !Boolean.TRUE.equals(folder.getDeleted())));
        checkBox.setFrame(getWindow().getFrame());
        checkBox.setWidth("20");
        return checkBox;
    }

    @Install(to = "foldersTable.name", subject = "columnGenerator")
    public Label<String> foldersTableNameColumnGenerator(ImapFolder folder) {
        Label<String> label = componentsFactory.create(Label.NAME);
        label.setHtmlEnabled(true);

        if (Boolean.TRUE.equals(folder.getDeleted())) {
            label.setValue("<strike>" + folder.getName() + "</strike>");
        } else if (Boolean.TRUE.equals(folder.getUnregistered())) {
            label.setValue("<span>* " + folder.getName() + "</span>");
        } else {
            label.setValue("<span>" + folder.getName() + "</span>");
        }

        return label;
    }

    @Subscribe("foldersTable.refresh")
    public void refreshFolders(Action.ActionPerformedEvent event) {
        refreshFolders();
    }

    protected void refreshFolders() {
        LinkedHashMap<ImapFolder, FolderRefresher.State> foldersWithState = folderRefresher.refresh(mailBoxDc.getItem());
        log.debug("refreshed folders from IMAP: {}", foldersWithState);

        ImapMailBox mailBox = mailBoxDc.getItem();
        List<ImapFolder> folders = mailBox.getFolders();
        List<ImapFolder> trackedFolders = new ArrayList<>();
        foldersWithState.forEach((folder, state) -> {
            switch (state) {
                case NEW:
                    folder.setDeleted(false);
                    folder.setUnregistered(true);
                    break;
                case DELETED:
                    folder.setDeleted(true);
                    folder.setUnregistered(false);
                    break;
                case UNCHANGED:
                    folder.setDeleted(false);
                    folder.setUnregistered(false);
                    break;
            }

            ImapFolder trackedFolder = folder;
            if (!dataContext.contains(folder)) {
                trackedFolder = dataContext.merge(folder);
            }
            trackedFolders.add(trackedFolder);
        });
        if (folders == null) {
            foldersDc.setItems(trackedFolders);
        } else {
            trackedFolders.forEach(imapFolder -> foldersDc.replaceItem(imapFolder));
        }

        setEnableForButtons(true);
        foldersPane.setVisible(true);
    }

    @Subscribe("foldersTable.enableSingle")
    public void enableFolder(Action.ActionPerformedEvent event) {
        ImapFolder folder = foldersTable.getSingleSelected();
        changeSelection(folder, true);
    }

    @Subscribe("foldersTable.enableWithChildren")
    public void enableFolderWithChildren(Action.ActionPerformedEvent event) {
        ImapFolder folder = foldersTable.getSingleSelected();
        changeSelectionWithChildren(folder, true);
    }

    @Subscribe("foldersTable.disableSingle")
    public void disableFolder(Action.ActionPerformedEvent event) {
        ImapFolder folder = foldersTable.getSingleSelected();
        changeSelection(folder, false);
    }

    @Subscribe("foldersTable.disableWithChildren")
    public void disableFolderWithChildren(Action.ActionPerformedEvent event) {
        ImapFolder folder = foldersTable.getSingleSelected();
        changeSelectionWithChildren(folder, false);
    }

    @Subscribe("eventsTable.enableAll")
    public void enableAllEvents(Action.ActionPerformedEvent event) {
        eventsDc.getMutableItems().forEach(imapFolderEvent -> imapFolderEvent.setEnabled(true));
    }

    @Subscribe("eventsTable.disableAll")
    public void disableAllEvents(Action.ActionPerformedEvent event) {
        eventsDc.getMutableItems().forEach(imapFolderEvent -> imapFolderEvent.setEnabled(false));
    }

    protected void changeSelectionWithChildren(ImapFolder folder, boolean selection) {
        changeSelection(folder, selection);
        if (folder != null) {
            for (ImapFolder childFolder : foldersDc.getItems()) {
                if (childFolder != null && folder.equals(childFolder.getParent())) {
                    changeSelectionWithChildren(childFolder, selection);
                }
            }
        }
    }

    protected void changeSelection(ImapFolder folder, boolean selection) {
        if (folder != null
                && Boolean.TRUE.equals(folder.getCanHoldMessages())
                && Boolean.TRUE.equals(folder.getEnabled()) != selection) {

            folder.setEnabled(selection);
            foldersDc.replaceItem(folder);
        }
    }
}
