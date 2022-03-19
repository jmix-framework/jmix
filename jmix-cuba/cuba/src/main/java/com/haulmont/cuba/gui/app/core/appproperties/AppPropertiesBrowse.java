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

package com.haulmont.cuba.gui.app.core.appproperties;

import com.haulmont.cuba.core.config.AppPropertyEntity;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.cuba.settings.Settings;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppPropertiesBrowse extends AbstractWindow {

    @Autowired
    private AppPropertiesDatasource paramsDs;

    @Named("paramsTable.editValue")
    private Action editValueAction;

    @Named("paramsTable.refresh")
    private RefreshAction refreshAction;

    @Autowired
    private TreeTable<AppPropertyEntity> paramsTable;

    @Autowired
    private TextField<String> searchField;

    @Autowired
    private Button exportBtn;

    @Autowired
    private HBoxLayout hintBox;

    private AppPropertyEntity lastSelected;

    @Override
    public void init(Map<String, Object> params) {
        paramsDs.addItemChangeListener(e -> {
            boolean enabled = e.getItem() != null && !e.getItem().getCategory();
            editValueAction.setEnabled(enabled);
            exportBtn.setEnabled(enabled);
        });
        paramsTable.setItemClickAction(editValueAction);

        paramsTable.sort("name", Table.SortDirection.ASCENDING);

        searchField.addValueChangeListener(e -> {
            paramsDs.refresh(ParamsMap.of("name", e.getValue()));

            if (StringUtils.isNotEmpty(e.getValue())) {
                paramsTable.expandAll();
            }
        });

        refreshAction.setBeforeRefreshHandler(() ->
                lastSelected = paramsTable.getSingleSelected()
        );
        refreshAction.setAfterRefreshHandler(() -> {
            if (StringUtils.isNotEmpty(searchField.getValue())) {
                paramsTable.expandAll();
            }

            if (lastSelected != null) {
                for (AppPropertyEntity entity : paramsDs.getItems()) {
                    if (entity.getName().equals(lastSelected.getName())) {
                        paramsTable.expand(entity.getId());
                        paramsTable.setSelected(entity);
                    }
                }
            }
        });
    }

    public void editValue() {
        com.haulmont.cuba.gui.components.Window editor = openWindow("appPropertyEditor", WindowManager.OpenType.DIALOG,
                ParamsMap.of("item", paramsDs.getItem()));
        editor.addCloseWithCommitListener(() -> {
            List<AppPropertyEntity> entities = paramsDs.loadAppPropertyEntities();
            for (AppPropertyEntity entity : entities) {
                if (entity.getName().equals(paramsDs.getItem().getName())) {
                    paramsDs.getItem().setCurrentValue(entity.getCurrentValue());
//                    paramsDs.getItem().setUpdateTs(entity.getUpdateTs());
//                    paramsDs.getItem().setUpdatedBy(entity.getUpdatedBy()); TODO: support audit fields (e.g., updateTs)
                    break;
                }
            }
        });
    }

    public void exportAsSql() {
        List<AppPropertyEntity> exported = paramsTable.getSelected().stream()
                .filter(appPropertyEntity -> !appPropertyEntity.getCategory())
                .collect(Collectors.toList());
        if (!exported.isEmpty()) {
            openWindow("appPropertiesExport", WindowManager.OpenType.DIALOG, ParamsMap.of("exported", exported));
        }
    }

    public void closeHint() {
        hintBox.setVisible(false);
        getSettings().get(hintBox.getId()).addAttribute("visible", "false");
    }

    @Override
    public void applySettings(Settings settings) {
        super.applySettings(settings);
        String visible = settings.get(hintBox.getId()).attributeValue("visible");
        if (visible != null)
            hintBox.setVisible(Boolean.parseBoolean(visible));
    }
}
