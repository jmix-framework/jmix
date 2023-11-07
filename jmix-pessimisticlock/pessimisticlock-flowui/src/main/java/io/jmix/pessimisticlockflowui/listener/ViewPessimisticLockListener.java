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

package io.jmix.pessimisticlockflowui.listener;

import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.event.view.ViewClosedEvent;
import io.jmix.flowui.view.LockStatus;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.pessimisticlockflowui.view.PessimisticLockSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component("pslock_ViewPessimisticLockListener")
public class ViewPessimisticLockListener {

    protected ApplicationContext applicationContext;

    public ViewPessimisticLockListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void setupLockListener(StandardDetailView.SetupLockEvent event) {
        StandardDetailView<?> view = event.getSource();
        Object entity = view.getEditedEntity();
        Object entityId = EntityValues.getId(entity);

        LockStatus currentLockStatus = getLockSupport(view).lock(requireNonNull(entityId));
        event.setLockStatus(currentLockStatus);

        if (currentLockStatus == LockStatus.LOCKED) {
            // The lock will be released by the viewClosedListener with navigation case
            if (UiComponentUtils.isComponentAttachedToDialog(view)) {
                view.addDetachListener(__ -> releaseLock(view));
            }
        } else if (currentLockStatus == LockStatus.FAILED) {
            view.setReadOnly(true);
        }
    }

    @EventListener
    public void viewClosedListener(ViewClosedEvent event) {
        releaseLock(event.getSource());
    }

    protected void releaseLock(View<?> view) {
        if (view instanceof StandardDetailView<?> detailView
                && detailView.getLockStatus() == LockStatus.LOCKED) {
            Object entity = detailView.getEditedEntity();
            Object entityId = requireNonNull(EntityValues.getId(entity));

            getLockSupport(detailView).unlock(entityId);
        }
    }

    protected PessimisticLockSupport getLockSupport(StandardDetailView<?> view) {
        Metadata metadata = applicationContext.getBean(Metadata.class);
        MetaClass entityMetaClass = metadata.getClass(view.getEditedEntity());

        return applicationContext.getBean(PessimisticLockSupport.class, view, entityMetaClass);
    }
}
