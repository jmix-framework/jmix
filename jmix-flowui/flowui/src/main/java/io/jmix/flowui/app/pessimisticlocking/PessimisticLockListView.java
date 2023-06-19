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

package io.jmix.flowui.app.pessimisticlocking;


import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Route(value = "system/pessimistic-locks", layout = DefaultMainViewParent.class)
@ViewController("sys_LockInfo.list")
@ViewDescriptor("pessimistic-lock-list-view.xml")
@DialogMode(width = "50em")
public class PessimisticLockListView extends StandardView {

    @ViewComponent
    protected CollectionContainer<LockInfo> locksDc;

    @ViewComponent
    protected DataGrid<LockInfo> locksTable;

    @Autowired
    protected LockManager service;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MessageTools messageTools;

    @Subscribe
    public void onInit(InitEvent event) {
        locksTable.addColumn(this::lockNameValueProvider)
                .setKey("objectTypeColumn")
                .setHeader(messages.getMessage(this.getClass(), "lockListView.objectType"))
                .setSortable(true);
        List<Grid.Column<LockInfo>> columnsOrder = Arrays.asList(
                locksTable.getColumnByKey("objectTypeColumn"),
                locksTable.getColumnByKey("objectIdColumn"),
                locksTable.getColumnByKey("usernameColumn"),
                locksTable.getColumnByKey("sinceColumn")
        );
        locksTable.setColumnOrder(columnsOrder);
        refresh();
    }
    
    @Subscribe("locksTable.unlock")
    public void onLocksUnlock(ActionPerformedEvent event) {
        LockInfo lockInfo = locksTable.getSingleSelectedItem();
        if (lockInfo != null) {
            service.unlock(lockInfo.getObjectType(), lockInfo.getObjectId());
            refresh();
            if (lockInfo.getObjectId() != null) {
                notifications.create(messages.formatMessage(PessimisticLockListView.class,
                                "hasBeenUnlockedWithId",
                                lockInfo.getObjectType(),
                                lockInfo.getId()))
                        .withPosition(Notification.Position.TOP_END)
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            } else {
                notifications.create(messages.formatMessage(PessimisticLockListView.class,
                                "hasBeenUnlockedWithoutId",
                                lockInfo.getObjectType()))
                        .withPosition(Notification.Position.TOP_END)
                        .withType(Notifications.Type.SUCCESS)
                        .show();
            }
        }
    }

    public String lockNameValueProvider(LockInfo value) {
        MetaClass metaClass = metadata.getSession().getClass(value.getObjectType());
        if (metaClass != null) {
            return messageTools.getEntityCaption(metaClass);
        } else {
            return value.getObjectType();
        }
    }

    protected void refresh() {
        Collection<LockInfo> locks = service.getCurrentLocks();
        locksDc.setItems(locks);
    }

    @Subscribe("locksTable.refresh")
    public void onLocksRefresh(ActionPerformedEvent event) {
        refresh();
    }
}
