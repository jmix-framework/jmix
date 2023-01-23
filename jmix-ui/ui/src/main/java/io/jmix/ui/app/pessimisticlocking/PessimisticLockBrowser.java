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

package io.jmix.ui.app.pessimisticlocking;


import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

@UiController("sys_LockInfo.browse")
@UiDescriptor("pessimistic-lock-browser.xml")
@Route("pessimisticlocks")
public class PessimisticLockBrowser extends Screen {

    @Autowired
    protected CollectionContainer<LockInfo> locksDc;

    @Autowired
    protected Table<LockInfo> locksTable;

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
        locksTable.getColumn("objectType")
                .setFormatter(this::applyFormatter);
        refresh();
    }
    
    @Subscribe("locksTable.unlock")
    public void onLocksUnlock(Action.ActionPerformedEvent event) {
        LockInfo lockInfo = locksTable.getSingleSelected();
        if (lockInfo != null) {
            service.unlock(lockInfo.getObjectType(), lockInfo.getObjectId());
            refresh();
            if (lockInfo.getObjectId() != null) {
                notifications.create().withCaption(
                        messages.formatMessage(PessimisticLockBrowser.class,
                                "hasBeenUnlockedWithId",
                                lockInfo.getObjectType(),
                                lockInfo.getId()))
                        .show();
            } else {
                notifications.create().withCaption(
                        messages.formatMessage(PessimisticLockBrowser.class,
                        "hasBeenUnlockedWithoutId",
                        lockInfo.getObjectType()))
                        .show();
            }
        }
    }

    protected void refresh() {
        Collection<LockInfo> locks = service.getCurrentLocks();
        locksDc.setItems(locks);
    }

    public String applyFormatter(Object value) {
        MetaClass metaClass = metadata.getSession().getClass((String) value);
        if (metaClass != null) {
            return messageTools.getEntityCaption(metaClass);
        } else {
            return (String) value;
        }
    }

    @Subscribe("locksTable.refresh")
    public void onLocksRefresh(Action.ActionPerformedEvent event) {
        refresh();
    }
}
