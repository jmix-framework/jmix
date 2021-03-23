/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dashboardsui.component.impl;

import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.WidgetLayout;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.screen.ScreenFragment;

public class CanvasWidgetLayout extends AbstractCanvasLayout {

    public static final String NAME = "canvasWidgetLayout";

    protected VBoxLayout verticalLayout;
    protected Widget widget = null;
    protected ScreenFragment widgetComponent;
    protected Component innerLayout;

    public CanvasWidgetLayout init(WidgetLayout model) {
        init(model, VBoxLayout.class);
        verticalLayout = (VBoxLayout) delegate;
        return this;
    }

    @Override
    public VBoxLayout getDelegate() {
        return verticalLayout;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public ScreenFragment getWidgetComponent() {
        return widgetComponent;
    }

    public void setWidgetComponent(ScreenFragment widgetComponent) {
        this.widgetComponent = widgetComponent;
    }

    public Component getInnerLayout() {
        return innerLayout;
    }

    public void setInnerLayout(Component innerLayout) {
        this.innerLayout = innerLayout;
    }

}
