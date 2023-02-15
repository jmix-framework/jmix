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

package io.jmix.ui.screen;

import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.AddAction;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.action.list.ExcludeAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component("ui_ScreenListComponentValidation")
public class ScreenListComponentValidation {

    @Autowired
    protected Validator validator;

    /**
     * Validate UI List component by performing validation of underlying entity instance
     * and its collection property if it can be affected by source screen.
     *
     * @param listComponent List component to validate
     * @param errors        Validation errors container to fill
     */
    public void validateListComponent(ListComponent<?> listComponent, ValidationErrors errors) {
        if (!listComponent.isVisibleRecursive()
                || !listComponent.isEnabledRecursive()) {
            return;
        }

        DataUnit items = listComponent.getItems();
        if (items instanceof ContainerDataUnit) {
            CollectionContainer<?> itemsContainer = ((ContainerDataUnit<?>) items).getContainer();
            if (itemsContainer instanceof CollectionPropertyContainer) {
                if (!hasListModificationActions(listComponent, itemsContainer.getItems())) {
                    return;
                }

                InstanceContainer<?> masterContainer = ((CollectionPropertyContainer<?>) itemsContainer).getMaster();
                String property = ((CollectionPropertyContainer<?>) itemsContainer).getProperty();
                MetaClass metaClass = masterContainer.getEntityMetaClass();
                Class<?> javaClass = metaClass.getJavaClass();
                Object instance = masterContainer.getItem();
                if (javaClass != KeyValueEntity.class) {
                    Set<ConstraintViolation<Object>> violations = validator.validateProperty(instance, property);
                    violations.forEach(violation -> errors.add(listComponent, violation.getMessage()));
                }
            }
        }
    }

    protected boolean hasListModificationActions(ListComponent<?> listComponent,
                                                 List<?> content) {
        Collection<Action> actions = listComponent.getActions();

        boolean hasAvailableExtendingAction = false;
        boolean hasAvailableReducingAction = false;
        for (Action action : actions) {
            if (!action.isVisible()) {
                continue;
            }
            if (!hasAvailableExtendingAction
                    && isExtendingAction(action)) {
                hasAvailableExtendingAction = action.isEnabled();
            } else if (!hasAvailableReducingAction
                    && isReducingAction(action)) {
                hasAvailableReducingAction = true;
            }
        }

        boolean unableToExtendEmptyContent = !hasAvailableExtendingAction && content.isEmpty();
        boolean noModificationActions = !hasAvailableExtendingAction && !hasAvailableReducingAction;
        boolean ignoreValidation = unableToExtendEmptyContent || noModificationActions;
        return !ignoreValidation;
    }

    protected boolean isExtendingAction(Action action) {
        return action instanceof AddAction
                || action instanceof CreateAction;
    }

    protected boolean isReducingAction(Action action) {
        return action instanceof ExcludeAction
                || action instanceof RemoveAction;
    }
}
