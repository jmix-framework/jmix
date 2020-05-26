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

import com.haulmont.cuba.gui.components.AbstractMainWindow;
import com.haulmont.cuba.gui.components.Image;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.SplitPanel;
import io.jmix.ui.component.mainwindow.AppMenu;

import javax.inject.Inject;
import java.util.Map;

public class AppMainWindow extends AbstractMainWindow {

    @Inject
    protected AppMenu mainMenu;

    @Inject
    protected BoxLayout titleBar;

    @Inject
    protected SplitPanel foldersSplit;

/*    @Inject
    protected FtsField ftsField;*/

    @Inject
    protected Image logoImage;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        mainMenu.focus();

        initLogoImage(logoImage);
        // todo layout context analyzer
//        initLayoutAnalyzerContextMenu(logoImage);
        // todo fts
//        initFtsField(ftsField);

        // todo see #429
//        if (webConfig.getUseInverseHeader()) {
//            titleBar.setStyleName("c-app-menubar c-inverse-header");
//        }

        //            todo folders pane
 /*       if (webConfig.getFoldersPaneEnabled()) {
            if (webConfig.getFoldersPaneVisibleByDefault()) {
                foldersSplit.setSplitPosition(webConfig.getFoldersPaneDefaultWidth(), SizeUnit.PIXELS);
            } else {
                foldersSplit.setSplitPosition(0);
            }

            CubaHorizontalSplitPanel vSplitPanel = (CubaHorizontalSplitPanel) WebComponentsHelper.unwrap(foldersSplit);
            vSplitPanel.setDefaultPosition(webConfig.getFoldersPaneDefaultWidth() + "px");
            vSplitPanel.setMaxSplitPosition(50, Unit.PERCENTAGE);
            vSplitPanel.setDockable(true);
        } else {
            foldersPane.setEnabled(false);
            foldersPane.setVisible(false);

            foldersSplit.remove(workArea);

            int foldersSplitIndex = indexOf(foldersSplit);

            remove(foldersSplit);
            add(workArea, foldersSplitIndex);

            expand(workArea);
        }*/
    }
}