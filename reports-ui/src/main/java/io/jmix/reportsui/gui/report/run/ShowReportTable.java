/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.gui.report.run;

import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.GroupTable;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.settings.Settings;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.ParamsMap;
import com.haulmont.chile.core.datatypes.Datatypes;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.StandardSerialization;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.JmixEntity;
import com.haulmont.cuba.core.global.Metadata;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.GroupDatasource;
import com.haulmont.cuba.gui.data.impl.DsContextImpl;
import com.haulmont.cuba.gui.data.impl.ValueGroupDatasourceImpl;
import io.jmix.reports.entity.CubaTableData;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.ui.component.*;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.xml.layout.ComponentsFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShowReportTable extends AbstractWindow {
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";
    public static final String TABLE_DATA_PARAMETER = "tableData";

    @Autowired
    protected GroupBoxLayout reportParamsBox;
    @Autowired
    protected ReportGuiManager reportGuiManager;
    @Autowired
    protected ThemeConstants themeConstants;
    @Autowired
    protected ComponentsFactory componentsFactory;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected LookupField<Report> reportLookup;
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

    @WindowParam(name = REPORT_PARAMETER)
    protected Report report;
    @WindowParam(name = TEMPLATE_CODE_PARAMETER)
    protected String templateCode;
    @WindowParam(name = PARAMS_PARAMETER)
    protected Map<String, Object> reportParameters;

    protected InputParametersFrame inputParametersFrame;
    protected DsContextImpl dsContext;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);
        dsContext = new DsContextImpl(getDsContext().getDataSupplier());

        //TODO dialog options
//        getDialogOptions()
//                .setWidth(themeConstants.get("cuba.gui.report.ShowReportTable.width"))
//                .setHeight(themeConstants.get("cuba.gui.report.ShowReportTable.height"))
//                .setResizable(true);

        if (report != null) {
            reportSelectorBox.setVisible(false);
            CubaTableData dto = (CubaTableData) serialization.deserialize((byte[]) params.get(TABLE_DATA_PARAMETER));
            drawTables(dto);
            openReportParameters(reportParameters);
        }
        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
            openReportParameters(null);
        });
    }

    private void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFrame.REPORT_PARAMETER, report,
                    InputParametersFrame.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder,
                    "report$inputParametersFrame", params);
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();
                if (templateCode == null || templateCode.isEmpty())
                    templateCode = findTableCode(report);
                ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                CubaTableData dto = (CubaTableData) serialization.deserialize(reportResult.getContent());
                drawTables(dto);
            }
        }
    }

    protected String findTableCode(Report report) {
        for (ReportTemplate reportTemplate : report.getTemplates()) {
            if (ReportOutputType.TABLE.equals(reportTemplate.getReportOutputType()))
                return reportTemplate.getCode();
        }
        return null;
    }

    protected void drawTables(CubaTableData dto) {
        Map<String, List<KeyValueEntity>> data = dto.getData();
        Map<String, Set<CubaTableData.ColumnInfo>> headerMap = dto.getHeaders();
        tablesVBoxLayout.removeAll();

        if (data == null || data.isEmpty())
            return;

        data.forEach((dataSetName, keyValueEntities) -> {
            if (keyValueEntities != null && !keyValueEntities.isEmpty()) {
                GroupDatasource dataSource = createDataSource(dataSetName, keyValueEntities, headerMap);
                Table table = createTable(dataSetName, dataSource, headerMap);

                GroupBoxLayout groupBox = componentsFactory.createComponent(GroupBoxLayout.class);
                groupBox.setCaption(dataSetName);
                groupBox.add(table);
                groupBox.expand(table);

                tablesVBoxLayout.add(groupBox);
                tablesVBoxLayout.expand(groupBox);
            }
        });
    }

    protected GroupDatasource createDataSource(String dataSetName, List<KeyValueEntity> keyValueEntities, Map<String, Set<CubaTableData.ColumnInfo>> headerMap) {
        DsBuilder dsBuilder = DsBuilder.create(getDsContext())
                .setId(dataSetName + "Ds")
                .setDataSupplier(getDsContext().getDataSupplier());
        ValueGroupDatasourceImpl ds = dsBuilder.buildValuesGroupDatasource();
        ds.setRefreshMode(CollectionDatasource.RefreshMode.NEVER);

        Set<CubaTableData.ColumnInfo> headers = headerMap.get(dataSetName);
        for (CubaTableData.ColumnInfo header : headers) {
            Class javaClass = header.getColumnClass();
            if (JmixEntity.class.isAssignableFrom(javaClass) ||
                    EnumClass.class.isAssignableFrom(javaClass) ||
                    Datatypes.get(javaClass) != null) {
                ds.addProperty(header.getKey(), javaClass);
            }
        }

        dsContext.register(ds);
        keyValueEntities.forEach(ds::includeItem);
        return ds;
    }

    protected Table createTable(String dataSetName, GroupDatasource dataSource, Map<String, Set<CubaTableData.ColumnInfo>> headerMap) {
        Table table = (Table) componentsFactory.createComponent(GroupTable.class);
        table.setId(dataSetName + "Table");

        Set<CubaTableData.ColumnInfo> headers = headerMap.get(dataSetName);

        createColumns(dataSource, table, headers);
        table.setDatasource(dataSource);
        table.setWidth("100%");
        table.setMultiSelect(true);
        table.setColumnControlVisible(false);
        table.setColumnReorderingAllowed(false);

        //TODO excel action
//        ExcelAction excelAction = ExcelAction.create(table);
//        excelAction.setFileName(dataSetName);
//        Button excelButton = componentsFactory.createComponent(Button.class);
//        excelButton.setAction(excelAction);

        ButtonsPanel buttonsPanel = componentsFactory.createComponent(ButtonsPanel.class);
        table.setButtonsPanel(buttonsPanel);
//        table.addAction(excelAction);
//        buttonsPanel.add(excelButton);
        return table;
    }

    protected void createColumns(GroupDatasource dataSource, Table table, Set<CubaTableData.ColumnInfo> headers) {
        Collection<MetaPropertyPath> paths = metadataTools.getPropertyPaths(dataSource.getMetaClass());
        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany() && !metadataTools.isSystem(property)) {
                Table.Column column = new Table.Column(metaPropertyPath);

                String propertyName = property.getName();

                CubaTableData.ColumnInfo columnInfo = getColumnInfo(propertyName, headers);
                column.setCaption(columnInfo.getCaption());
                column.setType(metaPropertyPath.getRangeJavaClass());

                Element element = DocumentHelper.createElement("column");
                column.setXmlDescriptor(element);
                if (columnInfo.getPosition() == null) {
                    table.addColumn(column);
                } else {
                    table.addColumn(column, columnInfo.getPosition());
                }
            }
        }
    }

    private CubaTableData.ColumnInfo getColumnInfo(String headerKey, Set<CubaTableData.ColumnInfo> headers) {
        return headers.stream()
                .filter(header -> headerKey.equals(header.getKey()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void applySettings(Settings settings) {
    }

    @Override
    public void saveSettings() {
    }
}
