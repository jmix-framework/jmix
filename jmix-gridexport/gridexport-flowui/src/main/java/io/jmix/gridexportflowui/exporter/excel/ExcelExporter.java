/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui.exporter.excel;

import com.google.common.base.Strings;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.AllRecordsExporter;
import io.jmix.gridexportflowui.exporter.EntityExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.flowui.download.DownloadFormat.XLSX;

/**
 * Use this class to export {@link DataGrid} into Excel format.
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
@Component("grdexp_ExcelExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExcelExporter extends AbstractDataGridExporter<ExcelExporter> {

    protected static final int COL_WIDTH_MAGIC = 48;

    private static final int SPACE_COUNT = 10;

    public static final int MAX_ROW_COUNT = SpreadsheetVersion.EXCEL2007.getMaxRows();

    protected Workbook wb;

    protected Font boldFont;
    protected Font stdFont;
    protected Sheet sheet;

    protected CellStyle timeFormatCellStyle;
    protected CellStyle dateFormatCellStyle;
    protected CellStyle dateTimeFormatCellStyle;
    protected CellStyle integerFormatCellStyle;
    protected CellStyle doubleFormatCellStyle;

    protected ExcelAutoColumnSizer[] sizers;

    protected boolean exportAggregation = true;

    protected Boolean exportExpanded = true;

    protected boolean isRowNumberExceeded = false;

    protected GridExportProperties gridExportProperties;

    protected AllRecordsExporter allRecordsExporter;

    protected Notifications notifications;

    public ExcelExporter(GridExportProperties gridExportProperties,
                         AllRecordsExporter allRecordsExporter,
                         Notifications notifications) {
        this.gridExportProperties = gridExportProperties;
        this.allRecordsExporter = allRecordsExporter;
        this.notifications = notifications;
    }

    protected void createWorkbookWithSheet() {
        if (gridExportProperties.getExcel().isUseSxssf()) {
            wb = new SXSSFWorkbook();
        } else {
            wb = new XSSFWorkbook();
        }

        sheet = wb.createSheet("Export");
    }

    protected void createFonts() {
        stdFont = wb.createFont();
        boldFont = wb.createFont();
        boldFont.setBold(true);
    }

    protected void createAutoColumnSizers(int count) {
        sizers = new ExcelAutoColumnSizer[count];
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void exportDataGrid(Downloader downloader, Grid<Object> dataGrid, ExportMode exportMode) {
        Preconditions.checkNotNullArgument(downloader, "Downloader is null");

        createWorkbookWithSheet();
        try {
            createFonts();
            createFormats();

            List<Grid.Column<Object>> columns = dataGrid.getColumns();

            int r = 0;

            Row row = sheet.createRow(r);
            createAutoColumnSizers(columns.size());

            float maxHeight = sheet.getDefaultRowHeightInPoints();

            CellStyle headerCellStyle = wb.createCellStyle();
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (DataGrid.Column<?> column : columns) {
                String caption = column.getHeaderText();

                int countOfReturnSymbols = StringUtils.countMatches(caption, "\n");
                if (countOfReturnSymbols > 0) {
                    maxHeight = Math.max(maxHeight, (countOfReturnSymbols + 1) * sheet.getDefaultRowHeightInPoints());
                    headerCellStyle.setWrapText(true);
                }
            }
            row.setHeightInPoints(maxHeight);

            for (int c = 0; c < columns.size(); c++) {
                DataGrid.Column<?> column = columns.get(c);
                String columnHeaderText = getColumnHeaderText(column);

                Cell cell = row.createCell(c);
                RichTextString richTextString = createStringCellValue(columnHeaderText);
                richTextString.applyFont(boldFont);
                cell.setCellValue(richTextString);

                ExcelAutoColumnSizer sizer = new ExcelAutoColumnSizer();
                sizer.notifyCellValue(columnHeaderText, boldFont);
                sizers[c] = sizer;

                cell.setCellStyle(headerCellStyle);
            }

            ContainerDataGridItems<Object> dataGridSource = (ContainerDataGridItems) ((ListDataComponent<Object>) dataGrid).getItems();
            if (dataGridSource == null) {
                throw new IllegalStateException("DataGrid is not bound to data");
            }

            if (exportMode == ExportMode.SELECTED_ROWS && dataGrid.getSelectedItems().size() > 0) {
                Set<Object> selected = dataGrid.getSelectedItems();
                List<Object> ordered = dataGridSource.getContainer().getItems().stream()
                        .filter(selected::contains)
                        .collect(Collectors.toList());

                for (Object item : ordered) {
                    if (checkIsRowNumberExceed(r)) {
                        break;
                    }

                    createDataGridRow(dataGrid, columns, 0, ++r, Id.of(item).getValue());
                }

            } else if (exportMode == ExportMode.CURRENT_PAGE) {
                if (dataGrid instanceof TreeDataGrid) {
                    TreeDataGrid treeDataGrid = (TreeDataGrid) dataGrid;
                    List<Object> items = dataGridSource.getContainer().getItems();

                    for (Object item : items) {
                        if (checkIsRowNumberExceed(r)) {
                            break;
                        }

                        r = createDataGridHierarchicalRow(treeDataGrid, ((ContainerTreeDataGridItems) dataGridSource),
                                columns, 0, r, item);
                    }
                } else {
                    for (Object itemId : dataGridSource.getContainer().getItems().stream()
                            .map(entity -> Id.of(entity).getValue())
                            .collect(Collectors.toList())
                    ) {
                        if (checkIsRowNumberExceed(r)) {
                            break;
                        }

                        createDataGridRow(dataGrid, columns, 0, ++r, itemId);
                    }
                }

            } else if (exportMode == ExportMode.ALL_ROWS) {
                boolean addLevelPadding = !(dataGrid instanceof TreeDataGrid);

                EntityExporter entityExporter = (entity, entityNumber) -> {
                    if (checkIsRowNumberExceed(entityNumber)) {
                        return false;
                    } else {
                        createDataGridRowForEntityInstance(
                                dataGrid,
                                columns,
                                0,
                                entityNumber,
                                entity,
                                addLevelPadding
                        );
                        return true;
                    }
                };
                allRecordsExporter.exportAll(((ListDataComponent<?>) dataGrid).getItems(), entityExporter);
            }

            for (int c = 0; c < columns.size(); c++) {
                sheet.setColumnWidth(c, sizers[c].getWidth() * COL_WIDTH_MAGIC);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                wb.write(out);
            } catch (IOException e) {
                throw new RuntimeException("Unable to write document", e);
            }

            if (isXlsxMaxRowNumberExceeded()) {
                showWarnNotification();
            }

            ByteArrayDownloadDataProvider dataProvider = new ByteArrayDownloadDataProvider(out.toByteArray(),
                    uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());


            downloader.download(dataProvider, getFileName(dataGrid) + "." + XLSX.getFileExt(), XLSX);

        } finally {
            disposeWorkBook();
        }
    }

    protected String getColumnHeaderText(DataGrid.Column<?> column) {
        String headerText = column.getHeaderText();
        if (!Strings.isNullOrEmpty(headerText)) {
            return headerText;
        } else {
            com.vaadin.flow.component.Component headerComponent = column.getHeaderComponent();
            if (headerComponent instanceof HasText hasText) {
                headerText = hasText.getText();
            }
            return Strings.nullToEmpty(headerText);
        }
    }

    protected int createDataGridHierarchicalRow(TreeGrid<?> dataGrid, ContainerTreeDataGridItems<Object> treeDataGridItems,
                                                List<DataGrid.Column<Object>> columns, int startColumn,
                                                int rowNumber, Object item) {
        if (!checkIsRowNumberExceed(rowNumber)) {
            createDataGridRow(dataGrid, columns, startColumn, ++rowNumber, Id.of(item).getValue());

            Collection<Object> children = treeDataGridItems.getChildren(item).collect(Collectors.toList());
            for (Object child : children) {
                rowNumber = createDataGridHierarchicalRow(dataGrid, treeDataGridItems, columns, startColumn, rowNumber, child);
            }
        }

        return rowNumber;
    }

    @SuppressWarnings("rawtypes")
    protected void createDataGridRow(Grid<?> dataGrid, List<DataGrid.Column<Object>> columns,
                                     int startColumn, int rowNumber, Object itemId) {
        Object entityInstance = ((ContainerDataGridItems) ((ListDataComponent) dataGrid).getItems()).getItem(itemId);
        createDataGridRowForEntityInstance(dataGrid, columns, startColumn, rowNumber, entityInstance, true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createDataGridRowForEntityInstance(Grid<?> dataGrid, List<DataGrid.Column<Object>> columns,
                                                      int startColumn, int rowNumber, Object entityInstance, boolean addLevelPadding) {
        if (startColumn >= columns.size()) {
            return;
        }
        Row row = sheet.createRow(rowNumber);

        int level = 0;
        if (addLevelPadding && dataGrid instanceof TreeDataGrid) {
            HierarchicalDataProvider dataProvider = ((TreeDataGrid<Object>) dataGrid).getDataProvider();
            level = ((ContainerTreeDataGridItems) dataProvider).getLevel(entityInstance);
        }
        for (int c = startColumn; c < columns.size(); c++) {
            Cell cell = row.createCell(c);

            DataGrid.Column<?> column = columns.get(c);
            MetaPropertyPath propertyPath = ((EnhancedDataGrid) dataGrid).getColumnMetaPropertyPath(column);

            Object cellValue = getColumnValue(dataGrid, columns.get(c), entityInstance);

            formatValueCell(cell, cellValue, propertyPath, c, rowNumber, level, null);
        }
    }

    protected Function<Object, InstanceContainer<Object>> createInstanceContainerProvider(Grid<?> dataGrid, Object item) {
        return entity -> {
            throw new UnsupportedOperationException("ExcelExporter doesn't provide instance container");
        };
    }

    protected void createFormats() {
        timeFormatCellStyle = wb.createCellStyle();
        String timeFormat = getMessage("excelExporter.timeFormat");
        timeFormatCellStyle.setDataFormat(getBuiltinFormat(timeFormat));

        dateFormatCellStyle = wb.createCellStyle();
        String dateFormat = getMessage("excelExporter.dateFormat");
        dateFormatCellStyle.setDataFormat(getBuiltinFormat(dateFormat));

        dateTimeFormatCellStyle = wb.createCellStyle();
        String dateTimeFormat = getMessage("excelExporter.dateTimeFormat");
        dateTimeFormatCellStyle.setDataFormat(getBuiltinFormat(dateTimeFormat));

        integerFormatCellStyle = wb.createCellStyle();
        String integerFormat = getMessage("excelExporter.integerFormat");
        integerFormatCellStyle.setDataFormat(getBuiltinFormat(integerFormat));

        DataFormat format = wb.createDataFormat();
        doubleFormatCellStyle = wb.createCellStyle();
        String doubleFormat = getMessage("excelExporter.doubleFormat");
        doubleFormatCellStyle.setDataFormat(format.getFormat(doubleFormat));
    }

    protected short getBuiltinFormat(String format) {
        return (short) BuiltinFormats.getBuiltinFormat(format);
    }

    protected String getMessage(String id) {
        return messages.getMessage(id);
    }

    protected String createSpaceString(int level) {
        if (level == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level * SPACE_COUNT; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    protected void formatValueCell(Cell cell, @Nullable Object cellValue, @Nullable MetaPropertyPath metaPropertyPath,
                                   int sizersIndex, int notificationRequired, int level, @Nullable Integer groupChildCount) {

        if (cellValue == null) {
            if (metaPropertyPath != null
                    && metaPropertyPath.getRange().isDatatype()) {
                Class<?> javaClass = metaPropertyPath.getRange().asDatatype().getJavaClass();
                if (Boolean.class.equals(javaClass)) {
                    cellValue = false;
                }
            } else {
                return;
            }
        }

        String childCountValue = "";
        if (groupChildCount != null) {
            childCountValue = " (" + groupChildCount + ")";
        }

        if (cellValue instanceof Number) {
            Number n = (Number) cellValue;
            Datatype<?> datatype = null;
            if (metaPropertyPath != null) {
                Range range = metaPropertyPath.getMetaProperty().getRange();
                if (range.isDatatype()) {
                    datatype = range.asDatatype();
                }
            }

            datatype = datatype == null ? datatypeRegistry.get(n.getClass()) : datatype;
            String str;
            // level is used for TreeTable, so level with 0 doesn't create spacing
            // and we should skip it
            if (sizersIndex == 0 && level > 0) {
                str = createSpaceString(level) + datatype.format(n);
                cell.setCellValue(str);
            } else {
                try {
                    str = datatype.format(n);
                    Number result = (Number) datatype.parse(str);
                    if (result != null) {
                        if (n instanceof Integer || n instanceof Long || n instanceof Byte || n instanceof Short) {
                            cell.setCellValue(result.longValue());
                            cell.setCellStyle(integerFormatCellStyle);
                        } else {
                            cell.setCellValue(result.doubleValue());
                            cell.setCellStyle(doubleFormatCellStyle);
                        }
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Unable to parse numeric value", e);
                }
                cell.setCellType(CellType.NUMERIC);
            }
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof Date) {
            Class<?> javaClass = null;
            if (metaPropertyPath != null) {
                MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
                if (metaProperty.getRange().isDatatype()) {
                    javaClass = metaProperty.getRange().asDatatype().getJavaClass();
                }
            }
            Date date = (Date) cellValue;

            cell.setCellValue(date);

            if (Objects.equals(java.sql.Time.class, javaClass)) {
                cell.setCellStyle(timeFormatCellStyle);
            } else if (Objects.equals(java.sql.Date.class, javaClass)) {
                cell.setCellStyle(dateFormatCellStyle);
            } else {
                cell.setCellStyle(dateTimeFormatCellStyle);
            }
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(Date.class).format(date);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof java.time.LocalTime) {
            java.time.LocalTime time = (java.time.LocalTime) cellValue;

            cell.setCellValue(java.sql.Time.valueOf(time));
            cell.setCellStyle(timeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(java.time.LocalTime.class).format(time);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof java.time.LocalDate) {
            java.time.LocalDate date = (java.time.LocalDate) cellValue;

            cell.setCellValue(date);
            cell.setCellStyle(dateFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(java.time.LocalDate.class).format(date);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof java.time.LocalDateTime) {
            java.time.LocalDateTime dateTime = (java.time.LocalDateTime) cellValue;

            cell.setCellValue(dateTime);
            cell.setCellStyle(dateTimeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(java.time.LocalDateTime.class).format(dateTime);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof Boolean) {
            String str = "";
            if (sizersIndex == 0) {
                str += createSpaceString(level);
            }
            str += ((Boolean) cellValue) ? getMessage("excelExporter.true") : getMessage("excelExporter.false");
            cell.setCellValue(createStringCellValue(str));
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof Enum) {
            String message = (sizersIndex == 0 ? createSpaceString(level) : "") +
                    messages.getMessage((Enum) cellValue);

            cell.setCellValue(message + childCountValue);
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(message, stdFont);
            }
        } else if (cellValue instanceof Entity) {
            Object entityVal = cellValue;
            String instanceName = metadataTools.getInstanceName(entityVal);
            String str = sizersIndex == 0 ? createSpaceString(level) + instanceName : instanceName;
            str = str + childCountValue;
            cell.setCellValue(createStringCellValue(str));
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof Collection) {
            String str = "";
            cell.setCellValue(createStringCellValue(str));
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof byte[]) {
            String str = messages.getMessage("excelExporter.bytes");
            cell.setCellValue(createStringCellValue(str));
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else {
            String strValue = cellValue == null ? "" : cellValue.toString();
            String str = sizersIndex == 0 ? createSpaceString(level) + strValue : strValue;
            str = str + childCountValue;
            cell.setCellValue(createStringCellValue(str));
            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        }
    }

    private RichTextString createStringCellValue(String str) {
        return new XSSFRichTextString(str);
    }

    protected boolean checkIsRowNumberExceed(int r) {
        return isRowNumberExceeded = r >= MAX_ROW_COUNT;
    }

    /**
     * @return true if exported table contains more than {@link ExcelExporter#MAX_ROW_COUNT} records
     */
    protected boolean isXlsxMaxRowNumberExceeded() {
        return isRowNumberExceeded;
    }

    protected void showWarnNotification() {
        notifications.create(
                        messages.getMessage(getClass(), "maximumRowsNumberExceededWarning.title"),
                        messages.formatMessage(getClass(),
                                "maximumRowsNumberExceededWarning.message",
                                MAX_ROW_COUNT))
                .withType(Notifications.Type.WARNING)
                .show();
    }

    /**
     * @return export aggregation
     */
    public boolean getExportAggregation() {
        return exportAggregation;
    }

    /**
     * Export table with aggregation
     *
     * @param exportAggregation set to true to export table with aggregation
     */
    public void setExportAggregation(boolean exportAggregation) {
        this.exportAggregation = exportAggregation;
    }

    /**
     * Export table with aggregation
     *
     * @param exportAggregation set to true to export table with aggregation
     * @return exporter instance
     */
    public ExcelExporter withExportAggregation(Boolean exportAggregation) {
        setExportAggregation(exportAggregation);
        return this;
    }

    /**
     * @return tree table export expanded
     */
    public Boolean getExportExpanded() {
        return exportExpanded;
    }

    /**
     * Export tree table with expanded rows
     *
     * @param exportExpanded set to true to export collapsed items in expanded view
     */
    public void setExportExpanded(Boolean exportExpanded) {
        this.exportExpanded = exportExpanded;
    }

    /**
     * Export tree table with expanded rows
     *
     * @param exportExpanded set to true to export collapsed items in expanded view
     * @return exporter instance
     */
    public ExcelExporter withExportExpanded(Boolean exportExpanded) {
        setExportExpanded(exportExpanded);
        return this;
    }

    @Override
    public String getLabel() {
        return getMessage("excelExporter.label");
    }

    protected void disposeWorkBook() {
        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook) wb).dispose();
        }
    }
}
