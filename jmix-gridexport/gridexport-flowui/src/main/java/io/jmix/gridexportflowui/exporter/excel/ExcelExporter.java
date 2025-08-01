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
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.kit.component.grid.JmixTreeGrid;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoader;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoaderFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
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
    protected CellStyle decimalFormatCellStyle;

    protected ExcelAutoColumnSizer[] sizers;

    protected boolean exportAggregation = true;

    protected Boolean exportExpanded = true;

    protected boolean isRowNumberExceeded = false;

    protected GridExportProperties gridExportProperties;
    protected Notifications notifications;
    protected AllEntitiesLoaderFactory allEntitiesLoaderFactory;
    protected CurrentAuthentication currentAuthentication;
    protected DateTimeTransformations dateTimeTransformations;

    public ExcelExporter(GridExportProperties gridExportProperties,
                         Notifications notifications,
                         AllEntitiesLoaderFactory allEntitiesLoaderFactory,
                         CurrentAuthentication currentAuthentication,
                         DateTimeTransformations dateTimeTransformations) {
        this.gridExportProperties = gridExportProperties;
        this.notifications = notifications;
        this.allEntitiesLoaderFactory = allEntitiesLoaderFactory;
        this.currentAuthentication = currentAuthentication;
        this.dateTimeTransformations = dateTimeTransformations;
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
    public void exportDataGrid(Downloader downloader, Grid<Object> dataGrid, ExportMode exportMode,
                               Predicate<Grid.Column<Object>> columnFilter) {
        Preconditions.checkNotNullArgument(downloader, "Downloader is null");

        createWorkbookWithSheet();
        try {
            createFonts();
            createFormats();

            List<Grid.Column<Object>> columns = getColumns(dataGrid, columnFilter);

            int r = 0;

            Row row = sheet.createRow(r);
            createAutoColumnSizers(columns.size());

            float maxHeight = sheet.getDefaultRowHeightInPoints();

            CellStyle headerCellStyle = wb.createCellStyle();
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            for (DataGrid.Column<?> column : columns) {
                String columnHeaderText = getColumnHeaderText(column);

                int countOfReturnSymbols = StringUtils.countMatches(columnHeaderText, "\n");
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
                if (dataGrid instanceof TreeDataGrid treeDataGrid
                        && dataGridSource instanceof ContainerTreeDataGridItems containerTreeDataGridItems) {

                    // only top level items
                    List<Object> items = containerTreeDataGridItems.getContainer().getItems().stream()
                            .filter(entity -> containerTreeDataGridItems.getLevel(entity) == 0)
                            .toList();

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

                AllEntitiesLoader entitiesLoader = allEntitiesLoaderFactory.getEntitiesLoader();
                entitiesLoader.loadAll(
                        ((ListDataComponent<?>) dataGrid).getItems(),
                        context -> {
                            if (!checkIsRowNumberExceed(context.getEntityNumber())) {
                                createDataGridRowForEntityInstance(
                                        dataGrid,
                                        columns,
                                        0,
                                        context.getEntityNumber(),
                                        context.getEntity(),
                                        addLevelPadding);
                                return true;
                            }
                            return false;
                        });
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
        String headerText = !isHeaderRowAppended(column)
                ? column.getHeaderText()
                : getDefaultHeaderText(column);

        if (!Strings.isNullOrEmpty(headerText)) {
            return headerText;
        }

        com.vaadin.flow.component.Component headerComponent = getDefaultHeaderComponent(column);

        if (headerComponent instanceof HasText hasText) {
            headerText = hasText.getText();
        } else if (headerComponent instanceof DataGridHeaderFilter dataGridHeaderFilter
                && dataGridHeaderFilter.getHeader() instanceof HasText hasText) {
            headerText = hasText.getText();
        }
        return Strings.nullToEmpty(headerText);
    }

    protected boolean isHeaderRowAppended(DataGrid.Column<?> column) {
        return column.getParent().isPresent() && !(column.getParent().get() instanceof DataGrid<?>);
    }

    @Nullable
    protected String getDefaultHeaderText(Grid.Column<?> column) {
        HeaderRow defaultHeaderRow = null;

        if (column.getGrid() instanceof JmixGrid<?> jmixGrid) {
            defaultHeaderRow = jmixGrid.getDefaultHeaderRow();
        } else if (column.getGrid() instanceof JmixTreeGrid<?> jmixTreeGrid) {
            defaultHeaderRow = jmixTreeGrid.getDefaultHeaderRow();
        }

        return defaultHeaderRow == null
                ? null
                : defaultHeaderRow.getCell(column).getText();
    }

    @Nullable
    protected com.vaadin.flow.component.Component getDefaultHeaderComponent(DataGrid.Column<?> column) {
        HeaderRow defaultHeaderRow = null;

        if (column.getGrid() instanceof JmixGrid<?> jmixGrid) {
            defaultHeaderRow = jmixGrid.getDefaultHeaderRow();
        } else if (column.getGrid() instanceof JmixTreeGrid<?> jmixTreeGrid) {
            defaultHeaderRow = jmixTreeGrid.getDefaultHeaderRow();
        }

        return defaultHeaderRow == null
                ? null
                : defaultHeaderRow.getCell(column).getComponent();
    }

    protected int createDataGridHierarchicalRow(TreeGrid<?> dataGrid, ContainerTreeDataGridItems<Object> treeDataGridItems,
                                                List<DataGrid.Column<Object>> columns, int startColumn,
                                                int rowNumber, Object item) {
        if (!checkIsRowNumberExceed(rowNumber)) {
            createDataGridRow(dataGrid, columns, startColumn, ++rowNumber, Id.of(item).getValue());

            Collection<Object> children = treeDataGridItems.getChildren(item).toList();
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
        DataFormat dataFormat = wb.getCreationHelper().createDataFormat();

        String timeFormat = getMessage("excelExporter.timeFormat");
        short timeDataFormat = getBuiltinFormat(timeFormat) == -1
                ? dataFormat.getFormat(timeFormat)
                : getBuiltinFormat(timeFormat);
        timeFormatCellStyle.setDataFormat(timeDataFormat);

        dateFormatCellStyle = wb.createCellStyle();
        String dateFormat = getMessage("excelExporter.dateFormat");
        short dateDataFormat = getBuiltinFormat(dateFormat) == -1
                ? dataFormat.getFormat(dateFormat)
                : getBuiltinFormat(dateFormat);
        dateFormatCellStyle.setDataFormat(dateDataFormat);

        dateTimeFormatCellStyle = wb.createCellStyle();
        String dateTimeFormat = getMessage("excelExporter.dateTimeFormat");
        short dateTimeDataFormat = getBuiltinFormat(dateTimeFormat) == -1
                ? dataFormat.getFormat(dateTimeFormat)
                : getBuiltinFormat(dateTimeFormat);
        dateTimeFormatCellStyle.setDataFormat(dateTimeDataFormat);

        integerFormatCellStyle = wb.createCellStyle();
        String integerFormat = getMessage("excelExporter.integerFormat");
        integerFormatCellStyle.setDataFormat(getBuiltinFormat(integerFormat));

        DataFormat doubleDataFormat = wb.createDataFormat();
        doubleFormatCellStyle = wb.createCellStyle();
        String doubleFormat = getMessage("excelExporter.doubleFormat");
        doubleFormatCellStyle.setDataFormat(doubleDataFormat.getFormat(doubleFormat));

        DataFormat decimalDataFormat = wb.createDataFormat();
        decimalFormatCellStyle = wb.createCellStyle();
        String decimalFormat = getMessage("excelExporter.decimalFormat");
        decimalFormatCellStyle.setDataFormat(decimalDataFormat.getFormat(decimalFormat));
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

    @SuppressWarnings({"rawtypes", "unchecked"})
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
                            cell.setCellValue(n.doubleValue());
                            cell.setCellStyle(n instanceof BigDecimal
                                    ? decimalFormatCellStyle
                                    : doubleFormatCellStyle);
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

            if (Objects.equals(Time.class, javaClass)) {
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
        } else if (cellValue instanceof LocalTime) {
            LocalTime time = (LocalTime) cellValue;

            cell.setCellValue(Time.valueOf(time));
            cell.setCellStyle(timeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(LocalTime.class).format(time);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof LocalDate) {
            LocalDate date = (LocalDate) cellValue;

            cell.setCellValue(date);
            cell.setCellStyle(dateFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(LocalDate.class).format(date);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) cellValue;

            cell.setCellValue(dateTime);
            cell.setCellStyle(dateTimeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(LocalDateTime.class).format(dateTime);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof OffsetTime offsetTime) {
            LocalTime time = dateTimeTransformations.transformToLocalTime(offsetTime);

            cell.setCellValue(Time.valueOf(time));
            cell.setCellStyle(timeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(LocalTime.class).format(time);
                sizers[sizersIndex].notifyCellValue(str, stdFont);
            }
        } else if (cellValue instanceof OffsetDateTime offsetDateTime) {
            LocalDateTime dateTime = (LocalDateTime) dateTimeTransformations.transformToType(offsetDateTime,
                    LocalDateTime.class,
                    currentAuthentication.getTimeZone().toZoneId());

            cell.setCellValue(dateTime);
            cell.setCellStyle(dateTimeFormatCellStyle);

            if (sizers[sizersIndex].isNotificationRequired(notificationRequired)) {
                String str = datatypeRegistry.get(LocalDateTime.class).format(dateTime);
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
        } else if (cellValue instanceof Collection<?> entitiesCollection) {
            // only a collection of entities is supported
            String instanceName = entitiesCollection.stream()
                    .map(metadataTools::getInstanceName)
                    .collect(Collectors.joining(", "));

            String str = sizersIndex == 0 ? createSpaceString(level) + instanceName : instanceName;
            str = str + childCountValue;
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

    protected List<Grid.Column<Object>> getColumns(Grid<Object> grid, Predicate<Grid.Column<Object>> columnFilter) {
        List<Grid.Column<Object>> columns = grid.getColumns().stream()
                .filter(columnFilter)
                .toList();

        List<Grid.Column<Object>> allColumns = getOrderedColumns(grid);
        if (allColumns.isEmpty()) {
            return columns;
        }

        return allColumns.stream()
                .filter(columns::contains)
                .toList();
    }

    /**
     * Returns a list of all columns (including those hidden by security) in the correct order for Jmix extensions
     * of {@link Grid}. Otherwise, it returns {@link Grid#getColumns()}.
     *
     * @param grid grid from which to get all columns
     * @return all (with hidden by security) columns list that has correct order
     */
    protected List<Grid.Column<Object>> getOrderedColumns(Grid<Object> grid) {
        if (grid instanceof DataGrid<Object> dataGrid) {
            return dataGrid.getAllColumns();
        } else if (grid instanceof TreeDataGrid<Object> treeDataGrid) {
            return treeDataGrid.getAllColumns();
        }
        return grid.getColumns();
    }
}
