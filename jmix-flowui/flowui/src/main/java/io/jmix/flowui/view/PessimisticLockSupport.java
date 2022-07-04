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

package io.jmix.flowui.view;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.core.pessimisticlocking.LockInfo;
import io.jmix.core.pessimisticlocking.LockManager;
import io.jmix.core.pessimisticlocking.LockNotSupported;
import io.jmix.flowui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("flowui_PessimisticLockingSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PessimisticLockSupport {

    protected View view;

    protected InstanceContainer<?> container;

    // TODO: gg, method injection
    @Autowired
    protected LockManager lockManager;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @Autowired
    protected Messages messages;

    @Autowired
    protected DatatypeFormatter datatypeFormatter;

    public PessimisticLockSupport(View view, InstanceContainer<?> container) {
        this.view = view;
        this.container = container;
    }

    public PessimisticLockStatus lock(Object entityId) {
        Preconditions.checkNotNullArgument(entityId);

        LockInfo lockInfo = lockManager.lock(getLockName(), entityId.toString());
        if (lockInfo == null) {
            return PessimisticLockStatus.LOCKED;
        } else if (!(lockInfo instanceof LockNotSupported)) {
            String title = messages.getMessage("entityLocked.title");
            String description = messages.formatMessage("", "entityLocked.message",
                    lockInfo.getUsername(),
                    datatypeFormatter.formatDateTime(lockInfo.getSince()));

            // TODO: gg, use Notifications bean
            Notification notification = new Notification(createNotificationLayout(title, description));
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            notification.setDuration(3000);
            notification.open();

            return PessimisticLockStatus.FAILED;
        } else {
            return PessimisticLockStatus.NOT_SUPPORTED;
        }
    }

    public void unlock(Object entityId) {
        Preconditions.checkNotNullArgument(entityId);

        lockManager.unlock(getLockName(), entityId.toString());
    }

    protected String getLockName() {
        return extendedEntities
                .getOriginalOrThisMetaClass(container.getEntityMetaClass())
                .getName();
    }

    // todo notification rework or restyle
    protected com.vaadin.flow.component.Component createNotificationLayout(String title, String message) {
        Label titleLabel = new Label(title);

        VerticalLayout layout = new VerticalLayout(titleLabel, new Span(message));
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignSelf(FlexComponent.Alignment.CENTER, titleLabel);

        return layout;
    }
}
