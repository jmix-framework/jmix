/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reportsflowui.view.template;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.selection.SelectionEvent;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.list.RemoveAction;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TableEditFragment extends AbstractDescriptionEditFragment<TableEditFragmentContent>
        implements ApplicationContextAware, InitializingBean {

    protected static final String TABLE_EDIT_FRAGMENT_ROOT_CLASS_NAME = "table-edit-fragment-root";

    public static final int UP = 1;
    public static final int DOWN = -1;

    protected ApplicationContext applicationContext;
    protected Metadata metadata;
    protected DataComponents dataComponents;
    protected UiComponents uiComponents;
    protected Actions actions;
    protected Notifications notifications;
    protected Messages messages;

    protected InstanceContainer<TemplateTableDescription> templateTableDescriptionDc;
    protected CollectionPropertyContainer<TemplateTableBand> templateTableBandsDc;
    protected CollectionPropertyContainer<TemplateTableColumn> templateTableColumnsDc;

    protected TableEditFragmentContent content;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        metadata = applicationContext.getBean(Metadata.class);
        dataComponents = applicationContext.getBean(DataComponents.class);
        uiComponents = applicationContext.getBean(UiComponents.class);
        actions = applicationContext.getBean(Actions.class);
        notifications = applicationContext.getBean(Notifications.class);
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        templateTableDescriptionDc = dataComponents.createInstanceContainer(TemplateTableDescription.class);
        templateTableBandsDc = dataComponents.createCollectionContainer(TemplateTableBand.class,
                templateTableDescriptionDc, "templateTableBands");
        templateTableColumnsDc = dataComponents.createCollectionContainer(TemplateTableColumn.class,
                templateTableBandsDc, "columns");

        content = createContent();
        content.setPadding(false);
        content.setClassName(TABLE_EDIT_FRAGMENT_ROOT_CLASS_NAME);

        initBands();
        initColumns();

        content.bindWithData(templateTableBandsDc, templateTableColumnsDc);
    }

    protected TableEditFragmentContent createContent() {
        return new TableEditFragmentContent(uiComponents, metadata, actions, messages);
    }

    protected void initBands() {
        content.getBandsDataGrid().addSelectionListener(this::onBandDataGridSelection);
        content.getDataGridAction(TableEditFragmentContent.CREATE_BAND_ID)
                .ifPresent(action ->
                        action.addActionPerformedListener(this::onBandCreateActionPerformed));
        content.getDataGridAction(TableEditFragmentContent.REMOVE_BAND_ID)
                .ifPresent(action -> ((RemoveAction<TemplateTableBand>) action)
                        .setAfterActionPerformedHandler(this::onBandRemoveAfterActionPerformed));
        content.getDataGridAction(TableEditFragmentContent.UP_BAND_ID)
                .ifPresent(action -> {
                    action.addEnabledRule(this::bandUpEnabledRule);
                    action.addActionPerformedListener(this::onBandUpActionPerformed);
                });
        content.getDataGridAction(TableEditFragmentContent.DOWN_BAND_ID)
                .ifPresent(action -> {
                    action.addEnabledRule(this::bandDownEnabledRule);
                    action.addActionPerformedListener(this::onBandDownActionPerformed);
                });
    }

    protected void initColumns() {
        content.getDataGridAction(TableEditFragmentContent.CREATE_COLUMN_ID)
                .ifPresent(action -> {
                    action.addEnabledRule(this::columnCreateEnabledRule);
                    action.addActionPerformedListener(this::onColumnCreateActionPerformed);
                });
        content.getDataGridAction(TableEditFragmentContent.REMOVE_COLUMN_ID)
                .ifPresent(action -> ((RemoveAction<TemplateTableColumn>) action)
                        .setAfterActionPerformedHandler(this::onColumnRemoveAfterActionPerformed));
        content.getDataGridAction(TableEditFragmentContent.UP_COLUMN_ID)
                .ifPresent(action -> {
                    action.addEnabledRule(this::columnUpEnabledRule);
                    action.addActionPerformedListener(this::onColumnUpActionPerformed);
                });
        content.getDataGridAction(TableEditFragmentContent.DOWN_COLUMN_ID)
                .ifPresent(action -> {
                    action.addEnabledRule(this::columnDownEnabledRule);
                    action.addActionPerformedListener(this::onColumnDownActionPerformed);
                });
    }

    protected void onBandDataGridSelection(SelectionEvent<Grid<TemplateTableBand>, TemplateTableBand> event) {
        content.getDataGridAction(TableEditFragmentContent.CREATE_COLUMN_ID)
                .ifPresent(Action::refreshState);
    }

    protected void onBandCreateActionPerformed(ActionPerformedEvent event) {
        TemplateTableBand templateTableBand = metadata.create(TemplateTableBand.class);
        templateTableBand.setPosition(templateTableBandsDc.getItems().size());
        templateTableBandsDc.getMutableItems().add(templateTableBand);

        content.getBandsDataGrid().select(templateTableBand);
        content.getBandsDataGrid().getEditor().editItem(templateTableBand);
    }

    protected void onBandRemoveAfterActionPerformed(RemoveOperation.AfterActionPerformedEvent<TemplateTableBand> event) {
        TemplateTableBand deletedColumn = event.getItems().iterator().next();
        int deletedPosition = deletedColumn.getPosition();

        for (TemplateTableBand templateTableBand : templateTableBandsDc.getItems()) {
            if (templateTableBand.getPosition() > deletedPosition) {
                int currentPosition = templateTableBand.getPosition();
                templateTableBand.setPosition(currentPosition - 1);
            }
        }
    }

    protected void onBandUpActionPerformed(ActionPerformedEvent event) {
        changeOrderBandsOfIndexes(UP);
    }

    protected boolean bandUpEnabledRule() {
        TemplateTableBand item = content.getBandsDataGrid().getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getPosition() > 0;
    }

    protected void onBandDownActionPerformed(ActionPerformedEvent event) {
        changeOrderBandsOfIndexes(DOWN);
    }

    protected boolean bandDownEnabledRule() {
        TemplateTableBand item = content.getBandsDataGrid().getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getPosition() < (templateTableBandsDc.getItems().size() - 1);
    }

    protected void onColumnCreateActionPerformed(ActionPerformedEvent event) {
        TemplateTableBand selectBand = content.getBandsDataGrid().getSingleSelectedItem();

        if (selectBand != null) {
            TemplateTableColumn item = metadata.create(TemplateTableColumn.class);
            item.setPosition(templateTableColumnsDc.getItems().size());

            templateTableColumnsDc.getMutableItems().add(item);

            content.getColumnsDataGrid().select(item);
            content.getColumnsDataGrid().getEditor().editItem(item);
        } else {
            notifications.create(messages.getMessage(getClass(), "notification.bandRequired.header"))
                    .show();
        }
    }

    protected boolean columnCreateEnabledRule() {
        TemplateTableBand selectBand = content.getBandsDataGrid().getSingleSelectedItem();
        return selectBand != null;
    }

    protected void onColumnRemoveAfterActionPerformed(RemoveOperation.AfterActionPerformedEvent<TemplateTableColumn> event) {
        TemplateTableColumn deletedColumn = event.getItems().iterator().next();
        int deletedPosition = deletedColumn.getPosition();

        for (TemplateTableColumn templateTableColumn : templateTableColumnsDc.getItems()) {
            if (templateTableColumn.getPosition() > deletedPosition) {
                int currentPosition = templateTableColumn.getPosition();
                templateTableColumn.setPosition(currentPosition - 1);
            }
        }
    }

    protected void onColumnUpActionPerformed(ActionPerformedEvent event) {
        changeOrderColumnsOfIndexes(UP);
    }

    protected boolean columnUpEnabledRule() {
        TemplateTableColumn item = content.getColumnsDataGrid().getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getPosition() > 0;
    }

    protected void onColumnDownActionPerformed(ActionPerformedEvent event) {
        changeOrderColumnsOfIndexes(DOWN);
    }

    protected boolean columnDownEnabledRule() {
        TemplateTableColumn item = content.getColumnsDataGrid().getSingleSelectedItem();
        if (item == null) {
            return false;
        }
        return item.getPosition() < (templateTableColumnsDc.getItems().size() - 1);
    }

    @Override
    public void setReportTemplate(@Nullable ReportTemplate reportTemplate) {
        super.setReportTemplate(reportTemplate);

        if (reportTemplate.getTemplateTableDescription() == null) {
            templateTableDescriptionDc.setItem(createDefaultTemplateTableDescription());
        } else {
            templateTableDescriptionDc.setItem(reportTemplate.getTemplateTableDescription());
        }
    }

    @Override
    protected TableEditFragmentContent initContent() {
        return content;
    }

    @Override
    public boolean isSupportPreview() {
        return false;
    }

    @Override
    public boolean isApplicable(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.TABLE;
    }

    @Override
    public boolean applyChanges() {
        getReportTemplate().setTemplateTableDescription(templateTableDescriptionDc.getItem());

        for (TemplateTableBand band : templateTableBandsDc.getItems()) {
            if (band.getBandName() == null) {
                notifications.create(messages.getMessage(
                                getClass(), "notification.bandTableOrColumnTableRequired.header"))
                        .withPosition(Notification.Position.BOTTOM_END)
                        .show();
                return false;
            }

            for (TemplateTableColumn column : band.getColumns()) {
                if (column.getKey() == null || column.getCaption() == null) {
                    notifications.create(messages.getMessage(
                                    getClass(), "notification.bandTableOrColumnTableRequired.header"))
                            .withPosition(Notification.Position.BOTTOM_END)
                            .show();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void initPreviewContent(VerticalLayout previewBox) {
        // do nothing
    }

    protected TemplateTableDescription createDefaultTemplateTableDescription() {
        return metadata.create(TemplateTableDescription.class);
    }

    protected void changeOrderBandsOfIndexes(int order) {
        List<TemplateTableBand> items = templateTableBandsDc.getMutableItems();
        TemplateTableBand currentItem = templateTableBandsDc.getItem();
        TemplateTableBand itemToSwap = IterableUtils.find(items,
                e -> e.getPosition().equals(currentItem.getPosition() - order)
        );

        int currentItemPosition = currentItem.getPosition();
        currentItem.setPosition(itemToSwap.getPosition());
        itemToSwap.setPosition(currentItemPosition);

        Collections.swap(items, itemToSwap.getPosition(), currentItem.getPosition());

        refreshBandActionStates();
    }

    protected void refreshBandActionStates() {
        content.getBandsDataGridActions().forEach(Action::refreshState);
    }

    private void changeOrderColumnsOfIndexes(int order) {
        List<TemplateTableColumn> items = templateTableColumnsDc.getMutableItems();
        TemplateTableColumn currentItem = templateTableColumnsDc.getItem();
        TemplateTableColumn itemToSwap = IterableUtils.find(items,
                e -> e.getPosition().equals(currentItem.getPosition() - order)
        );

        int currentItemPosition = currentItem.getPosition();
        currentItem.setPosition(itemToSwap.getPosition());
        itemToSwap.setPosition(currentItemPosition);

        Collections.swap(items, itemToSwap.getPosition(), currentItem.getPosition());

        refreshColumnActionStates();
    }

    protected void refreshColumnActionStates() {
        content.getColumnsDataGridActions().forEach(Action::refreshState);
    }
}
