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

package io.jmix.flowui.app.jmxconsole;


import com.google.common.base.Strings;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.navigation.UrlParamSerializer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "system/jmxconsole", layout = DefaultMainViewParent.class)
@ViewController("sys_JmxConsoleView")
@ViewDescriptor("jmx-console-view.xml")
@LookupComponent("mbeansDataGrid")
public class JmxConsoleView extends StandardView {
    @ViewComponent
    protected DataGrid<ManagedBeanInfo> mbeansDataGrid;
    @ViewComponent
    protected TypedTextField<String> mbeanSearchField;
    @ViewComponent
    protected CollectionLoader<ManagedBeanInfo> mbeanDl;

    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected UrlParamSerializer urlParamSerializer;


    @Subscribe
    public void onInit(final InitEvent event) {
        initSearchField();
    }

    protected void initSearchField() {
        mbeanSearchField.addTypedValueChangeListener(valueChangeEvent -> mbeanDl.load());
    }

    @Install(to = "mbeanDl", target = Target.DATA_LOADER)
    protected List<ManagedBeanInfo> mbeanDlLoadDelegate(final LoadContext loadContext) {
        return reloadMBeans(mbeanSearchField.getValue());
    }

    @Nullable
    @Install(to = "mbeansDataGrid.edit", subject = "routeParametersProvider")
    public RouteParameters mbeansDataGridEditRouteParametersProvider() {
        ManagedBeanInfo selectedItem = mbeansDataGrid.getSingleSelectedItem();
        if (selectedItem != null) {
            String serializedObjectName = urlParamSerializer.serialize(selectedItem.getObjectName());
            return new RouteParameters(MBeanInfoDetailView.MBEAN_ROUTE_PARAM_NAME, serializedObjectName);
        }

        return null;
    }

    protected List<ManagedBeanInfo> reloadMBeans(String objectName) {
        List<ManagedBeanInfo> managedBeanInfos = jmxControl.getManagedBeans();

        return managedBeanInfos.stream()
                .filter(managedBeanInfo -> Strings.isNullOrEmpty(objectName) ||
                        StringUtils.containsIgnoreCase(managedBeanInfo.getObjectName(), objectName))
                .sorted(Comparator.comparing(ManagedBeanInfo::getDomain))
                .collect(Collectors.toList());
    }
}
