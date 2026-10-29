/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitoolsflowui.gridexport;

import io.jmix.aitools.AiExclusionSupport;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.gridexportflowui.exporter.DataGridRows;
import io.jmix.gridexportflowui.exporter.DataGridRows.Column;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Prepares data grid rows for a message to the AI: leaves out the columns the developer excluded from AI and renders
 * the rest as a captioned Markdown table.
 * <p>
 * Not a component by itself, as it names the optional grid export add-on's types; it is registered by the
 * auto-configuration that is active only when that add-on is present.
 */
@Experimental
@NullMarked
public class AiGridDataSupport {

    private static final Pattern LINE_BREAK = Pattern.compile("\r\n|\r|\n");
    private static final String MESSAGE_GROUP = "io.jmix.aitoolsflowui.gridexport";

    @Autowired
    protected AiExclusionSupport exclusionSupport;
    @Autowired
    protected Messages messages;

    /**
     * Drops the columns whose property path passes through an entity or attribute excluded from AI. A column
     * without a property path is kept, as is a column that is empty in every row.
     *
     * @param rows rows as read from the grid
     * @return the rows with the allowed columns only, in the original order
     */
    public DataGridRows selectColumns(DataGridRows rows) {
        List<Column> columns = rows.getColumns();
        List<Integer> keptIndexes = IntStream.range(0, columns.size())
                .filter(i -> isAllowed(columns.get(i)))
                .boxed()
                .toList();

        List<Column> keptColumns = keptIndexes.stream()
                .map(columns::get)
                .toList();
        List<List<String>> keptRows = rows.getRows().stream()
                .map(row -> keptIndexes.stream().map(row::get).toList())
                .toList();
        return new DataGridRows(keptColumns, keptRows);
    }

    /**
     * Renders the rows as the caption line followed by a Markdown table. The header of a column is its header text,
     * or its key if the text is empty. Line breaks in a cell become spaces, and a backslash and {@code |} are
     * escaped, so a value cannot break the table.
     *
     * @param rows    rows to render
     * @param caption line put above the table
     * @return the caption alone if there are no columns, otherwise the caption and the table
     */
    public String formatDataBlock(DataGridRows rows, String caption) {
        List<Column> columns = rows.getColumns();
        if (columns.isEmpty()) {
            return caption;
        }

        List<String> lines = new ArrayList<>();
        lines.add(caption);
        lines.add("");
        lines.add(tableRow(columns.stream().map(this::headerOf).toList()));
        lines.add(tableRow(columns.stream().map(column -> "---").toList()));
        for (List<String> row : rows.getRows()) {
            lines.add(tableRow(row));
        }
        return String.join("\n", lines);
    }

    /**
     * Builds the caption that tells the model how much of the list it is looking at.
     *
     * @param title    title of the list, e.g. the entity name or the view title
     * @param mode     how the rows were taken, {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @param rowCount number of rows sent
     * @return localized caption
     * @throws IllegalArgumentException if the mode is {@link ExportMode#ALL_ROWS}, which is never sent
     */
    public String buildCaption(String title, ExportMode mode, int rowCount) {
        String key = switch (mode) {
            case CURRENT_PAGE -> "aiGridDataSupport.caption.currentPage";
            case SELECTED_ROWS -> "aiGridDataSupport.caption.selectedRows";
            default -> throw new IllegalArgumentException("Unsupported export mode: " + mode);
        };
        return messages.formatMessage(MESSAGE_GROUP, key, title, rowCount);
    }

    protected boolean isAllowed(Column column) {
        @Nullable MetaPropertyPath path = column.getMetaPropertyPath();
        return path == null || !exclusionSupport.isExcluded(path);
    }

    protected String headerOf(Column column) {
        String header = column.getHeaderText();
        @Nullable String key = column.getKey();
        if (header.isEmpty() && key != null) {
            return key;
        }
        return header;
    }

    protected String tableRow(List<String> cells) {
        return cells.stream()
                .map(this::escapeCell)
                .collect(Collectors.joining(" | ", "| ", " |"));
    }

    protected String escapeCell(String value) {
        return LINE_BREAK.matcher(value).replaceAll(" ")
                .replace("\\", "\\\\")
                .replace("|", "\\|");
    }
}
