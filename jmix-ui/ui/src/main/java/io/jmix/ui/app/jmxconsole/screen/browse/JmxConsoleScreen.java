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

package io.jmix.ui.app.jmxconsole.screen.browse;

import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.jmxconsole.JmxControl;
import io.jmix.ui.app.jmxconsole.model.ManagedBeanInfo;
import io.jmix.ui.app.jmxconsole.screen.inspect.MBeanInspectScreen;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UiController("ui_JmxConsoleScreen")
@UiDescriptor("jmx-console-screen.xml")
@LookupComponent("mbeansTable")
public class JmxConsoleScreen extends Screen {
    @Autowired
    protected CollectionContainer<ManagedBeanInfo> mbeanDc;

    @Autowired
    protected GroupTable<ManagedBeanInfo> mbeansTable;

    @Autowired
    protected JmxControl jmxControl;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    @Qualifier("mbeansTable.inspect")
    protected Action inspectAction;


    @Subscribe
    public void beforeShow(BeforeShowEvent beforeShowEvent) {
        mbeansTable.setItemClickAction(inspectAction);

        reloadMBeans(null);
    }

    @Subscribe("mbeansTable.inspect")
    protected void openInspectScreen(Action.ActionPerformedEvent event) {
        ManagedBeanInfo mbi = mbeansTable.getSingleSelected();

        if (mbi != null) {
            MBeanInspectScreen inspectMBeanScreen = screenBuilders.editor(ManagedBeanInfo.class, this)
                    .withOpenMode(OpenMode.NEW_TAB)
                    .editEntity(mbi)
                    .withScreenClass(MBeanInspectScreen.class)
                    .build();
            inspectMBeanScreen.addAfterCloseListener(afterCloseEvent -> mbeansTable.focus());
            inspectMBeanScreen.show();
        }
    }

    @Subscribe("objectNameField")
    protected void objectNamValueChanged(HasValue.ValueChangeEvent<String> event) {
        reloadMBeans(event.getValue());
    }

    protected void reloadMBeans(String objectName) {
        List<ManagedBeanInfo> managedBeanInfos = jmxControl.getManagedBeans();

        if (StringUtils.isNotEmpty(objectName)) {
            List<ManagedBeanInfo> res = managedBeanInfos.stream()
                    .filter(managedBeanInfo -> StringUtils.contains(managedBeanInfo.getObjectName().toLowerCase(), objectName.toLowerCase()))
                    .sorted(Comparator.comparing(ManagedBeanInfo::getDomain))
                    .collect(Collectors.toList());

            setManagedBeans(res);
            mbeansTable.expandAll();
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