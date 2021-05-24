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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.flowlayout.JmixFlowLayoutState;
import com.vaadin.ui.Layout;

public class JmixFlowLayout extends JmixCssActionsLayout implements Layout.MarginHandler, Layout.SpacingHandler {

    public JmixFlowLayout() {
        setStyleName("jmix-flowlayout");
    }

    @Override
    protected JmixFlowLayoutState getState() {
        return (JmixFlowLayoutState) super.getState();
    }

    @Override
    protected JmixFlowLayoutState getState(boolean markAsDirty) {
        return (JmixFlowLayoutState) super.getState(markAsDirty);
    }
}