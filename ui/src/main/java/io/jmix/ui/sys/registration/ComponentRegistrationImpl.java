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

package io.jmix.ui.sys.registration;

import com.google.common.base.Strings;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.Component;
import io.jmix.ui.xml.layout.ComponentLoader;

import javax.annotation.Nullable;

@Internal
public class ComponentRegistrationImpl implements ComponentRegistration {

    protected final String name;
    protected final String tag;
    protected final Class<? extends Component> componentClass;
    protected final Class<? extends ComponentLoader> componentClassLoader;

    public ComponentRegistrationImpl(String name,
                                     @Nullable String tag,
                                     @Nullable Class<? extends Component> componentClass,
                                     @Nullable Class<? extends ComponentLoader> componentClassLoader) {
        this.name = name;
        this.tag = Strings.isNullOrEmpty(tag) ? name : tag;
        this.componentClass = componentClass;
        this.componentClassLoader = componentClassLoader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Nullable
    @Override
    public Class<? extends Component> getComponentClass() {
        return componentClass;
    }

    @Nullable
    @Override
    public Class<? extends ComponentLoader> getComponentLoaderClass() {
        return componentClassLoader;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return name.equals(((ComponentRegistrationImpl) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        String componentClass = getComponentClass() == null ? "null" : getComponentClass().getName();
        String componentLoaderClass = getComponentLoaderClass() == null ? "null" : getComponentLoaderClass().getName();

        return "{\"name\": \"" + name + "\", "
                + "\"tag\": \"" + tag + "\", "
                + "\"componentClass\": \"" + componentClass + "\", "
                + "\"componentClassLoader\": \"" + componentLoaderClass + "\"}";
    }
}
