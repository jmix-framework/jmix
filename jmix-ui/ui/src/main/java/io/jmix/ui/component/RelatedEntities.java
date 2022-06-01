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

package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.PropertyOption;

import javax.annotation.Nullable;

@StudioComponent(
        caption = "RelatedEntities",
        category = "Components",
        xmlElement = "relatedEntities",
        icon = "io/jmix/ui/icon/component/relatedEntities.svg",
        canvasTextProperty = "caption",
        canvasBehaviour = CanvasBehaviour.POPUP_BUTTON,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/related-entities.html"
)
public interface RelatedEntities<E> extends Component,
        Component.HasCaption, Component.BelongToFrame, Component.HasIcon,
        Component.Focusable, HasHtmlCaption, HasHtmlDescription {

    String NAME = "relatedEntities";

    OpenMode getOpenMode();

    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "THIS_TAB",
            options = {"NEW_TAB", "THIS_TAB", "DIALOG", "NEW_WINDOW"})
    void setOpenMode(OpenMode openMode);

    @Nullable
    String getExcludePropertiesRegex();

    @StudioProperty(name = "exclude", type = PropertyType.STRING)
    void setExcludePropertiesRegex(@Nullable String excludeRegex);

    @StudioElement
    void addPropertyOption(PropertyOption property);

    void removePropertyOption(String property);

    @Nullable
    ListComponent<E> getListComponent();

    @StudioProperty(name = "for", type = PropertyType.COMPONENT_REF, required = true,
            options = {"io.jmix.ui.component.ListComponent"})
    void setListComponent(@Nullable ListComponent<E> listComponent);
}
