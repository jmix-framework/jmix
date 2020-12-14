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

import io.jmix.core.EntityStates;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.imap.AvailableBeansProvider;
import io.jmix.imap.entity.*;
import io.jmix.imap.exception.ImapException;
import io.jmix.imapui.screen.mailbox.helper.FolderRefresher;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static jdk.nashorn.internal.runtime.ECMAErrors.getMessage;

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
    protected CheckBox useProxyChkBox;

    @Autowired
    protected Form proxyParams;

    @Autowired
    protected TextField<String> proxyHostField;

    @Autowired
    protected TextField<String> proxyPortField;

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
    protected Component mailBoxRootCertificateField;

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
    protected BoxLayout selectedFolderPanel;

    @Autowired
    protected GridLayout editEventsGrid;

    @Autowired
    protected CheckBox allEventsChkBox;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected CollectionContainer<ImapEventHandler> handlersDc;

    @Autowired
    protected SplitPanel foldersPane;

    @Autowired
    protected DataLoader mailBoxLoader;

    @Autowired
    protected DataContext dataContext;

    @Subscribe
    protected void beforeShow(BeforeShowEvent event) {
        mailBoxLoader.load();

        makeEventsInfoColumn();
        setupEvents();

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

    }

    @Subscribe
    protected void afterShow(AfterShowEvent event) {
        setTrashFolderControls();
    }

    @Subscribe("checkConnectionBtn")
    public void checkConnection(Button.ClickEvent clickEvent) {
        setEnableForButtons(false);
        try {
            refreshFolders();
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage(getClass(), "connectionSucceed"))
                    .show();
        } catch (ImapException e) {
            log.error("Connection Error", e);
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "connectionFailed"))
                    .withDescription(e.getMessage())
                    .show();
        }
    }

    @Install(to = "trashFolderEntityPicker.entity_lookup", subject = "screenOptionsSupplier")
    protected ScreenOptions trashFolderEntityPickerLookupScreenOptionsSupplier() {
        return new MapScreenOptions(ParamsMap.of("mailBox", getEditedEntity()));
    }

    @Override
    public void setEntityToEdit(ImapMailBox item) {
        if (entityStates.isNew(item)) {
            item.setAuthenticationMethod(ImapAuthenticationMethod.SIMPLE);
            item.setAuthentication(metadata.create(ImapSimpleAuthentication.class));
        }

        super.setEntityToEdit(item);
    }


    @Subscribe
    protected void beforeCommit(BeforeCommitChangesEvent event) {
        if (!connectionEstablished) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(getMessage("saveWithoutConnectionWarning"))
                    .show();
            event.preventCommit();
        }
    }

    @Subscribe(id = "mailBoxDc", target = Target.DATA_CONTAINER)
    protected void onOrderDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ImapMailBox> event) {
        if (Objects.equals("secureMode", event.getProperty())) {
            mailBoxRootCertificateField.setVisible(event.getValue() != null);
        }
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

    @Install(to = "foldersTable.selected", subject = "columnGenerator")
    public CheckBox foldersTableSelectedColumnGenerator(ImapFolder folder) {
        CheckBox checkBox = componentsFactory.create(CheckBox.class);
        checkBox.setValueSource(new ContainerValueSource(foldersTable.getInstanceContainer(folder), "selected"));
        checkBox.setEditable(Boolean.TRUE.equals(folder.getSelectable() && !Boolean.TRUE.equals(folder.getDisabled())));
        checkBox.setFrame(getWindow().getFrame());
        checkBox.setWidth("20");
        return checkBox;
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
                    folder.setDisabled(false);
                    folder.setUnregistered(true);
                    break;
                case DELETED:
                    folder.setDisabled(true);
                    folder.setUnregistered(false);
                    break;
                case UNCHANGED:
                    folder.setDisabled(false);
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

    @Install(to = "foldersTable.name", subject = "columnGenerator")
    public Label<String> foldersTableNameColumnGenerator(ImapFolder folder) {
        Label<String> label = componentsFactory.create(Label.NAME);
        label.setHtmlEnabled(true);

        if (Boolean.TRUE.equals(folder.getDisabled())) {
            label.setValue("<strike>" + folder.getName() + "</strike>");
        } else if (Boolean.TRUE.equals(folder.getUnregistered())) {
            label.setValue("<span>* " + folder.getName() + "</span>");
        } else {
            label.setValue("<span>" + folder.getName() + "</span>");
        }

        return label;
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
                && Boolean.TRUE.equals(folder.getSelectable())
                && Boolean.TRUE.equals(folder.getSelected()) != selection) {

            folder.setSelected(selection);
            foldersDc.replaceItem(folder);
        }
    }

    protected void setupEvents() {
        ImapEventType[] eventTypes = ImapEventType.values();
        Map<CheckBox, ImapEventType> eventCheckBoxes = new HashMap<>(eventTypes.length);
        AtomicBoolean eventsChanging = new AtomicBoolean(false);
        editEventsGrid.setRows(eventTypes.length + 1);
        for (int i = 0; i < eventTypes.length; i++) {
            ImapEventType eventType = eventTypes[i];
            String eventName = messages.getMessage(eventType);

            Label<String> label = componentsFactory.create(Label.NAME);
            label.setValue(eventName);
            editEventsGrid.add(label, 0, i + 1);

            CheckBox checkBox = componentsFactory.create(CheckBox.class);
            checkBox.setAlignment(Component.Alignment.MIDDLE_CENTER);
            checkBox.setFrame(getWindow().getFrame());
            checkBox.setDescription(eventName);
            checkBox.setId(eventName + "_chkBox");
            checkBox.addValueChangeListener(e -> {
                if (eventsChanging.get()) {
                    return;
                }

                ImapFolder selectedFolder = foldersTable.getSingleSelected();

                if (selectedFolder == null) {
                    return;
                }

                eventsChanging.set(true);
                toggleEvent(Boolean.TRUE.equals(e.getValue()), selectedFolder, eventType);

                allEventsChkBox.setValue(eventCheckBoxes.keySet().stream().allMatch(CheckBox::isChecked));

                eventsChanging.set(false);
            });
            eventCheckBoxes.put(checkBox, eventType);
            editEventsGrid.add(checkBox, 1, i + 1);
        }

        allEventsChkBox.addValueChangeListener(e -> {
            if (eventsChanging.get()) {
                return;
            }

            ImapFolder selectedFolder = foldersTable.getSingleSelected();

            if (selectedFolder == null) {
                return;
            }

            eventsChanging.set(true);
            eventCheckBoxes.forEach((checkbox, eventType) -> {
                Boolean value = e.getValue();
                checkbox.setValue(value);
                toggleEvent(Boolean.TRUE.equals(e.getValue()), selectedFolder, eventType);
            });

            eventsChanging.set(false);
        });

        foldersDc.addItemChangeListener(e -> {
            ImapFolder folder = e.getItem();
            if (!selectedFolderPanel.isVisible() && folder != null) {
                selectedFolderPanel.setVisible(true);
            }
            if (selectedFolderPanel.isVisible() && (folder == null)) {
                selectedFolderPanel.setVisible(false);
            }

            eventsChanging.set(true);

            if (folder != null) {
                eventCheckBoxes.forEach((checkBox, eventType) -> checkBox.setValue(folder.hasEvent(eventType)));
                allEventsChkBox.setValue(eventCheckBoxes.keySet().stream().allMatch(CheckBox::isChecked));
            }

            eventsChanging.set(false);
        });
    }

    protected void toggleEvent(boolean value, ImapFolder imapFolder, ImapEventType eventType) {
        if (value && !imapFolder.hasEvent(eventType)) {
            ImapFolderEvent imapEvent = metadata.create(ImapFolderEvent.class);
            imapEvent.setEvent(eventType);
            imapEvent.setFolder(imapFolder);
            List<ImapFolderEvent> events = imapFolder.getEvents();
            if (events == null) {
                events = new ArrayList<>(ImapEventType.values().length);
                imapFolder.setEvents(events);
            }
            events.add(imapEvent);
            eventsDc.getMutableItems().add(imapEvent);
            foldersDc.replaceItem(imapFolder);
        } else if (!value && imapFolder.hasEvent(eventType)) {
            ImapFolderEvent event = imapFolder.getEvent(eventType);
            imapFolder.getEvents().remove(event);
            eventsDc.getMutableItems().remove(event);
            foldersDc.replaceItem(imapFolder);
        }
    }

    protected void makeEventsInfoColumn() {
        for (ImapEventType eventType : ImapEventType.values()) {
            String message = messages.getMessage(eventType);

            foldersTable.addGeneratedColumn(message, folder -> {
                HBoxLayout hbox = componentsFactory.create(HBoxLayout.class);
                hbox.setWidthFull();
                hbox.setFrame(getWindow().getFrame());
                if (folder.hasEvent(eventType)) {
                    LinkButton button = componentsFactory.create(LinkButton.class);
                    button.setAction(new ImapEventTypeConfigurationAction(eventType));
                    button.setCaption("");
                    button.setIconFromSet(JmixIcon.GEAR);
                    button.setAlignment(Component.Alignment.MIDDLE_LEFT);
                    hbox.add(button);
                }
                return hbox;
            });
        }
    }

    protected class ImapEventTypeConfigurationAction extends BaseAction {

        protected ImapEventType eventType;

        public ImapEventTypeConfigurationAction(ImapEventType eventType) {
            super("event-" + eventType.getId());
            this.eventType = eventType;
        }

        @Override
        public void actionPerform(Component component) {
            ImapFolder selectedFolder = foldersTable.getSingleSelected();
            if (selectedFolder == null) {
                return;
            }

            ImapFolderEvent event = selectedFolder.getEvent(eventType);
            if (event == null) {
                return;
            }

            eventsDc.setItem(event);
            if (event.getEventHandlers() == null) {
                event.setEventHandlers(new ArrayList<>());
            }

            Screen eventEditor = screenBuilders.editor(ImapFolderEvent.class, getWindow().getFrameOwner())
                    .editEntity(event)
                    .withOpenMode(OpenMode.DIALOG)
                    .withContainer(eventsDc)
                    .build()
                    .show();

            eventEditor.addAfterCloseListener(e -> {
                for (int i = 0; i < event.getEventHandlers().size(); i++) {
                    ImapEventHandler handler = event.getEventHandlers().get(i);
                    handler.setHandlingOrder(i);
                    handlersDc.replaceItem(handler);
                }
            });
        }
    }
}
