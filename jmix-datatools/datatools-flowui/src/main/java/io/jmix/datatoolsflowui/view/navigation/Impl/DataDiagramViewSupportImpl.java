/*
 * Copyright 2026 Haulmont.
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

package io.jmix.datatoolsflowui.view.navigation.Impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import io.jmix.datatoolsflowui.view.navigation.DataDiagramViewSupport;
import io.jmix.flowui.view.ViewRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class DataDiagramViewSupportImpl implements DataDiagramViewSupport {

    @Autowired
    protected ViewRegistry viewRegistry;

    @Override
    public void open() {
        Class<? extends Component> navigationTarget = viewRegistry.getViewInfo("datatl_dataModelDiagramView")
                .getControllerClass();

        String url = viewRegistry.getRouteConfiguration().getUrl(navigationTarget);
        UI.getCurrent().getPage().open(url);
    }
}
