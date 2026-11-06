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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.aitools.AiExclusionSupport;
import io.jmix.aitoolsflowui.model.AiChatDataBlock;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
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
 * Prepares data grid rows for a message to the AI and opens the chat with them: leaves out the columns the developer
 * excluded from AI and renders the rest as a captioned Markdown table.
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
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DialogWindows dialogWindows;

    /**
     * Tells whether the rows would have any column that may be sent to the AI: a column that passes the column
     * filter, as {@link #readRows} takes it, and is not excluded from AI. Columns of a grid that is not a
     * {@link Grid} are only known when the rows are read, so such a grid is assumed to have one.
     *
     * @param context the grid and the way its rows are read
     * @return {@code true} if at least one column may be sent
     */
    public boolean hasAllowedColumns(AiGridDataContext context) {
        if (!(context.getTarget() instanceof Grid<?> grid)) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Grid<Object> objectGrid = (Grid<Object>) grid;
        return objectGrid.getColumns().stream()
                .filter(context.getColumnFilter())
                .anyMatch(column -> isAllowed(getColumnMetaPropertyPath(objectGrid, column)));
    }

    /**
     * Counts the rows that would be sent, without reading their values.
     *
     * @param context the grid and the way its rows are read
     * @param mode    {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @return number of rows
     * @throws IllegalArgumentException if the mode is {@link ExportMode#ALL_ROWS}, which is never sent
     */
    public int countRows(AiGridDataContext context, ExportMode mode) {
        return switch (mode) {
            case SELECTED_ROWS -> context.getTarget().getSelectedItems().size();
            case CURRENT_PAGE -> context.getTarget().getItems() instanceof ContainerDataUnit<?> unit
                    ? unit.getContainer().getItems().size()
                    : readRows(context, mode).getRows().size();
            default -> throw new IllegalArgumentException("Unsupported export mode: " + mode);
        };
    }

    /**
     * Reads the rows as the export would write them and leaves out the columns excluded from AI.
     *
     * @param context the grid and the way its rows are read
     * @param mode    {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @return the rows with the allowed columns only
     * @throws IllegalArgumentException if the mode is {@link ExportMode#ALL_ROWS}, which is never sent
     */
    public DataGridRows readRows(AiGridDataContext context, ExportMode mode) {
        return selectColumns(context.getExporter().readRows(context.getTarget(), mode, context.getColumnFilter()));
    }

    /**
     * Opens the AI chat as a dialog over the view of the grid, with the rows waiting to be sent with the user's next
     * question. Nothing is stored until the user sends a message; the row limit is not checked here. The chat is
     * {@link AiChatView} or a subclass of it that replaces the view.
     *
     * @param context          the grid and the way its rows are read
     * @param mode             {@link ExportMode#CURRENT_PAGE} or {@link ExportMode#SELECTED_ROWS}
     * @param conversation     conversation to continue, or {@code null} to start a new one with the first message
     * @param suggestedPrompts questions to offer with the rows, or an empty list to let the chat choose them
     * @return the opened chat dialog
     * @throws IllegalArgumentException if the mode is {@link ExportMode#ALL_ROWS}, which is never sent
     */
    public DialogWindow<AiChatView> openChat(AiGridDataContext context,
                                             ExportMode mode,
                                             @Nullable AiConversation conversation,
                                             List<String> suggestedPrompts) {
        DataGridRows rows = readRows(context, mode);
        int rowCount = rows.getRows().size();
        String title = getListTitle(context);
        View<?> origin = UiComponentUtils.getView((Component) context.getTarget());

        AiChatDataBlock dataBlock = new AiChatDataBlock(
                messages.formatMessage(MESSAGE_GROUP, "aiGridDataSupport.chipCaption", title, rowCount),
                formatDataBlock(rows, buildCaption(title, mode, rowCount)),
                getMetaClass(context),
                origin,
                suggestedPrompts);

        DialogWindow<AiChatView> window = dialogWindows.view(origin, AiChatView.class)
                .withViewConfigurer(chatView -> {
                    chatView.setConversation(conversation);
                    chatView.setDataBlock(dataBlock);
                })
                .build();
        configureChatWindow(window);
        window.open();
        return window;
    }

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
     * Builds the caption that tells the model which part of the list the user is looking at and to answer from it.
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
        return isAllowed(column.getMetaPropertyPath());
    }

    protected boolean isAllowed(@Nullable MetaPropertyPath path) {
        return path == null || !exclusionSupport.isExcluded(path);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected MetaPropertyPath getColumnMetaPropertyPath(Grid<Object> grid, Grid.Column<Object> column) {
        return grid instanceof EnhancedDataGrid<?> enhancedGrid
                ? ((EnhancedDataGrid<Object>) enhancedGrid).getColumnMetaPropertyPath(column)
                : null;
    }

    /**
     * Returns the name of the list the rows come from, shown on the chat chip and to the AI: the entity caption, or
     * the grid id when the grid has no entity.
     *
     * @param context the grid
     * @return the list name
     */
    protected String getListTitle(AiGridDataContext context) {
        MetaClass metaClass = getMetaClass(context);
        if (metaClass != null) {
            return messageTools.getEntityCaption(metaClass);
        }
        return ((Component) context.getTarget()).getId().orElse("");
    }

    @Nullable
    protected MetaClass getMetaClass(AiGridDataContext context) {
        DataUnit items = context.getTarget().getItems();
        return items instanceof ContainerDataUnit<?> unit ? unit.getContainer().getEntityMetaClass() : null;
    }

    protected void configureChatWindow(DialogWindow<AiChatView> window) {
        window.setWidth("50em");
        window.setHeight("80vh");
        window.setResizable(true);
        window.setMaximizable(true);
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
