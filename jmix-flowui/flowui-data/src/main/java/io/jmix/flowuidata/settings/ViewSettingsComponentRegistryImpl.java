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

package io.jmix.flowuidata.settings;

import com.vaadin.flow.component.Component;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.facet.settings.Settings;
import io.jmix.flowui.facet.settings.ViewSettingsComponentRegistry;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects {@link ComponentSettingsBinder} and provides information for which component registered settings class.
 */
@org.springframework.stereotype.Component("ui_ViewSettingsComponentRegistryImpl")
public class ViewSettingsComponentRegistryImpl implements ViewSettingsComponentRegistry, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ViewSettingsComponentRegistryImpl.class);

    protected List<ComponentSettingsBinder<?, ?>> binders;

    protected Map<Class<? extends Component>, ComponentSettingsBinder<?, ?>> componentBinders =
            new ConcurrentHashMap<>();

    public ViewSettingsComponentRegistryImpl(List<ComponentSettingsBinder<?, ?>> binders) {
        this.binders = binders;
    }

    @Override
    public void afterPropertiesSet() {
        for (ComponentSettingsBinder<?, ?> binder : binders) {
            register(binder);
        }
    }

    @Override
    public Class<? extends Settings> getSettingsClass(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        ComponentSettingsBinder<?, ?> binder = componentBinders.get(componentClass);
        if (binder != null) {
            return binder.getSettingsClass();
        }

        throw new IllegalStateException(String.format("Can't find settings class for '%s'", componentClass));
    }

    @Override
    public ComponentSettingsBinder<?, ?> getSettingsBinder(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        ComponentSettingsBinder<?, ?> binder = componentBinders.get(componentClass);
        if (binder != null) {
            return binder;
        }

        throw new IllegalStateException(String.format("Cannot find settings binder for '%s'", componentClass));
    }

    @Override
    public boolean isSettingsRegisteredFor(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        ComponentSettingsBinder<?, ?> binder = componentBinders.get(componentClass);
        return binder != null;
    }

    protected void register(ComponentSettingsBinder<?, ?> binder) {
        Preconditions.checkNotNullArgument(binder.getComponentClass(),
                "Component class cannot be null in '%s'", binder.getClass());
        Preconditions.checkNotNullArgument(binder.getSettingsClass(),
                "Settings class cannot be null in '%s'", binder.getClass());

        if (componentBinders.containsKey(binder.getComponentClass())) {
            log.debug("Component '{}' already has registered settings. Skip settings binder: {}",
                    binder.getComponentClass(), binder.getClass());
            return;
        }

        componentBinders.put(binder.getComponentClass(), binder);
    }
}
