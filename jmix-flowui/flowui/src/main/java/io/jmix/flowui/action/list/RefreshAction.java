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

package io.jmix.flowui.action.list;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.EmptyDataUnit;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@ActionType(RefreshAction.ID)
public class RefreshAction<E> extends ListDataComponentAction<RefreshAction<E>, E> {

    public static final String ID = "refresh";

    private static final Logger log = LoggerFactory.getLogger(RefreshAction.class);

    public RefreshAction() {
        super(ID);
    }

    public RefreshAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.REFRESH);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Refresh");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        checkTarget();

        if (target.getItems() instanceof EmptyDataUnit) {
            return;
        }

        checkTargetItems(ContainerDataUnit.class);

        // Both target and target.getItems are checked in the methods above
        //noinspection ConstantConditions
        CollectionContainer<E> container = ((ContainerDataUnit<E>) target.getItems()).getContainer();
        if (container == null) {
            throw new IllegalStateException(String.format("%s target is not bound to %s",
                    getClass().getSimpleName(), CollectionContainer.class.getSimpleName()));
        }

        DataLoader loader = null;
        if (container instanceof HasLoader) {
            loader = ((HasLoader) container).getLoader();
        }

        if (loader != null) {
            DataContext dataContext = loader.getDataContext();
            if (dataContext != null) {
                for (Object entity : container.getItems()) {
                    dataContext.evict(entity);
                }
            }
            loader.load();
        } else {
            log.warn("{} '{}' target container has no loader, refresh is impossible",
                    getClass().getSimpleName(), getId());
        }
    }
}
