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

package io.jmix.ui.xml.layout.loader;

import io.jmix.core.Metadata;
import io.jmix.ui.component.HasFilterMode;
import io.jmix.ui.component.TagPicker;
import io.jmix.ui.component.data.options.ContainerOptions;

public class TagPickerLoader extends AbstractValuePickerLoader<TagPicker> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(TagPicker.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignFrame(resultComponent);
        assignXmlDescriptor(resultComponent, element);

        loadData(resultComponent, element);
        //noinspection unchecked
        loadOptionsContainer(element)
                .ifPresent(container -> resultComponent.setOptions(new ContainerOptions(container)));

        loadCaption(resultComponent, element);
        loadCaptionAsHtml(resultComponent, element);
        loadDescription(resultComponent, element);
        loadDescriptionAsHtml(resultComponent, element);

        loadIcon(resultComponent, element);
        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
        loadStyleName(resultComponent, element);
        loadVisible(resultComponent, element);
        loadAlign(resultComponent, element);
        loadEnable(resultComponent, element);
        loadEditable(resultComponent, element);
        loadTabIndex(resultComponent, element);
        loadContextHelp(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
        loadHtmlSanitizerEnabled(resultComponent, element);
        loadRequired(resultComponent, element);
        loadInputPrompt(resultComponent, element);

        loadValidation(resultComponent, element);
        loadActions(resultComponent, element);
        if (resultComponent.getActions().isEmpty()) {
            addDefaultActions();
        }

        loadBoolean(element, "inlineTags",
                resultComponent::setInlineTags);
        loadBoolean(element, "hideSelectedOptions",
                resultComponent::setHideSelectedOptions);
        loadEnum(element, TagPicker.TagPosition.class, "tagPosition",
                resultComponent::setTagPosition);
        loadEnum(element, HasFilterMode.FilterMode.class, "filterMode",
                resultComponent::setFilterMode);
        loadString(element, "metaClass", s ->
                resultComponent.setMetaClass(getMetadata().getClass(s)));
        loadInteger(element, "pageLength",
                resultComponent::setPageLength);
    }

    protected Metadata getMetadata() {
        return applicationContext.getBean(Metadata.class);
    }
}
