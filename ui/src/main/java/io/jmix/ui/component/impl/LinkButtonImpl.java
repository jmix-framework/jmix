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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.LinkButton;
import com.vaadin.ui.themes.ValoTheme;
import io.jmix.ui.widget.JmixButton;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class LinkButtonImpl extends ButtonImpl implements LinkButton {

    public LinkButtonImpl() {
    }

    @Override
    protected void initComponent(JmixButton component) {
        super.initComponent(component);

        component.addStyleName(ValoTheme.BUTTON_LINK);
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);
        component.addStyleName(ValoTheme.BUTTON_LINK);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(ValoTheme.BUTTON_LINK, ""));
    }
}