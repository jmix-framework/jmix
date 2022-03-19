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

package io.jmix.ui.screen;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockManager;
import io.jmix.core.pessimisticlocking.LockNotSupported;
import io.jmix.ui.Notifications;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("ui_PessimisticLockingSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PessimisticLockSupport {

    protected Screen screen;

    protected InstanceContainer<?> container;

    @Autowired
    protected LockManager lockManager;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected Messages messages;

    @Autowired
    protected DatatypeFormatter datatypeFormatter;

    public PessimisticLockSupport(Screen screen, InstanceContainer<?> container) {
        this.screen = screen;
        this.container = container;
    }

    public PessimisticLockStatus lock(Object entityId) {
        LockInfo lockInfo = lockManager.lock(getLockName(), entityId.toString());
        if (lockInfo == null) {
            return PessimisticLockStatus.LOCKED;
        } else if (!(lockInfo instanceof LockNotSupported)) {
            UiControllerUtils.getScreenContext(screen).getNotifications().create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("entityLocked.msg"))
                    .withDescription(
                            messages.formatMessage("", "entityLocked.desc",
                                    lockInfo.getUsername(),
                                    datatypeFormatter.formatDateTime(lockInfo.getSince())
                            ))
                    .show();
            return PessimisticLockStatus.FAILED;
        } else {
            return PessimisticLockStatus.NOT_SUPPORTED;
        }
    }

    public void unlock(Object entityId) {
        lockManager.unlock(getLockName(), entityId.toString());
    }

    protected String getLockName() {
        return extendedEntities
                .getOriginalOrThisMetaClass(container.getEntityMetaClass())
                .getName();
    }
}
