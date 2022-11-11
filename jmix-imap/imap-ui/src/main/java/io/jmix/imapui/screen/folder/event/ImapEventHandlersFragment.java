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

package io.jmix.imapui.screen.folder.event;

import io.jmix.imap.AvailableBeansProvider;
import io.jmix.imap.entity.ImapEventHandler;
import io.jmix.imap.entity.ImapEventType;
import io.jmix.imap.entity.ImapFolderEvent;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

@UiController("imap_EventHandlersFragment")
@UiDescriptor("imap-event-handlers-fragment.xml")
public class ImapEventHandlersFragment extends ScreenFragment {

    @Autowired
    protected InstanceContainer<ImapFolderEvent> eventsDc;

    @Autowired
    protected CollectionContainer<ImapEventHandler> handlersDc;

    @Autowired
    protected Table<ImapEventHandler> handlersTable;

    @Autowired
    protected Button addHandlerBtn;

    @Autowired
    protected Button upHandlerBtn;

    @Autowired
    protected Button downHandlerBtn;

    @Autowired
    @Qualifier("handlersTable.remove")
    protected RemoveAction<ImapEventHandler> removeAction;

    @Autowired
    protected AvailableBeansProvider availableBeansProvider;

    @Autowired
    protected UiComponents componentsFactory;

    @Autowired
    protected DataContext dataContext;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected MessageBundle messageBundle;

    protected Map<ImapEventHandler, ComboBox> handlerMethodComboBoxFields = Collections.emptyMap();

    protected Map<String, List<String>> availableHandlers = new HashMap<>();

    protected long maxHandlersCount;

    @Autowired
    protected GroupBoxLayout handlersGroupBox;

    public void refresh() {
        ImapFolderEvent folderEvent = eventsDc.getItemOrNull();
        if (folderEvent != null) {
            ImapEventType eventType = folderEvent.getEvent();
            availableHandlers = eventType != null ? availableBeansProvider.getEventHandlers(eventType.getEventClass()) : Collections.emptyMap();
            maxHandlersCount = availableHandlers.values().stream().mapToLong(Collection::size).sum();

            removeMissedHandlers(availableHandlers);
            enableAddButton();

            handlerMethodComboBoxFields = new HashMap<>();
            List<ImapEventHandler> eventHandlers = folderEvent.getEventHandlers();
            if (eventHandlers != null) {
                eventHandlers.forEach(eventHandler -> handlerMethodComboBoxFields.put(eventHandler, makeBeanMethodLookup(availableHandlers, eventHandler)));
            }

            removeAction.setConfirmation(false);
            removeAction.setAfterActionPerformedHandler(e -> updateHandlingOrders());
        } else {
            addHandlerBtn.setEnabled(false);
        }
    }

    @Install(to = "handlersTable.beanName", subject = "columnGenerator")
    protected ComboBox<String> handlersTableBeanNameColumnGenerator(ImapEventHandler eventHandler) {
        ComboBox<String> comboBox = componentsFactory.create(ComboBox.class);
        comboBox.setValueSource(new ContainerValueSource(handlersTable.getInstanceContainer(eventHandler), "beanName"));
        comboBox.setWidth("250px");
        comboBox.setOptionsList(new ArrayList<>(availableHandlers.keySet()));
        return comboBox;
    }

    @Install(to = "handlersTable.methodName", subject = "columnGenerator")
    protected ComboBox<String> handlersTableMethodNameColumnGenerator(ImapEventHandler eventHandler) {
        ComboBox<String> comboBox = handlerMethodComboBoxFields.get(eventHandler);
        comboBox = comboBox != null ? comboBox : makeBeanMethodLookup(availableHandlers, eventHandler);
        comboBox.setValueSource(new ContainerValueSource(handlersTable.getInstanceContainer(eventHandler), "methodName"));
        comboBox.setOptionsList(methodNames(availableHandlers, eventHandler.getBeanName()));
        return comboBox;
    }

    @Subscribe(id = "handlersDc", target = Target.DATA_CONTAINER)
    protected void onHandlersDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ImapEventHandler> event) {
        ImapEventHandler item = event.getItem();
        if (StringUtils.equals("beanName", event.getProperty())) {
            if (event.getValue() != null) {
                String beanName = event.getValue().toString();
                List<String> methods = availableHandlers.get(beanName);
                if (handlersDc.getItems().stream()
                        .filter(eventHandler -> eventHandler != item && beanName.equals(eventHandler.getBeanName()))
                        .count() == methods.size()) {

                    notifications.create(Notifications.NotificationType.HUMANIZED)
                            .withCaption(messageBundle.getMessage("beanNameConflictWarning"))
                            .show();
                    item.setBeanName(event.getPrevValue() != null ? event.getPrevValue().toString() : null);
                } else {
                    handlersDc.replaceItem(item);
                    item.setMethodName(null);
                }

            } else {
                handlersDc.replaceItem(item);
                item.setMethodName(null);
            }
        }
        if (StringUtils.equals("methodName", event.getProperty()) && event.getValue() != null) {
            String methodName = event.getValue().toString();
            handlersDc.getItems().stream()
                    .filter(eventHandler -> eventHandler != item && methodName.equals(eventHandler.getMethodName()) && item.getBeanName().equals(eventHandler.getBeanName()))
                    .findFirst().ifPresent(eventHandler -> {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("methodNameConflictWarning"))
                        .show();
                item.setMethodName(event.getPrevValue() != null ? event.getPrevValue().toString() : null);
            });
        }

    }

    @Subscribe(id = "handlersDc", target = Target.DATA_CONTAINER)
    protected void onHandlersDcCollectionChange(CollectionContainer.CollectionChangeEvent<ImapEventHandler> e) {
        if (e.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
            e.getChanges().forEach(eventHandler -> handlerMethodComboBoxFields.remove(eventHandler));
            enableAddButton();
        } else if (e.getChangeType() == CollectionChangeType.ADD_ITEMS) {
            e.getChanges().forEach(eventHandler -> handlerMethodComboBoxFields.putIfAbsent(eventHandler, makeBeanMethodLookup(availableHandlers, eventHandler)));
            enableAddButton();
        } else if (e.getChangeType() == CollectionChangeType.REFRESH) {
            enableAddButton();
        }
    }

    @Subscribe(id = "handlersDc", target = Target.DATA_CONTAINER)
    protected void onHandlersDcItemChange(InstanceContainer.ItemChangeEvent<ImapEventHandler> e) {
        ImapEventHandler handler = e.getItem();
        if (handler == null) {
            return;
        }
        updateButtons(handler);
    }

    protected void removeMissedHandlers(Map<String, List<String>> availableBeans) {
        List<ImapEventHandler> missedHandlers = handlersDc.getItems().stream()
                .filter(eventHandler ->
                        !availableBeans.containsKey(eventHandler.getBeanName()) ||
                                !availableBeans.get(eventHandler.getBeanName()).contains(eventHandler.getMethodName())
                ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(missedHandlers)) {
            List<String> beanMethods = missedHandlers.stream()
                    .map(handler -> String.format("%s#%s", handler.getBeanName(), handler.getMethodName()))
                    .collect(Collectors.toList());
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.formatMessage("missedHandlersWarning", beanMethods))
                    .show();
            handlersDc.getMutableItems().removeAll(missedHandlers);
        }
    }

    protected ComboBox makeBeanMethodLookup(Map<String, List<String>> availableBeans, ImapEventHandler eventHandler) {
        ComboBox<String> lookup = componentsFactory.create(ComboBox.class);
        lookup.setValueSource(new ContainerValueSource(handlersTable.getInstanceContainer(eventHandler), "methodName"));
        lookup.setWidth("250px");
        String beanName = eventHandler.getBeanName();
        lookup.setFrame(getHostScreen().getWindow());
        lookup.setOptionsList(methodNames(availableBeans, beanName));
        return lookup;
    }

    protected List<String> methodNames(Map<String, List<String>> availableBeans, String beanName) {
        return Optional.ofNullable(beanName)
                .map(availableBeans::get)
                .orElse(Collections.emptyList());
    }

    @Subscribe("handlersTable.add")
    public void addHandler(Action.ActionPerformedEvent event) {
        ImapEventHandler handler = dataContext.create(ImapEventHandler.class);
        handler.setEvent(eventsDc.getItem());
        handler.setHandlingOrder(getMaxHandlingOrder());
        handlersDc.getMutableItems().add(handler);
    }

    @Subscribe("handlersTable.up")
    public void moveUpHandler(Action.ActionPerformedEvent event) {
        ImapEventHandler handler = handlersTable.getSingleSelected();
        if (handler != null) {
            List<ImapEventHandler> eventHandlers = handlersDc.getMutableItems();
            int index = eventHandlers.indexOf(handler);
            if (index != 0) {
                Collections.swap(eventHandlers, index, index - 1);
                updateButtons(handler);
            }
        }
    }

    @Subscribe("handlersTable.down")
    public void moveDownHandler(Action.ActionPerformedEvent event) {
        ImapEventHandler handler = handlersDc.getItem();
        if (handler != null) {
            List<ImapEventHandler> eventHandlers = handlersDc.getMutableItems();
            int index = eventHandlers.indexOf(handler);
            if (index != eventHandlers.size() - 1) {
                Collections.swap(eventHandlers, index, index + 1);
                updateButtons(handler);
            }
        }
    }

    protected void enableAddButton() {
        addHandlerBtn.setEnabled(handlersDc.getItems().size() < maxHandlersCount);
    }

    protected void updateHandlingOrders() {
        for (int i = 0; i < handlersDc.getItems().size(); i++) {
            ImapEventHandler item = handlersDc.getItems().get(i);
            item.setHandlingOrder(i);
            handlersDc.replaceItem(item);
        }
    }

    protected int getMaxHandlingOrder() {
        return CollectionUtils.isNotEmpty(handlersDc.getItems()) ? handlersDc.getItems().size() - 1 : 0;
    }

    protected void updateButtons(ImapEventHandler handler) {
        int index = handlersDc.getItemIndex(handler);
        upHandlerBtn.setEnabled(index > 0);
        downHandlerBtn.setEnabled(index < handlersDc.getItems().size() - 1);
    }
}