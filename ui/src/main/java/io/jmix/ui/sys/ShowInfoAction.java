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
package io.jmix.ui.sys;

import io.jmix.core.AppBeans;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;

public class ShowInfoAction extends BaseAction {

    public static final String ACTION_ID = "showSystemInfo";
    public static final String ACTION_PERMISSION = "cuba.gui.showInfo";

    public ShowInfoAction() {
        super(ACTION_ID);

        Messages messages = AppBeans.get(Messages.NAME);
        setCaption(messages.getMessage("table.showInfoAction"));
    }

    @Override
    public void actionPerform(Component component) {
        if (component instanceof ListComponent) {

            JmixEntity selectedItem = ((ListComponent) component).getSingleSelected();
            if (selectedItem != null) {
                Metadata metadata = AppBeans.get(Metadata.class);
                showInfo(selectedItem, metadata.getClass(selectedItem), (Component.BelongToFrame) component);
            }
        }
    }

    public void showInfo(JmixEntity entity, MetaClass metaClass, Component.BelongToFrame component) {
        Screens screens = ComponentsHelper.getScreenContext(component)
                .getScreens();

        // todo sysInfoWindow
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("sysInfoWindow");

        Screen screen = screens.create(windowInfo.getId(), OpenMode.DIALOG, new MapScreenOptions(ParamsMap.of(
                "metaClass", metaClass,
                "item", entity)));

        screen.show();
    }
}
