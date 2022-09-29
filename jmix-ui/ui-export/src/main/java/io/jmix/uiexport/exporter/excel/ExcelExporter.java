/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package io.jmix.uiexport.exporter.excel;

import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.*;
import io.jmix.ui.component.data.meta.EntityDataGridItems;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.uiexport.action.ExportAction;
import io.jmix.uiexport.exporter.AbstractTableExporter;
import io.jmix.uiexport.exporter.ExportMode;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jmix.ui.download.DownloadFormat.XLS;
import static io.jmix.ui.download.DownloadFormat.XLSX;

/**
 * Use this class to export {@link Table} into Excel format
 * <br>Just create an instance of {@link ExportAction} with <code>withExporter</code> method.
 */
@SuppressWarnings("rawtypes")
@Component("ui_ExcelExporter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExcelExporter extends AbstractTableExporter<ExcelExporter> {

    public static enum ExportFormat {
        XLS,
        XLSX
    }

    protected static final int COL_WIDTH_MAGIC = 48;

    private static final int SPACE_COUNT = 10;

    public static final int MAX_ROW_COUNT = 65535;

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

    protected ExportFormat exportFormat = ExportFormat.XLSX;

    protected boolean isRowNumberExceeded = false;

    protected void createWorkbookWithSheet() {
        switch (exportFormat) {
            case XLS:
                wb = new HSSFWorkbook();
                break;
            case XLSX:
                wb = new XSSFWorkbook();
                break;
            default:
                throw new IllegalStateException("Unknown export format " + exportFormat);
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

    @Override
    public void exportTable(Downloader downloader, Table<Object> table, ExportMode exportMode) {
        if (downloader == null) {
            throw new IllegalArgumentException("Downloader is null");
        }

        if (table.getItems() == null) {
            throw new IllegalStateException("Table items should not be null");
        }

        @SuppressWarnings("unchecked")
        List<Table.Column<Object>> columns = Collections.unmodifiableList(table.getNotCollapsedColumns()).stream()
                .map(c -> (Table.Column<Object>) c)
                .collect(Collectors.toList());

        createWorkbookWithSheet();
        createFonts();
        createFormats();

        int r = 0;

        Row row = sheet.createRow(r);
        createAutoColumnSizers(columns.size());

        float maxHeight = sheet.getDefaultRowHeightInPoints();

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        for (Table.Column<Object> column : columns) {
            String caption = column.getCaption();

            int countOfReturnSymbols = StringUtils.countMatches(caption, "\n");
            if (countOfReturnSymbols > 0) {
                maxHeight = Math.max(maxHeight, (countOfReturnSymbols + 1) * sheet.getDefaultRowHeightInPoints());
                headerCellStyle.setWrapText(true);
            }
        }
        row.setHeightInPoints(maxHeight);

        for (int c = 0; c < columns.size(); c++) {
            Table.Column<Object> column = columns.get(c);
            String caption = column.getCaption();

            Cell cell = row.createCell(c);
            RichTextString richTextString = createStringCellValue(caption);
            richTextString.applyFont(boldFont);
            cell.setCellValue(richTextString);

            ExcelAutoColumnSizer sizer = new ExcelAutoColumnSizer();
            sizer.notifyCellValue(caption, boldFont);
            sizers[c] = sizer;

            cell.setCellStyle(headerCellStyle);
        }

        TableItems<Object> tableItems = table.getItems();

        if (exportMode == ExportMode.SELECTED && table.getSelected().size() > 0) {
            Set<Object> selected = table.getSelected();

            List<Object> ordered = tableItems.getItemIds().stream()
                    .map(tableItems::getItem)
                    .filter(selected::contains)
                    .collect(Collectors.toList());
            for (Object item : ordered) {
                if (checkIsRowNumberExceed(r)) {
                    break;
                }

                createRow(table, columns, 0, ++r, Id.of(item).getValue());
            }
        } else {
            if (table.isAggregatable() && exportAggregation
                    && hasAggregatableColumn(table)) {
                if (table.getAggregationStyle() == Table.AggregationStyle.TOP) {
                    r = createAggregatableRow(table, columns, ++r, 1);
                }
            }
            if (table instanceof TreeTable) {
                TreeTable<Object> treeTable = (TreeTable<Object>) table;
                TreeTableItems<Object> treeTableSource = (TreeTableItems<Object>) treeTable.getItems();
                if (treeTableSource != null) {
                    for (Object itemId : treeTableSource.getRootItemIds()) {
                        if (checkIsRowNumberExceed(r)) {
                            break;
                        }

                        r = createHierarchicalRow(treeTable, columns, exportExpanded, r, itemId);
                    }
                }
            } else if (table instanceof GroupTable && tableItems instanceof GroupTableItems
                    && ((GroupTableItems<Object>) tableItems).hasGroups()) {
                GroupTableItems<Object> groupTableSource = (GroupTableItems<Object>) tableItems;

                for (Object item : groupTableSource.rootGroups()) {
                    if (checkIsRowNumberExceed(r)) {
                        break;
                    }

                    r = createGroupRow((GroupTable<Object>) table, columns, ++r, (GroupInfo<?>) item, 0);
                }
            } else {
                if (tableItems != null) {
                    for (Object itemId : tableItems.getItemIds()) {
                        if (checkIsRowNumberExceed(r)) {
                            break;
                        }

                        createRow(table, columns, 0, ++r, itemId);
                    }
                }
            }
            if (table.isAggregatable() && exportAggregation
                    && hasAggregatableColumn(table)) {
                if (table.getAggregationStyle() == Table.AggregationStyle.BOTTOM) {
                    r = createAggregatableRow(table, columns, ++r, 1);
                }
            }
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

        if (isXlsMaxRowNumberExceeded()) {
            Notifications notifications = ComponentsHelper.getScreenContext(table).getNotifications();

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage("actions.warningExport.title"))
                    .withDescription(messages.getMessage("actions.warningExport.message"))
                    .show();
        }

        ByteArrayDataProvider dataProvider = new ByteArrayDataProvider(out.toByteArray(),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());
        switch (exportFormat) {
            case XLSX:
                downloader.download(dataProvider, getFileName(table) + ".xlsx", XLSX);
                break;
            case XLS:
                downloader.download(dataProvider, getFileName(table) + ".xls", XLS);
                break;
        }
    }

    @Override
    public void exportDataGrid(Downloader downloader, DataGrid<Object> dataGrid, ExportMode exportMode) {
        if (downloader == null) {
            throw new IllegalArgumentException("Downloader is null");
        }

        createWorkbookWithSheet();
        createFonts();
        createFormats();

        List<DataGrid.Column<Object>> columns = dataGrid.getVisibleColumns().stream()
                .filter(col -> !col.isCollapsed())
                .collect(Collectors.toList());

        int r = 0;

        Row row = sheet.createRow(r);
        createAutoColumnSizers(columns.size());

        float maxHeight = sheet.getDefaultRowHeightInPoints();

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        for (DataGrid.Column column : columns) {
            String caption = column.getCaption();

            int countOfReturnSymbols = StringUtils.countMatches(caption, "\n");
            if (countOfReturnSymbols > 0) {
                maxHeight = Math.max(maxHeight, (countOfReturnSymbols + 1) * sheet.getDefaultRowHeightInPoints());
                headerCellStyle.setWrapText(true);
            }
        }
        row.setHeightInPoints(maxHeight);

        for (int c = 0; c < columns.size(); c++) {
            DataGrid.Column column = columns.get(c);
            String caption = column.getCaption();

            Cell cell = row.createCell(c);
            RichTextString richTextString = createStringCellValue(caption);
            richTextString.applyFont(boldFont);
            cell.setCellValue(richTextString);

            ExcelAutoColumnSizer sizer = new ExcelAutoColumnSizer();
            sizer.notifyCellValue(caption, boldFont);
            sizers[c] = sizer;

            cell.setCellStyle(headerCellStyle);
        }

        EntityDataGridItems<Object> dataGridSource = (EntityDataGridItems) dataGrid.getItems();
        if (dataGridSource == null) {
            throw new IllegalStateException("DataGrid is not bound to data");
        }
        if (exportMode == ExportMode.SELECTED && dataGrid.getSelected().size() > 0) {
            Set<Object> selected = dataGrid.getSelected();
            List<Object> ordered = dataGridSource.getItems()
                    .filter(selected::contains)
                    .collect(Collectors.toList());
            for (Object item : ordered) {
                if (checkIsRowNumberExceed(r)) {
                    break;
                }

                createDataGridRow(dataGrid, columns, 0, ++r, Id.of(item).getValue());
            }
        } else {
            if (dataGrid instanceof TreeDataGrid) {
                TreeDataGrid treeDataGrid = (TreeDataGrid) dataGrid;
                TreeDataGridItems<Object> treeDataGridItems = (TreeDataGridItems) dataGridSource;
                List<Object> items = treeDataGridItems.getChildren(null).collect(Collectors.toList());
                for (Object item : items) {
                    if (checkIsRowNumberExceed(r)) {
                        break;
                    }

                    r = createDataGridHierarchicalRow(treeDataGrid, treeDataGridItems, columns, 0, r, item);
                }
            } else {
                for (Object itemId : dataGridSource.getItems().map(entity -> Id.of(entity).getValue()).collect(Collectors.toList())) {
                    if (checkIsRowNumberExceed(r)) {
                        break;
                    }

                    createDataGridRow(dataGrid, columns, 0, ++r, itemId);
                }
            }
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
        ByteArrayDataProvider dataProvider = new ByteArrayDataProvider(out.toByteArray(),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());
        switch (exportFormat) {
            case XLSX:
                downloader.download(dataProvider, getFileName(dataGrid) + "." + XLSX.getFileExt(), XLSX);
                break;
            case XLS:
                downloader.download(dataProvider, getFileName(dataGrid) + "." + XLS.getFileExt(), XLS);
                break;
        }
    }

    protected int createDataGridHierarchicalRow(TreeDataGrid dataGrid, TreeDataGridItems<Object> treeDataGridItems,
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

    protected void createDataGridRow(DataGrid dataGrid, List<DataGrid.Column<Object>> columns,
                                     int startColumn, int rowNumber, Object itemId) {
        if (startColumn >= columns.size()) {
            return;
        }
        Row row = sheet.createRow(rowNumber);
        Object item = dataGrid.getItems().getItem(itemId);

        int level = 0;
        if (dataGrid instanceof TreeDataGrid) {
            level = ((TreeDataGrid<Object>) dataGrid).getLevel(item);
        }
        for (int c = startColumn; c < columns.size(); c++) {
            Cell cell = row.createCell(c);

            DataGrid.Column column = columns.get(c);
            MetaPropertyPath propertyPath = null;
            if (column.getPropertyPath() != null) {
                propertyPath = column.getPropertyPath();
            }

            Object cellValue = getColumnValue(dataGrid, columns.get(c), item);

            formatValueCell(cell, cellValue, propertyPath, c, rowNumber, level, null);
        }
    }

    protected Function<Object, InstanceContainer<Object>> createInstanceContainerProvider(DataGrid dataGrid, Object item) {
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

    private short getBuiltinFormat(String format) {
        return (short) BuiltinFormats.getBuiltinFormat(format);
    }

    private String getMessage(String id) {
        return messages.getMessage(id);
    }

    protected int createHierarchicalRow(TreeTable<Object> table, List<Table.Column<Object>> columns,
                                        Boolean exportExpanded, int rowNumber, Object itemId) {
        TreeTableItems<Object> treeTableSource = (TreeTableItems<Object>) table.getItems();
        createRow(table, columns, 0, ++rowNumber, itemId);
        if (BooleanUtils.isTrue(exportExpanded) && !table.isExpanded(itemId) && !treeTableSource.getChildren(itemId).isEmpty()) {
            return rowNumber;
        } else {
            Collection<?> children = treeTableSource.getChildren(itemId);
            if (!children.isEmpty()) {
                for (Object id : children) {
                    if (BooleanUtils.isTrue(exportExpanded) && !table.isExpanded(id) && !treeTableSource.getChildren(id).isEmpty()) {
                        createRow(table, columns, 0, ++rowNumber, id);
                        continue;
                    }
                    rowNumber = createHierarchicalRow(table, columns, exportExpanded, rowNumber, id);
                }
            }
        }
        return rowNumber;
    }

    protected int createAggregatableRow(Table<Object> table, List<Table.Column<Object>> columns, int rowNumber,
                                        int aggregatableRow) {
        Row row = sheet.createRow(rowNumber);
        Map<Object, Object> results = table.getAggregationResults();

        int i = 0;
        for (Table.Column<Object> column : columns) {
            AggregationInfo agr = column.getAggregation();
            if (agr != null) {
                Object key = agr.getPropertyPath() != null ? agr.getPropertyPath() : column.getId();
                Object aggregationResult = results.get(key);
                if (aggregationResult != null) {
                    Cell cell = row.createCell(i);
                    formatValueCell(cell, aggregationResult, null, i, rowNumber, 0, null);
                }
            }
            i++;
        }
        return rowNumber;
    }

    protected int createGroupRow(GroupTable<Object> table, List<Table.Column<Object>> columns, int rowNumber,
                                 GroupInfo<?> groupInfo, int groupNumber) {
        GroupTableItems<Object> groupTableSource = (GroupTableItems<Object>) table.getItems();

        Row row = sheet.createRow(rowNumber);
        Map<Object, Object> aggregations = table.isAggregatable()
                ? table.getAggregationResults(groupInfo)
                : Collections.emptyMap();

        int i = 0;
        int initialGroupNumber = groupNumber;
        for (Table.Column<Object> column : columns) {
            if (i == initialGroupNumber) {
                Cell cell = row.createCell(i);
                Object val = groupInfo.getValue();

                if (val == null) {
                    val = getMessage("excelExporter.empty");
                }

                Collection<?> children = groupTableSource.getGroupItemIds(groupInfo);
                if (children.isEmpty()) {
                    return rowNumber;
                }

                Integer groupChildCount = null;
                if (table.isShowItemsCountForGroup()) {
                    groupChildCount = children.size();
                }

                Object captionValue = val;

                Element xmlDescriptor = column.getXmlDescriptor();
                if (xmlDescriptor != null && StringUtils.isNotEmpty(xmlDescriptor.attributeValue("captionProperty"))) {
                    String captionProperty = xmlDescriptor.attributeValue("captionProperty");

                    Object itemId = children.iterator().next();
                    Object item = groupTableSource.getItemNN(itemId);
                    captionValue = EntityValues.getValue(item, captionProperty);
                }

                GroupTable.GroupCellValueFormatter<Object> groupCellValueFormatter =
                        table.getGroupCellValueFormatter();

                if (groupCellValueFormatter != null) {
                    // disable separate "(N)" printing
                    groupChildCount = null;

                    List<Object> groupItems = groupTableSource.getGroupItemIds(groupInfo).stream()
                            .map(groupTableSource::getItem)
                            .collect(Collectors.toList());

                    GroupTable.GroupCellContext<Object> cellContext = new GroupTable.GroupCellContext<>(
                            groupInfo, captionValue, metadataTools.format(captionValue), groupItems
                    );

                    captionValue = groupCellValueFormatter.format(cellContext);
                }

                MetaPropertyPath columnId = (MetaPropertyPath) column.getId();
                formatValueCell(cell, captionValue, columnId, groupNumber++, rowNumber, 0, groupChildCount);
            } else {
                AggregationInfo agr = column.getAggregation();
                if (agr != null) {
                    Object key = agr.getPropertyPath() != null ? agr.getPropertyPath() : column.getId();
                    Object aggregationResult = aggregations.get(key);
                    if (aggregationResult != null) {
                        Cell cell = row.createCell(i);
                        formatValueCell(cell, aggregationResult, null, i, rowNumber, 0, null);
                    }
                }
            }

            i++;
        }

        int oldRowNumber = rowNumber;
        List<GroupInfo> children = groupTableSource.getChildren(groupInfo);
        if (children.size() > 0) {
            for (GroupInfo child : children) {
                rowNumber = createGroupRow(table, columns, ++rowNumber, child, groupNumber);
            }
        } else {
            Collection<?> itemIds = groupTableSource.getGroupItemIds(groupInfo);
            for (Object itemId : itemIds) {
                createRow(table, columns, groupNumber, ++rowNumber, itemId);
            }
        }

        if (checkIsRowNumberExceed(rowNumber)) {
            sheet.groupRow(oldRowNumber + 1, MAX_ROW_COUNT);
        } else {
            sheet.groupRow(oldRowNumber + 1, rowNumber);
        }

        return rowNumber;
    }

    protected void createRow(Table<Object> table, List<Table.Column<Object>> columns, int startColumn, int rowNumber, Object itemId) {
        if (startColumn >= columns.size()) {
            return;
        }

        if (rowNumber > MAX_ROW_COUNT) {
            return;
        }

        Row row = sheet.createRow(rowNumber);
        if (table.getItems() != null) {
            Object instance = table.getItems().getItem(itemId);

            int level = 0;
            if (table instanceof TreeTable) {
                level = ((TreeTable<Object>) table).getLevel(itemId);
            }

            for (int c = startColumn; c < columns.size(); c++) {
                Cell cell = row.createCell(c);

                Table.Column<Object> column = columns.get(c);
                MetaPropertyPath propertyPath = null;
                if (column.getId() instanceof MetaPropertyPath) {
                    propertyPath = (MetaPropertyPath) column.getId();
                }

                Object cellValue = getColumnValue(table, column, instance);

                formatValueCell(cell, cellValue, propertyPath, c, rowNumber, level, null);
            }
        }
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
                Class javaClass = metaPropertyPath.getRange().asDatatype().getJavaClass();
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
            Datatype datatype = null;
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
            Class javaClass = null;
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
        switch (exportFormat) {
            case XLSX:
                return new XSSFRichTextString(str);
            case XLS:
                return new HSSFRichTextString(str);
        }
        throw new IllegalStateException("Unknown export format " + exportFormat);

    }

    protected boolean checkIsRowNumberExceed(int r) {
        return isRowNumberExceeded = exportFormat == ExportFormat.XLS && r >= MAX_ROW_COUNT;
    }

    /**
     * @return true if exported table contains more than 65536 records
     */
    protected boolean isXlsMaxRowNumberExceeded() {
        return isRowNumberExceeded;
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
     * @return export aggregation
     */
    public boolean getExportAggregation() {
        return exportAggregation;
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
     * Checks that at least one column in table is aggregatable.
     *
     * @param table table
     * @return true if at least one column is aggregatable
     */
    protected boolean hasAggregatableColumn(Table<Object> table) {
        List<Table.Column<Object>> columns = table.getColumns();
        for (Table.Column<Object> column : columns) {
            if (column.getAggregation() != null) {
                return true;
            }
        }
        return false;
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
    public String getCaption() {
        return getMessage("excelExporter.caption");
    }
}