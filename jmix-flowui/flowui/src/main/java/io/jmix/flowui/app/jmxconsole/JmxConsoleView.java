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


import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
    protected CollectionContainer<ManagedBeanInfo> mbeanDc;
    @ViewComponent
    protected TypedTextField mbeanSearchField;

    @Autowired
    protected JmxControl jmxControl;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;

    protected Icon searchIcon;

    @Subscribe
    public void onInit(final InitEvent event) {
        initSearchField();
        reloadMBeans(null);
    }

    protected void initSearchField() {
        createSearchIcon();
        mbeanSearchField.setSuffixComponent(searchIcon);
        mbeanSearchField.addKeyPressListener(Key.ENTER, keyPressEvent -> reloadMBeans(mbeanSearchField.getValue()));
    }

    protected void createSearchIcon() {
        searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.addClickListener(event -> reloadMBeans(mbeanSearchField.getValue()));
    }

    @Subscribe("mbeansDataGrid.inspect")
    public void onMbeansDataGridInspect(final ActionPerformedEvent event) {
        showMBeanDetail();
    }

    protected void showMBeanDetail() {
        ManagedBeanInfo managedBeanInfo = mbeansDataGrid.getSingleSelectedItem();

        if (managedBeanInfo != null) {
            viewNavigators.detailView(mbeansDataGrid)
                    .withViewClass(MBeanInfoDetailView.class)
                    .navigate();
        }
    }

    protected void reloadMBeans(String objectName) {
        List<ManagedBeanInfo> managedBeanInfos = jmxControl.getManagedBeans();

        if (StringUtils.isNotEmpty(objectName)) {
            List<ManagedBeanInfo> res = managedBeanInfos.stream()
                    .filter(managedBeanInfo -> StringUtils.containsIgnoreCase(managedBeanInfo.getObjectName(), objectName))
                    .sorted(Comparator.comparing(ManagedBeanInfo::getDomain))
                    .collect(Collectors.toList());

            setManagedBeans(res);
        } else {
            List<ManagedBeanInfo> res = managedBeanInfos.stream()
                    .sorted(Comparator.comparing(ManagedBeanInfo::getDomain))
                    .collect(Collectors.toList());
            setManagedBeans(res);
        }
    }

    protected void setManagedBeans(List<ManagedBeanInfo> res) {
        mbeanDc.getMutableItems().clear();
        mbeanDc.getMutableItems().addAll(res);
    }
}