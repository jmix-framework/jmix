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

package io.jmix.pessimisticlockflowui.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.view.LockStatus;
import io.jmix.flowui.view.View;
import io.jmix.pessimisticlock.LockManager;
import io.jmix.pessimisticlock.entity.LockInfo;
import io.jmix.pessimisticlock.entity.LockNotSupported;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("pslock_PessimisticLockSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PessimisticLockSupport {

    protected View<?> view;

    protected MetaClass entityMetaClass;

    protected LockManager lockManager;
    protected ExtendedEntities extendedEntities;
    protected Messages messages;
    protected DatatypeFormatter datatypeFormatter;
    protected Notifications notifications;

    public PessimisticLockSupport(View<?> view, MetaClass entityMetaClass) {
        this.view = view;
        this.entityMetaClass = entityMetaClass;
    }

    @Autowired
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Autowired
    public void setExtendedEntities(ExtendedEntities extendedEntities) {
        this.extendedEntities = extendedEntities;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDatatypeFormatter(DatatypeFormatter datatypeFormatter) {
        this.datatypeFormatter = datatypeFormatter;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    public LockStatus lock(Object entityId) {
        Preconditions.checkNotNullArgument(entityId);

        LockInfo lockInfo = lockManager.lock(getLockName(), entityId.toString());
        if (lockInfo == null) {
            return LockStatus.LOCKED;
        } else if (!(lockInfo instanceof LockNotSupported)) {
            String title = messages.getMessage("entityLocked.title");
            String message = messages.formatMessage("", "entityLocked.message",
                    lockInfo.getUsername(),
                    datatypeFormatter.formatDateTime(lockInfo.getSince()));

            notifications.create(title, message)
                    .withPosition(Notification.Position.MIDDLE)
                    .withThemeVariant(NotificationVariant.LUMO_PRIMARY)
                    .withDuration(3000)
                    .show();

            return LockStatus.FAILED;
        } else {
            return LockStatus.NOT_SUPPORTED;
        }
    }

    public void unlock(Object entityId) {
        Preconditions.checkNotNullArgument(entityId);

        lockManager.unlock(getLockName(), entityId.toString());
    }

    protected String getLockName() {
        return extendedEntities
                .getOriginalOrThisMetaClass(entityMetaClass)
                .getName();
    }
}
