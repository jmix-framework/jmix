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
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.pivottableflowui.component.PivotTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Jmix action to show {@link PivotTable} component.
 * When the action executes, the pivot table shows data from the component that implements {@link ListDataComponent}.
 */
@ActionType(ShowPivotTableAction.ID)
public class ShowPivotTableAction<E> extends ListDataComponentAction<ShowPivotTableAction<E>, E>
        implements ApplicationContextAware, ViewOpeningAction {

    public static final String ID = "pvttbl_showPivotTable";

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected Dialogs dialogs;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();
    protected OpenMode openMode;

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

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        return openMode;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        this.openMode = openMode;
    }

    @Nullable
    @Override
    public String getViewId() {
        return viewInitializer.getViewId();
    }

    @Override
    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    @Nullable
    @Override
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    @Override
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        viewInitializer.setViewClass(viewClass);
    }

    @Nullable
    @Override
    public RouteParametersProvider getRouteParametersProvider() {
        return viewInitializer.getRouteParametersProvider();
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider routeParameters) {
        viewInitializer.setRouteParametersProvider(routeParameters);
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        return viewInitializer.getQueryParametersProvider();
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider queryParameters) {
        viewInitializer.setQueryParametersProvider(queryParameters);
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(
            @Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Nullable
    @Override
    public <V extends View<?>> Consumer<DialogWindow.AfterCloseEvent<V>> getAfterCloseHandler() {
        return viewInitializer.getAfterCloseHandler();
    }

    @Override
    public <V extends View<?>> void setViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        viewInitializer.setViewConfigurer(viewConfigurer);
    }

    @Nullable
    @Override
    public <V extends View<?>> Consumer<V> getViewConfigurer() {
        return viewInitializer.getViewConfigurer();
    }

    /**
     * Executes the action to show the pivot table.
     */
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
                            .withText(messages.getMessage("io.jmix.pivottableflowui.action/showPivotTable.SELECTED_ROWS"))
                            .withHandler(event -> showPivotTable(ShowPivotTableMode.SELECTED_ROWS)),
                    new SecuredBaseAction("ShowPivotTableMode.ALL_ROWS")
                            .withText(messages.getMessage("io.jmix.pivottableflowui.action/showPivotTable.ALL_ROWS"))
                            .withHandler(event -> showPivotTable(ShowPivotTableMode.ALL_ROWS)),
                    new DialogAction(DialogAction.Type.CANCEL)
            };

            dialogs.createOptionDialog()
                    .withHeader(messages.getMessage("io.jmix.pivottableflowui.action/showPivotTable.dialogTitle"))
                    .withText(messages.getMessage("io.jmix.pivottableflowui.action/showPivotTable.dialogMessage"))
                    .withActions(actions)
                    .withWidth("32em")
                    .open();
        }
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

    /**
     *  Specifies whether to show all rows or prompt the user to select rows.
     *
     *  @return true if all rows should be displayed, false otherwise
     */
    protected boolean needShowAll() {
        if (target.getSelectedItems().isEmpty()
                || !(target.getItems() instanceof ContainerDataUnit)) {
            return true;
        }

        return ((ContainerDataUnit<?>) target.getItems()).getContainer().getItems().size() <= 1;
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

    protected void showPivotTable(ShowPivotTableMode mode) {
        Collection<?> items;
        if (ShowPivotTableMode.ALL_ROWS.equals(mode)) {
            if (target.getItems() instanceof ContainerDataUnit<?> containerDataUnit) {
                CollectionContainer<?> container = containerDataUnit.getContainer();
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
                .show(openMode);
    }
}