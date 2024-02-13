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

package io.jmix.reportsflowui.view.run;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Actions;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.KeyValueCollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.gridexportflowui.action.ExcelExportAction;
import io.jmix.reports.entity.JmixTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.MissingDefaultTemplateException;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Route(value = "reports/tables", layout = DefaultMainViewParent.class)
@ViewController("report_ReportTableView")
@ViewDescriptor("report-table-view.xml")
@DialogMode(width = "50em", resizable = true)
public class ReportTableView extends StandardView {

    @ViewComponent
    protected HorizontalLayout parametersFrameHolder;
    @ViewComponent
    protected HorizontalLayout reportForm;
    @ViewComponent
    protected VerticalLayout tablesVBoxLayout;
    @ViewComponent
    protected Div parametersBox;

    @ViewComponent
    protected CollectionLoader<Report> reportsDl;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected StandardSerialization serialization;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected Actions actions;
    @Autowired
    protected ReportRunner reportRunner;
    @Autowired
    protected DataComponents dataComponents;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Notifications notifications;

    protected String templateCode;
    protected Map<String, Object> reportParameters;
    protected InputParametersFragment inputParametersFrame;
    protected ReportOutputDocument reportOutputDocument;

    /**
     * @deprecated use {@link #reportOutputDocument}
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    protected Report report;

    /**
     * @deprecated use {@link #reportOutputDocument}
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    protected byte[] tableData;

    /**
     * @deprecated use {@link #setReportOutputDocument(ReportOutputDocument)}
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    public void setReport(Report report) {
        this.report = report;
    }

    public void setTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
    }

    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

    /**
     * @deprecated use {@link #setReportOutputDocument(ReportOutputDocument)}
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    public void setTableData(byte[] tableData) {
        this.tableData = tableData;
    }

    public void setReportOutputDocument(ReportOutputDocument reportOutputDocument) {
        this.reportOutputDocument = reportOutputDocument;
    }

    @Subscribe("reportEntityComboBox")
    public void onReportEntityComboBoxComponentValueChange(AbstractField.ComponentValueChangeEvent<EntityComboBox<Report>, Report> event) {
        openReportParameters(event.getValue());
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        reportsDl.load();
        reportForm.setVisible(false);

        if (reportOutputDocument != null) {
            drawTables(reportOutputDocument);
        } else {
            JmixTableData dto = (JmixTableData) serialization.deserialize(tableData);
            drawTables(dto);
        }

        if (report != null) {
            openReportParameters(report);
        } else if (reportOutputDocument != null) {
            openReportParameters((Report) reportOutputDocument.getReport());
        }
    }


    private void openReportParameters(Report report) {
        parametersFrameHolder.removeAll();

        inputParametersFrame = uiComponents.create(InputParametersFragment.class);
        inputParametersFrame.setReport(report);
        inputParametersFrame.setParameters(reportParameters);

        parametersFrameHolder.add(inputParametersFrame);

        boolean isParameterBoxVisible = report.getInputParameters().stream()
                .anyMatch(param -> param.getHidden() == null || !param.getHidden());

        parametersBox.setVisible(isParameterBoxVisible);
    }

    @Subscribe("runAction")
    public void onRunAction(ActionPerformedEvent event) {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            ValidationErrors validationErrors = viewValidation.validateUiComponents(inputParametersFrame.getContent());
            if (validationErrors.isEmpty()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();
                if (templateCode == null || templateCode.isEmpty()) {
                    templateCode = findTableCode(report);
                }

                try {
                    ReportOutputDocument reportOutputDocument = reportRunner.byReportEntity(report)
                            .withParams(parameters)
                            .withTemplateCode(templateCode)
                            .run();

                    drawTables(reportOutputDocument);
                } catch (MissingDefaultTemplateException e) {
                    notifications.create(
                                    messages.getMessage("runningReportError.title"),
                                    messages.getMessage("missingDefaultTemplateError.description"))
                            .withType(Notifications.Type.ERROR)
                            .show();
                }
            } else {
                viewValidation.showValidationErrors(validationErrors);
            }
        }
    }

    @Nullable
    protected String findTableCode(Report report) {
        for (ReportTemplate reportTemplate : report.getTemplates()) {
            if (ReportOutputType.TABLE.equals(reportTemplate.getReportOutputType()))
                return reportTemplate.getCode();
        }
        return null;
    }

    /**
     * @deprecated use {@link #drawTables(ReportOutputDocument)}
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    protected void drawTables(JmixTableData dto) {
        Map<String, List<KeyValueEntity>> data = dto.getData();
        Map<String, Set<JmixTableData.ColumnInfo>> headerMap = dto.getHeaders();
        tablesVBoxLayout.removeAll();

        if (data == null || data.isEmpty()) {
            return;
        }

        JmixTabSheet jmixTabSheet = uiComponents.create(JmixTabSheet.class);
        jmixTabSheet.setWidthFull();

        data.forEach((dataSetName, keyValueEntities) -> {
            if (CollectionUtils.isNotEmpty(keyValueEntities)) {
                KeyValueCollectionContainer container = createContainer(dataSetName, keyValueEntities, headerMap);
                DataGrid<KeyValueEntity> dataGrid = createTable(dataSetName, container, headerMap);
                HorizontalLayout buttonsPanel = createButtonsPanel(reportOutputDocument, dataGrid);

                VerticalLayout verticalLayout = uiComponents.create(VerticalLayout.class);
                verticalLayout.setPadding(false);
                verticalLayout.add(buttonsPanel);
                verticalLayout.add(dataGrid);

                verticalLayout.expand(dataGrid);
                jmixTabSheet.add(dataSetName, verticalLayout);
            }
        });

        tablesVBoxLayout.add(jmixTabSheet);
        tablesVBoxLayout.expand(jmixTabSheet);
    }

    protected void drawTables(ReportOutputDocument document) {
        JmixTableData dto = (JmixTableData) serialization.deserialize(document.getContent());

        Map<String, List<KeyValueEntity>> data = dto.getData();
        Map<String, Set<JmixTableData.ColumnInfo>> headerMap = dto.getHeaders();
        tablesVBoxLayout.removeAll();

        if (data == null || data.isEmpty()) {
            return;
        }

        JmixTabSheet jmixTabSheet = uiComponents.create(JmixTabSheet.class);
        jmixTabSheet.setWidthFull();

        data.forEach((dataSetName, keyValueEntities) -> {
            if (CollectionUtils.isNotEmpty(keyValueEntities)) {
                KeyValueCollectionContainer container = createContainer(dataSetName, keyValueEntities, headerMap);
                DataGrid<KeyValueEntity> dataGrid = createTable(dataSetName, container, headerMap);
                HorizontalLayout buttonsPanel = createButtonsPanel(document, dataGrid);

                VerticalLayout verticalLayout = uiComponents.create(VerticalLayout.class);
                verticalLayout.setPadding(false);
                verticalLayout.add(buttonsPanel);
                verticalLayout.add(dataGrid);

                verticalLayout.expand(dataGrid);
                jmixTabSheet.add(dataSetName, verticalLayout);
            }
        });

        tablesVBoxLayout.add(jmixTabSheet);
        tablesVBoxLayout.expand(jmixTabSheet);
    }

    private HorizontalLayout createButtonsPanel(ReportOutputDocument document, DataGrid<KeyValueEntity> dataGrid) {
        ExcelExportAction excelExportAction = actions.create(ExcelExportAction.ID);
        excelExportAction.withFileName(document.getReport().getName());
        dataGrid.addAction(excelExportAction);

        JmixButton excelButton = uiComponents.create(JmixButton.class);
        excelButton.setAction(excelExportAction);

        HorizontalLayout buttonsPanel = uiComponents.create(HorizontalLayout.class);
        buttonsPanel.setClassName("buttons-panel");
        buttonsPanel.add(excelButton);

        return buttonsPanel;
    }

    protected KeyValueCollectionContainer createContainer(String dataSetName, List<KeyValueEntity> keyValueEntities, Map<String, Set<JmixTableData.ColumnInfo>> headerMap) {
        KeyValueCollectionContainer collectionContainer = dataComponents.createKeyValueCollectionContainer();
        collectionContainer.setItems(keyValueEntities);

        Set<JmixTableData.ColumnInfo> columnInfos = headerMap.get(dataSetName);
        columnInfos.forEach(columnInfo -> {
            Class javaClass = columnInfo.getColumnClass();
            if (Entity.class.isAssignableFrom(javaClass) ||
                EnumClass.class.isAssignableFrom(javaClass) ||
                datatypeRegistry.find(javaClass) != null) {
                collectionContainer.addProperty(columnInfo.getKey(), javaClass);
            }
        });
        return collectionContainer;
    }

    protected DataGrid<KeyValueEntity> createTable(String dataSetName, KeyValueCollectionContainer container, Map<String, Set<JmixTableData.ColumnInfo>> headerMap) {
        DataGrid<KeyValueEntity> dataGrid = uiComponents.create(DataGrid.class);
        dataGrid.setId(dataSetName + "Table");

        Set<JmixTableData.ColumnInfo> headers = headerMap.get(dataSetName);

        createColumns(container, dataGrid, headers);
        dataGrid.setItems(new ContainerDataGridItems<>(container));
        dataGrid.setWidth("100%");
        dataGrid.setMultiSelect(true);
        dataGrid.setColumnReorderingAllowed(false);
        return dataGrid;
    }

    protected void createColumns(KeyValueCollectionContainer collectionContainer, DataGrid<KeyValueEntity> table, Set<JmixTableData.ColumnInfo> headers) {
        Collection<MetaPropertyPath> paths = metadataTools.getPropertyPaths(collectionContainer.getEntityMetaClass());
        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany() && !metadataTools.isSystem(property)) {
                String propertyName = property.getName();

                JmixTableData.ColumnInfo columnInfo = getColumnInfo(propertyName, headers);
                Grid.Column<KeyValueEntity> column = table.addColumn(metaPropertyPath);
                if (columnInfo.getPosition() != null) {
                    table.setColumnPosition(column, columnInfo.getPosition());
                }
                column.setHeader(columnInfo.getCaption());
            }
        }
    }

    private JmixTableData.ColumnInfo getColumnInfo(String headerKey, Set<JmixTableData.ColumnInfo> headers) {
        return headers.stream()
                .filter(header -> headerKey.equals(header.getKey()))
                .findFirst()
                .orElse(null);
    }
}
