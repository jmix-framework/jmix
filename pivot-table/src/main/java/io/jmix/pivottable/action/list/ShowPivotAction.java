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

package io.jmix.pivottable.action.list;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.model.CollectionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Gets items from {@link ListComponent} and shows them in new screen with pivot table component.
 */
@StudioAction(target = "io.jmix.ui.component.ListComponent", description = "Gets items from Table/DataGrid and shows them in a new screen with PivotTable component")
@ActionType(ShowPivotAction.ID)
public class ShowPivotAction extends ListAction {

    public static final String ID = "showPivot";

    protected Messages messages;
    protected ApplicationContext applicationContext;

    protected String includedProperties;
    protected String excludedProperties;

    /**
     * Provides two modes for exporting rows from component.
     */
    protected enum ShowPivotMode {
        ALL_ROWS, SELECTED_ROWS
    }

    public ShowPivotAction() {
        super(ID);
    }

    public ShowPivotAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage("actions.showPivotAction.caption");
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Set excluded properties separated by a comma. Excluded properties will not be shown in the PivotTable.
     *
     * @param properties excluded properties
     */
    @StudioPropertiesItem(
            name = "excludedProperties",
            description = "Set excluded properties separated by a comma. Excluded properties will not be shown in the PivotTable")
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
    @StudioPropertiesItem(
            name = "includedProperties", description = "Sets included properties separated by a comma. Only included properties will be shown in the PivotTable")
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

    @Override
    public void actionPerform(Component component) {
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    private void execute() {
        if (target == null) {
            throw new IllegalStateException("ShowPivotAction target is not set");
        }

        if (needShowAll()) {
            showPivotTable(ShowPivotMode.ALL_ROWS);
        } else {
            Action[] actions = new Action[]{
                    new BaseAction("actions.showPivotAction.SELECTED_ROWS")
                            .withCaption(messages.getMessage("actions.showPivotAction.SELECTED_ROWS"))
                            .withPrimary(true)
                            .withHandler(event -> showPivotTable(ShowPivotMode.SELECTED_ROWS)),
                    new BaseAction("actions.showPivotAction.ALL_ROWS")
                            .withCaption(messages.getMessage("actions.showPivotAction.ALL_ROWS"))
                            .withHandler(event -> showPivotTable(ShowPivotMode.ALL_ROWS)),
                    new DialogAction(DialogAction.Type.CANCEL)
            };

            Dialogs dialogs = ComponentsHelper.getScreenContext(target).getDialogs();
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("actions.showPivotAction.dialogTitle"))
                    .withMessage(messages.getMessage("actions.showPivotAction.dialogMessage"))
                    .withActions(actions)
                    .show();
        }
    }

    protected boolean needShowAll() {
        if (target.getSelected().isEmpty()
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

    @SuppressWarnings("unchecked")
    protected void showPivotTable(ShowPivotMode mode) {
        Frame frame = target.getFrame();
        if (frame == null) {
            throw new IllegalStateException(
                    String.format("ShowPivotAction cannot be used by component '%s' which is not added to frame",
                            target.getId()));
        }

        Collection items;
        if (ShowPivotMode.ALL_ROWS.equals(mode)) {
            if (target.getItems() instanceof ContainerDataUnit) {
                CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
                items = container.getItems();
            } else {
                items = Collections.emptyList();
            }
        } else {
            items = target.getSelected();
        }

        PivotScreenBuilder showPivotManager = applicationContext.getBean(PivotScreenBuilder.class, target);
        showPivotManager.withItems(items)
                .withIncludedProperties(parseProperties(includedProperties))
                .withExcludedProperties(parseProperties(excludedProperties))
                .build()
                .show();
    }
}