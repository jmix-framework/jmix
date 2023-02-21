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

package io.jmix.reportsui.screen.report.run;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.reports.entity.JmixTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.ui.Actions;
import io.jmix.ui.Fragments;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.table.ContainerGroupTableItems;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.KeyValueCollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.gridexportui.action.ExcelExportAction;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UiController("report_ShowReportTable.screen")
@UiDescriptor("show-report-table-screen.xml")
public class ShowReportTableScreen extends Screen {

    @Autowired
    protected GroupBoxLayout reportParamsBox;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected EntityComboBox<Report> reportEntityComboBox;
    @Autowired
    protected Button printReportBtn;
    @Autowired
    protected BoxLayout parametersFrameHolder;
    @Autowired
    protected HBoxLayout reportSelectorBox;
    @Autowired
    protected VBoxLayout tablesVBoxLayout;
    @Autowired
    protected StandardSerialization serialization;
    @Autowired
    protected Fragments fragments;
    @Autowired
    protected ScreenValidation screenValidation;
    @Autowired
    protected Actions actions;

    @Autowired
    private DataComponents dataComponents;
    @Autowired
    private CollectionLoader<Report> reportsDl;
    @Autowired
    private DatatypeRegistry datatypeRegistry;

    @Autowired
    protected ReportRunner reportRunner;

    protected Report report;

    protected String templateCode;

    protected Map<String, Object> reportParameters;

    protected InputParametersFragment inputParametersFrame;

    protected byte[] tableData;

    public void setReport(Report report) {
        this.report = report;
    }

    public void setTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
    }

    public void setReportParameters(Map<String, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

    public void setTableData(byte[] tableData) {
        this.tableData = tableData;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        reportsDl.load();
        if (report != null) {
            reportSelectorBox.setVisible(false);
            JmixTableData dto = (JmixTableData) serialization.deserialize(tableData);
            drawTables(dto);
            openReportParameters(reportParameters);
        }
    }

    @Subscribe("reportEntityComboBox")
    protected void onReportEntityComboBoxValueChange(HasValue.ValueChangeEvent<Report> event) {
        report = event.getValue();
        openReportParameters(null);
    }

    private void openReportParameters(@Nullable Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFragment.REPORT_PARAMETER, report,
                    InputParametersFragment.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFragment) fragments.create(this,
                    "report_InputParameters.fragment",
                    new MapScreenOptions(params))
                    .init();

            parametersFrameHolder.add(inputParametersFrame.getFragment());
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    @Subscribe("printReportBtn")
    protected void printReport(Button.ClickEvent button) {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
            if (validationErrors.isEmpty()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();
                if (templateCode == null || templateCode.isEmpty())
                    templateCode = findTableCode(report);
                ReportOutputDocument reportResult = reportRunner.byReportEntity(report)
                        .withParams(parameters)
                        .withTemplateCode(templateCode)
                        .run();
                JmixTableData dto = (JmixTableData) serialization.deserialize(reportResult.getContent());
                drawTables(dto);
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
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

    protected void drawTables(JmixTableData dto) {
        Map<String, List<KeyValueEntity>> data = dto.getData();
        Map<String, Set<JmixTableData.ColumnInfo>> headerMap = dto.getHeaders();
        tablesVBoxLayout.removeAll();

        if (data == null || data.isEmpty())
            return;

        data.forEach((dataSetName, keyValueEntities) -> {
            if (CollectionUtils.isNotEmpty(keyValueEntities)) {
                KeyValueCollectionContainer container = createContainer(dataSetName, keyValueEntities, headerMap);
                Table table = createTable(dataSetName, container, headerMap);

                GroupBoxLayout groupBox = uiComponents.create(GroupBoxLayout.class);
                groupBox.setCaption(dataSetName);
                groupBox.add(table);
                groupBox.expand(table);

                tablesVBoxLayout.add(groupBox);
                tablesVBoxLayout.expand(groupBox);
            }
        });
    }

    protected KeyValueCollectionContainer createContainer(String dataSetName, List<KeyValueEntity> keyValueEntities, Map<String, Set<JmixTableData.ColumnInfo>> headerMap) {
        KeyValueCollectionContainer collectionContainer = dataComponents.createKeyValueCollectionContainer();
        collectionContainer.setIdName(dataSetName + "Dc");
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

    protected Table createTable(String dataSetName, KeyValueCollectionContainer collectionContainer, Map<String, Set<JmixTableData.ColumnInfo>> headerMap) {
        Table table = uiComponents.create(GroupTable.class);
        table.setId(dataSetName + "Table");

        Set<JmixTableData.ColumnInfo> headers = headerMap.get(dataSetName);

        createColumns(collectionContainer, table, headers);
        table.setItems(new ContainerGroupTableItems(collectionContainer));
        table.setWidth("100%");
        table.setMultiSelect(true);
        table.setColumnControlVisible(false);
        table.setColumnReorderingAllowed(false);

        Action excelExportAction = actions.create(ExcelExportAction.ID);
        Button excelButton = uiComponents.create(Button.class);
        excelButton.setAction(excelExportAction);

        ButtonsPanel buttonsPanel = uiComponents.create(ButtonsPanel.class);
        table.setButtonsPanel(buttonsPanel);
        table.addAction(excelExportAction);
        buttonsPanel.add(excelButton);
        return table;
    }

    protected void createColumns(KeyValueCollectionContainer collectionContainer, Table table, Set<JmixTableData.ColumnInfo> headers) {
        Collection<MetaPropertyPath> paths = metadataTools.getPropertyPaths(collectionContainer.getEntityMetaClass());
        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany() && !metadataTools.isSystem(property)) {
                String propertyName = property.getName();

                JmixTableData.ColumnInfo columnInfo = getColumnInfo(propertyName, headers);

                Element element = DocumentHelper.createElement("column");
                Table.Column column = columnInfo.getPosition() == null
                        ? table.addColumn(metaPropertyPath)
                        : table.addColumn(metaPropertyPath, columnInfo.getPosition());

                column.setXmlDescriptor(element);
                column.setCaption(columnInfo.getCaption());
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
