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

package com.haulmont.cuba.web.app.folders;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.components.filter.UserSetHelper;
import com.haulmont.cuba.security.entity.SearchFolder;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Button;
import io.jmix.ui.screen.StandardCloseAction;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;

@Deprecated
public class SaveSetWindow extends AbstractWindow {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected LookupField<SearchFolder> folderSelect;

    @WindowParam
    protected CubaFoldersPane foldersPane;

    @WindowParam(name = "items")
    protected Set ids;
    @WindowParam
    protected String componentPath;
    @WindowParam
    protected String componentId;
    @WindowParam
    protected String entityType;
    @WindowParam
    protected String entityClass;
    @WindowParam
    protected String query;
    @WindowParam
    protected String username;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        ThemeConstants themeConstants = App.getInstance().getThemeConstants();
        folderSelect.setWidth(themeConstants.get("cuba.web.save-set-window.folderSelect.width"));
    }

    @Subscribe("createNew")
    protected void onCreateNewClick(Button.ClickEvent event) {
        AppUI appUI = AppUI.getCurrent();
        if (appUI == null) {
            return;
        }

        QueryParser parser = queryTransformerFactory.parser(query);
        String entityAlias = parser.getEntityAlias(entityType);
        String filterXml = UserSetHelper.generateSetFilter(ids, entityClass, componentId, entityAlias);

        SearchFolder folder = metadata.create(SearchFolder.class);
        folder.setUsername(username);
        folder.setName("");
        folder.setFilterXml(filterXml);
        folder.setFilterComponentId(componentPath);
        folder.setEntityType(entityType);
        folder.setIsSet(true);

        Runnable commitHandler = () -> {
            foldersPane.saveFolder(folder);
            foldersPane.refreshFolders();
        };

        FolderEditWindow window = AppFolderEditWindow.create(false, false, folder, null, commitHandler);
        appUI.addWindow(window);
        window.addCloseListener(e -> close(COMMIT_ACTION_ID));
    }

    @Subscribe("insertBtn")
    protected void onInsertBtnClick(Button.ClickEvent event) {
        SearchFolder folder = folderSelect.getValue();
        AppUI appUI = AppUI.getCurrent();
        if (appUI != null && folder == null) {
            appUI.getNotifications().create(Notifications.NotificationType.TRAY)
                    .withCaption(getMessage("saveSetWindow.notSelected"))
                    .show();
            return;
        }
        String filterXml = folder.getFilterXml();
        folder.setFilterXml(UserSetHelper.addEntities(filterXml, ids));

        foldersPane.saveFolder(folder);
        foldersPane.refreshFolders();

        close(new StandardCloseAction(COMMIT_ACTION_ID, false));
    }
}
