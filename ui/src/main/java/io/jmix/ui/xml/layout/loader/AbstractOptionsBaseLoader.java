/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.component.OptionsField;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import io.jmix.ui.component.data.options.ContainerOptions;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public abstract class AbstractOptionsBaseLoader<T extends OptionsField> extends AbstractFieldLoader<T> {
    @Override
    public void loadComponent() {
        super.loadComponent();
    }

    protected void loadCaptionProperty(T component, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            component.setOptionCaptionProvider(
                    new CaptionAdapter(captionProperty, applicationContext.getBean(Metadata.class), applicationContext.getBean(MetadataTools.class)));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadOptionsEnum(T resultComponent, Element element) {
        String optionsEnumClass = element.attributeValue("optionsEnum");
        if (StringUtils.isNotEmpty(optionsEnumClass)) {
            resultComponent.setOptionsEnum(getClassManager().findClass(optionsEnumClass));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void loadData(T component, Element element) {
        super.loadData(component, element);

        loadOptionsContainer(element).ifPresent(optionsContainer ->
                component.setOptions(new ContainerOptions(optionsContainer)));
    }
}
