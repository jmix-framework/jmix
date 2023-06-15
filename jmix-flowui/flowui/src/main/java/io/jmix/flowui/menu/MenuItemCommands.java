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

package io.jmix.flowui.menu;

import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.UI;
import io.jmix.core.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Component("flowui_MenuItemCommands")
public class MenuItemCommands {

    protected DataManager dataManager;
    protected ClassManager classManager;

    protected FetchPlanRepository fetchPlanRepository;

    protected ApplicationContext applicationContext;

    public MenuItemCommands(DataManager dataManager, ClassManager classManager,
                            FetchPlanRepository fetchPlanRepository, ApplicationContext applicationContext) {
        this.dataManager = dataManager;
        this.classManager = classManager;
        this.fetchPlanRepository = fetchPlanRepository;
        this.applicationContext = applicationContext;
    }

    /**
     * Create menu command.
     *
     * @param ui   app ui
     * @param item menu item
     * @return command
     */
    @Nullable
    public MenuItemCommand create(UI ui, MenuItem item) {
        if (StringUtils.isNotEmpty(item.getBean()) && StringUtils.isNotBlank(item.getBeanMethod())) {
            return new BeanCommand(ui, item, item.getBean(), item.getBeanMethod());
        }

        return null;
    }

    protected Map<String, Object> buildMethodParametersMap(List<MenuItem.MenuItemProperty> properties) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        for (MenuItem.MenuItemProperty property : properties) {
            String name = property.getName();
            Object value = property.getValue() != null
                    ? property.getValue()
                    : loadEntity(property);

            builder.put(name, value);
        }

        return builder.build();
    }

    protected Object loadEntity(MenuItem.MenuItemProperty property) {
        LoadContext<Object> loadContext = new LoadContext<>(property.getEntityClass())
                .setId(property.getEntityId());

        if (StringUtils.isNotEmpty(property.getFetchPlanName())) {
            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(
                    property.getEntityClass(),
                    property.getFetchPlanName()
            );

            loadContext.setFetchPlan(fetchPlan);
        }

        Object entity = dataManager.load(loadContext);

        if (entity == null) {
            String message = String.format("Unable to load entity of class '%s' with id '%s'",
                    property.getEntityClass(), property.getEntityClass());
            throw new RuntimeException(message);
        }

        return entity;
    }

    protected class BeanCommand implements MenuItemCommand {

        protected UI ui;
        protected MenuItem item;

        protected String bean;
        protected String beanMethod;

        public BeanCommand(UI ui, MenuItem item, String bean, String beanMethod) {
            this.ui = ui;
            this.item = item;

            this.bean = bean;
            this.beanMethod = beanMethod;
        }

        @Override
        public void run() {
            Object beanInstance = applicationContext.getBean(bean);
            try {
                Method methodWithParams = MethodUtils.getAccessibleMethod(beanInstance.getClass(),
                        beanMethod, Map.class);

                if (methodWithParams != null) {
                    methodWithParams.invoke(beanInstance, getMethodParameters());
                    return;
                }

                Method methodWithView = MethodUtils.getAccessibleMethod(beanInstance.getClass(),
                        beanMethod, UI.class);
                if (methodWithView != null) {
                    methodWithView.invoke(beanInstance, ui);
                    return;
                }

                MethodUtils.invokeMethod(beanInstance, beanMethod, (Object[]) null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Unable to execute bean method", e);
            }
        }

        protected Map<String, Object> getMethodParameters() {
            return buildMethodParametersMap(item.getProperties());
        }

        @Override
        public String getDescription() {
            return String.format("Calling bean method: %s#%s", bean, beanMethod);
        }
    }
}
