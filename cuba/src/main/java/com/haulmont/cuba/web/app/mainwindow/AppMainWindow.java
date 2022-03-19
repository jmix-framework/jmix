/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.web.app.mainwindow;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.gui.components.AbstractMainWindow;
import com.haulmont.cuba.gui.components.Image;
import com.vaadin.server.Sizeable;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.SizeUnit;
import io.jmix.ui.component.SplitPanel;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.widget.JmixHorizontalSplitPanel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class AppMainWindow extends AbstractMainWindow {

    @Autowired
    protected AppMenu mainMenu;

    @Autowired
    protected BoxLayout titleBar;

    @Autowired
    protected SplitPanel foldersSplit;

/*    @Autowired
    protected FtsField ftsField;*/

    @Autowired
    protected Image logoImage;

    @Autowired
    protected CubaProperties cubaProperties;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        mainMenu.focus();

        initLogoImage(logoImage);
        // todo layout context analyzer
//        initLayoutAnalyzerContextMenu(logoImage);
        // todo fts
//        initFtsField(ftsField);

        if (cubaProperties.isFoldersPaneEnabled()) {
            if (cubaProperties.isFoldersPaneVisibleByDefault()) {
                foldersSplit.setSplitPosition(cubaProperties.getFoldersPaneDefaultWidth(), SizeUnit.PIXELS);
            } else {
                foldersSplit.setSplitPosition(0);
            }

            JmixHorizontalSplitPanel vSplitPanel = foldersSplit.unwrap(JmixHorizontalSplitPanel.class);
            vSplitPanel.setDefaultPosition(cubaProperties.getFoldersPaneDefaultWidth() + "px");
            vSplitPanel.setMaxSplitPosition(50, Sizeable.Unit.PERCENTAGE);
            vSplitPanel.setDockable(true);
        } else {
            foldersPane.setEnabled(false);
            foldersPane.setVisible(false);

            foldersSplit.remove(workArea);

            int foldersSplitIndex = indexOf(foldersSplit);

            remove(foldersSplit);
            add(workArea, foldersSplitIndex);

            expand(workArea);
        }
    }
}