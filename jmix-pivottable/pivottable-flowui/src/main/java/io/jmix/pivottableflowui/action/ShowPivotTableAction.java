/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.action;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ActionType(ShowPivotTableAction.ID)
public class ShowPivotTableAction extends ListDataComponentAction<ShowPivotTableAction, Object>
        implements ApplicationContextAware {

    public static final String ID = "pvttbl_showPivotTable";

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected Dialogs dialogs;

    protected String includedProperties;
    protected String excludedProperties;

    protected enum ShowPivotTableMode {
        ALL_ROWS, SELECTED_ROWS
    }

    public ShowPivotTableAction() {
        super(ID);
    }

    public ShowPivotTableAction(String id) {
        super(id);
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("ShowPivotAction target is not set");
        }

        if (needShowAll()) {
            showPivotTable(ShowPivotTableMode.ALL_ROWS);
        } else {
            Action[] actions = new Action[]{
                    new SecuredBaseAction("ShowPivotTableMode.SELECTED_ROWS")
                            .withText(messages.getMessage("actions.showPivotAction.SELECTED_ROWS"))
                            .withHandler(event -> showPivotTable(ShowPivotTableMode.SELECTED_ROWS)),
                    new SecuredBaseAction("ShowPivotTableMode.ALL_ROWS")
                            .withText(messages.getMessage("actions.showPivotAction.ALL_ROWS"))
                            .withHandler(event -> showPivotTable(ShowPivotTableMode.ALL_ROWS)),
                    new DialogAction(DialogAction.Type.CANCEL)
            };

            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage("actions.showPivotAction.dialogTitle"))
                    .withText(messages.getMessage("actions.showPivotAction.dialogMessage"))
                    .withActions(actions)
                    .withWidth("32em")
                    .open();
        }
    }

    protected boolean needShowAll() {
        if (target.getSelectedItems().isEmpty()
                || !(target.getItems() instanceof ContainerDataUnit)) {
            return true;
        }

        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        return container != null && container.getItems().size() <= 1;
    }

    protected List<String> parseProperties(String properties) {
        if (Strings.isNullOrEmpty(properties)) {
            return Collections.emptyList();
        }

        properties = properties.replace(" ", "");
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }

        String[] propertiesArray = properties.split(",");
        return Arrays.asList(propertiesArray);
    }

    /**
     * Set excluded properties separated by a comma. Excluded properties will not be shown in the PivotTable.
     *
     * @param properties excluded properties
     */
    public void setExcludedProperties(String properties) {
        excludedProperties = properties;
    }

    public String getExcludedProperties() {
        return excludedProperties;
    }

    /**
     * @return list with parsed excluded properties
     */
    public List<String> getExcludedPropertiesList() {
        if (Strings.isNullOrEmpty(excludedProperties)) {
            return Collections.emptyList();
        }
        return parseProperties(excludedProperties);
    }

    /**
     * Sets included properties separated by a comma. Only included properties will be shown in the PivotTable.
     *
     * @param properties included properties
     */
    public void setIncludedProperties(String properties) {
        includedProperties = properties;
    }

    public String getIncludedProperties() {
        return includedProperties;
    }

    /**
     * @return list with parsed included properties
     */
    public List<String> getIncludedPropertiesList() {
        if (Strings.isNullOrEmpty(includedProperties)) {
            return Collections.emptyList();
        }
        return parseProperties(includedProperties);
    }

    @SuppressWarnings("unchecked")
    protected void showPivotTable(ShowPivotTableMode mode) {
        Collection items;
        if (ShowPivotTableMode.ALL_ROWS.equals(mode)) {
            if (target.getItems() instanceof ContainerDataUnit) {
                CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
                items = container.getItems();
            } else {
                items = Collections.emptyList();
            }
        } else {
            items = target.getSelectedItems();
        }

        PivotTableViewBuilder showPivotManager = applicationContext.getBean(PivotTableViewBuilder.class, target);
        showPivotManager.withItems(items)
                .withIncludedProperties(parseProperties(includedProperties))
                .withExcludedProperties(parseProperties(excludedProperties))
                .show();
    }
}
