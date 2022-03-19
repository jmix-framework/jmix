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

import com.vaadin.shared.ui.MarginInfo;
import io.jmix.ui.component.FlowBoxLayout;
import io.jmix.ui.widget.JmixFlowLayout;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class FlowBoxLayoutImpl extends AbstractOrderedLayout<JmixFlowLayout> implements FlowBoxLayout {

    protected static final String FLOWLAYOUT_STYLENAME = "jmix-flowlayout";

    public FlowBoxLayoutImpl() {
        component = createComponent();
    }

    protected JmixFlowLayout createComponent() {
        return new JmixFlowLayout();
    }

    @Override
    public void setStyleName(@Nullable String styleName) {
        super.setStyleName(styleName);

        component.addStyleName(FLOWLAYOUT_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(FLOWLAYOUT_STYLENAME, ""));
    }

    @Override
    public void setMargin(io.jmix.ui.component.MarginInfo marginInfo) {
        MarginInfo vMargin = new MarginInfo(marginInfo.hasTop(), marginInfo.hasRight(), marginInfo.hasBottom(),
                marginInfo.hasLeft());
        component.setMargin(vMargin);
    }

    @Override
    public io.jmix.ui.component.MarginInfo getMargin() {
        MarginInfo vMargin = component.getMargin();
        return new io.jmix.ui.component.MarginInfo(vMargin.hasTop(), vMargin.hasRight(), vMargin.hasBottom(),
                vMargin.hasLeft());
    }

    @Override
    public void setSpacing(boolean enabled) {
        component.setSpacing(enabled);
    }

    @Override
    public boolean getSpacing() {
        return component.isSpacing();
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return component.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        component.setRequiredIndicatorVisible(visible);
    }
}
