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

import io.jmix.ui.Screens.LaunchMode;

import javax.annotation.Nullable;

public interface RelatedEntities<E> extends Component,
        Component.HasCaption, Component.BelongToFrame, Component.HasIcon,
        Component.Focusable, HasHtmlCaption, HasHtmlDescription {

    String NAME = "relatedEntities";

    LaunchMode getLaunchMode();

    void setLaunchMode(LaunchMode launchMode);

    @Nullable
    String getExcludePropertiesRegex();

    void setExcludePropertiesRegex(@Nullable String excludeRegex);

    void addPropertyOption(String property,
                           @Nullable String screen,
                           @Nullable String caption,
                           @Nullable String filterCaption);

    void removePropertyOption(String property);

    @Nullable
    ListComponent<E> getListComponent();

    void setListComponent(@Nullable ListComponent<E> listComponent);
}
