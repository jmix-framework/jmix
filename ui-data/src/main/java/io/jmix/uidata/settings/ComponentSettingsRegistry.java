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

package io.jmix.uidata.settings;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.impl.TableImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects {@link ComponentSettingsBinder} and provides information for which component registered settings class.
 */
@Internal
@org.springframework.stereotype.Component("ui_ComponentSettingsRegistry")
public class ComponentSettingsRegistry implements InitializingBean {

    @Autowired
    protected List<ComponentSettingsBinder> binders;

    protected Map<Class<? extends Component>, Class<? extends ComponentSettings>> componentSettings = new ConcurrentHashMap<>();
    protected Map<Class<? extends Component>, ComponentSettingsBinder> componentBinder = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        for (ComponentSettingsBinder binder : binders) {
            register(binder);
        }
    }

    /**
     * @param componentClass component class (e.g. WebTable)
     * @return component settings class, otherwise it will throws an exception
     */
    public Class<? extends ComponentSettings> getSettingsClass(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        Class<? extends ComponentSettings> settingClass = componentSettings.get(componentClass);
        if (settingClass != null) {
            return settingClass;
        }

        throw new IllegalStateException(String.format("Can't find settings class for '%s'", componentClass));
    }

    /**
     * @param componentClass component class
     * @return component settings binder or throws an exception if there is no binder registered for this class
     */
    public ComponentSettingsBinder getBinder(Class<? extends Component> componentClass) {
        ComponentSettingsBinder binder = componentBinder.get(componentClass);
        if (binder == null) {
            throw new IllegalStateException(String.format("Cannot find binder for: '%s'", componentClass));
        }
        return binder;
    }

    /**
     * @param componentClass component class (e.g. {@link TableImpl})
     * @return true if settings is registered for component class
     */
    public boolean isSettingsRegisteredFor(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        Class<? extends ComponentSettings> settingsClass = componentSettings.get(componentClass);
        return settingsClass != null;
    }

    protected void register(ComponentSettingsBinder binder) {
        Preconditions.checkNotNullArgument(binder.getComponentClass(),
                "Component class cannot be null in '%s'", binder.getClass());
        Preconditions.checkNotNullArgument(binder.getSettingsClass(),
                "Settings class cannot be null in '%s'", binder.getClass());

        componentSettings.put(binder.getComponentClass(), binder.getSettingsClass());
        componentBinder.put(binder.getComponentClass(), binder);
    }
}
