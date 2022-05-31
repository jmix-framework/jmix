/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.avatar.Avatar;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class AvatarLoader extends AbstractComponentLoader<Avatar> {

    @Override
    protected Avatar createComponent() {
        return factory.create(Avatar.class);
    }

    @Override
    public void loadComponent() {
        loadResourceString(element, "name", context.getMessageGroup(), resultComponent::setName);
        loadString(element, "image", resultComponent::setImage);
        loadInteger(element, "colorIndex", resultComponent::setColorIndex);
        loadResourceString(element, "abbreviation", context.getMessageGroup(), resultComponent::setAbbreviation);

        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }
}
