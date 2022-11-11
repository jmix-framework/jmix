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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.progressbar.ProgressBar;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public class ProgressBarLoader extends AbstractComponentLoader<ProgressBar> {

    @Override
    protected ProgressBar createComponent() {
        return factory.create(ProgressBar.class);
    }

    @Override
    public void loadComponent() {
        loadDouble(element, "max", resultComponent::setMax);
        loadDouble(element, "min", resultComponent::setMin);
        loadDouble(element, "value", resultComponent::setValue);
        loadBoolean(element, "indeterminate", resultComponent::setIndeterminate);

        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }
}
