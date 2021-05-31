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

package io.jmix.ui.settings.component.binder;

import io.jmix.core.JmixOrder;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.Table.Column;
import io.jmix.ui.component.data.meta.EntityTableItems;
import io.jmix.ui.component.impl.GroupTableImpl;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.GroupTableSettings;
import io.jmix.ui.settings.component.SettingsWrapper;
import io.jmix.ui.settings.component.TableSettings;
import io.jmix.ui.widget.JmixGroupTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Order(JmixOrder.LOWEST_PRECEDENCE)
@org.springframework.stereotype.Component("ui_GroupTableSettingsBinder")
public class GroupTableSettingsBinder extends AbstractTableSettingsBinder {

    private static final Logger log = LoggerFactory.getLogger(GroupTableSettingsBinder.class);

    protected MetadataTools metadataTools;

    @Autowired
    protected void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public Class<? extends Component> getComponentClass() {
        return GroupTableImpl.class;
    }

    @Override
    public Class<? extends ComponentSettings> getSettingsClass() {
        return GroupTableSettings.class;
    }

    @Override
    public boolean saveSettings(Table component, SettingsWrapper wrapper) {
        GroupTable groupTable = (GroupTable) component;
        GroupTableSettings groupTableSettings = wrapper.getSettings();

        boolean commonTableSettingsChanged = super.saveSettings(component, wrapper);
        boolean groupTableSettingsChanged = isGroupTableSettingsChanged(groupTable, groupTableSettings);

        if (!groupTableSettingsChanged && !commonTableSettingsChanged) {
            return false;
        }

        if (groupTableSettingsChanged) {
            // save columns settings if they were not saved
            if (groupTableSettings.getColumns() == null) {
                groupTableSettings.setColumns(getTableColumnSettings(groupTable));
            }

            groupTableSettings.setGroupProperties(getGroupProperties(groupTable));
        }

        return true;
    }

    @Override
    public GroupTableSettings getSettings(Table component) {
        GroupTable groupTable = (GroupTable) component;
        GroupTableSettings groupTableSettings = (GroupTableSettings) super.getSettings(component);

        List<String> groupProperties = getGroupProperties(groupTable);
        if (!groupProperties.isEmpty()) {
            groupTableSettings.setGroupProperties(groupProperties);
        }

        return groupTableSettings;
    }

    @Override
    protected void applyColumnSettings(TableSettings tableSettings, Table table) {
        super.applyColumnSettings(tableSettings, table);

        GroupTableSettings groupTableSettings = (GroupTableSettings) tableSettings;
        List<String> groupProperties = groupTableSettings.getGroupProperties();
        if (groupProperties != null) {
            MetaClass metaClass = ((EntityTableItems) table.getItems()).getEntityMetaClass();
            List<MetaPropertyPath> properties = new ArrayList<>(groupProperties.size());

            for (String id : groupProperties) {
                MetaPropertyPath property = metadataTools.resolveMetaPropertyPathOrNull(metaClass, id);
                if (property != null) {
                    properties.add(property);
                } else {
                    log.warn("Ignored group property '{}'", id);
                }
            }

            ((GroupTable) table).groupBy(properties.toArray());
        } else {
            ((GroupTable) table).ungroup();
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean isGroupTableSettingsChanged(GroupTable groupTable, GroupTableSettings groupTableSettings) {
        // if group properties are null consider settings changed, because we cannot track changes
        // without previous "state"
        if (groupTableSettings.getGroupProperties() == null) {
            return true;
        }

        List<String> settingsProperties = groupTableSettings.getGroupProperties();
        Collection<?> tableGroupProperties = getJmixGroupTable(groupTable).getGroupProperties();
        if (settingsProperties.size() != tableGroupProperties.size()) {
            return true;
        }

        List<Object> groupProperties = new ArrayList<>(tableGroupProperties);
        List<Column> visibleColumns = groupTable.getNotCollapsedColumns();

        for (int i = 0; i < groupProperties.size(); i++) {
            String columnId = groupProperties.get(i).toString();
            String settingsColumnId = settingsProperties.get(i);

            Column column = groupTable.getColumn(columnId);
            if (visibleColumns.contains(column)) {
                if (!columnId.equals(settingsColumnId)) {
                    return true;
                }
            } else if (columnId.equals(settingsColumnId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected TableSettings createTableSettings() {
        return new GroupTableSettings();
    }

    protected List<String> getGroupProperties(GroupTable groupTable) {
        Collection<?> groupTableColumns = getJmixGroupTable(groupTable).getGroupProperties();
        List<String> groupProperties = new ArrayList<>(groupTableColumns.size());

        for (Object groupProperty : groupTableColumns) {
            Column column = groupTable.getColumn(groupProperty.toString());

            if (groupTable.getNotCollapsedColumns().contains(column)) {
                groupProperties.add(groupProperty.toString());
            }
        }

        return groupProperties;
    }

    protected JmixGroupTable getJmixGroupTable(GroupTable groupTable) {
        return groupTable.unwrap(JmixGroupTable.class);
    }
}
