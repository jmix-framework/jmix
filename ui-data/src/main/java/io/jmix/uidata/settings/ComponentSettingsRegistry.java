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

import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.Component;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.ComponentSettings;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects {@link ComponentSettingsBinder} and provides information for which component registered settings class.
 */
@org.springframework.stereotype.Component(ComponentSettingsRegistry.NAME)
public class ComponentSettingsRegistry implements InitializingBean {

    public static final String NAME = "uidata_ComponentSettingsRegistry";

    @Inject
    protected List<ComponentSettingsBinder> binders;

    protected Map<Class<? extends Component>, Class<? extends ComponentSettings>> settingsClasses = new ConcurrentHashMap<>();
    protected Map<Class<? extends ComponentSettings>, Class<? extends ComponentSettingsBinder>> binderClasses = new ConcurrentHashMap<>();

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

        Class<? extends ComponentSettings> settingClass = settingsClasses.get(componentClass);
        if (settingClass != null) {
            return settingClass;
        }

        throw new IllegalStateException(String.format("Can't find settings class for '%s'", componentClass));
    }

    /**
     * @param settingsClass settings class (e.g. {@link TableSettings})
     * @return binder class if registered, otherwise in throws an exception
     */
    public Class<? extends ComponentSettingsBinder> getBinderClass(Class<? extends ComponentSettings> settingsClass) {
        Preconditions.checkNotNullArgument(settingsClass);

        Class<? extends ComponentSettingsBinder> binderClass = binderClasses.get(settingsClass);
        if (binderClass != null) {
            return binderClass;
        }

        throw new IllegalStateException(String.format("Cannot find binder class for '%s'", settingsClass));
    }

    /**
     * @param componentClass component class (e.g. WebTable)
     * @return true if settings is registered for component class
     */
    public boolean isSettingsRegisteredFor(Class<? extends Component> componentClass) {
        Preconditions.checkNotNullArgument(componentClass);

        Class<? extends ComponentSettings> settingsClass = settingsClasses.get(componentClass);
        return settingsClass != null;
    }

    protected void register(ComponentSettingsBinder binder) {
        Preconditions.checkNotNullArgument(binder.getComponentClass(),
                "Component class cannot be null in '%s'", binder.getClass());
        Preconditions.checkNotNullArgument(binder.getSettingsClass(),
                "Settings class cannot be null in '%s'", binder.getClass());

        settingsClasses.put(binder.getComponentClass(), binder.getSettingsClass());
        binderClasses.put(binder.getSettingsClass(), binder.getClass());
    }
}
