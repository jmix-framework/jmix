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

package grid_data;

import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.gridexportflowui.exporter.DataGridRows;
import io.jmix.gridexportflowui.exporter.DataGridRows.Column;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiGridDataTestConfiguration;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAuthentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Preparation of grid data for the AI: which columns may be sent and how the rows become a Markdown block.
 */
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiGridDataTestConfiguration.class})
class AiGridDataSupportTest {

    @Autowired AiGridDataSupport support;
    @Autowired Metadata metadata;
    @Autowired MetadataTools metadataTools;

    @Test
    void selectColumns_excludedAttribute_dropped() {
        DataGridRows rows = rows(List.of(column("number", "Number", "number"),
                        column("note", "Note", "internalNote")),
                List.of("1", "private"));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Number"), headers(result));
    }

    @Test
    void selectColumns_secretAttribute_dropped() {
        DataGridRows rows = rows(List.of(column("number", "Number", "number"),
                        column("key", "Key", "apiKey")),
                List.of("1", "abc"));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Number"), headers(result));
    }

    @Test
    void selectColumns_secretAttributeOfReference_dropped() {
        DataGridRows rows = rows(List.of(column("customer", "Customer", "customer.name"),
                        column("token", "Token", "customer.secretToken")),
                List.of("Acme", "t0k3n"));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Customer"), headers(result));
    }

    @Test
    void selectColumns_pathThroughExcludedEntity_dropped() {
        DataGridRows rows = rows(List.of(column("number", "Number", "number"),
                        column("hidden", "Hidden", "hidden.name")),
                List.of("1", "x"));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Number"), headers(result));
    }

    @Test
    void selectColumns_dtoColumn_kept() {
        DataGridRows rows = rows(List.of(column("number", "Number", "number"),
                        column("customer", "Customer", "customer.name")),
                List.of("1", "Acme"));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Number", "Customer"), headers(result));
    }

    @Test
    void selectColumns_columnWithoutPath_kept() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("generated", "Generated", null)),
                List.of(List.of("value")));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Generated"), headers(result));
        assertEquals(List.of(List.of("value")), result.getRows());
    }

    @Test
    void selectColumns_columnEmptyInEveryRow_kept() {
        DataGridRows rows = new DataGridRows(
                List.of(column("number", "Number", "number"), column("customer", "Customer", "customer.name")),
                List.of(List.of("1", ""), List.of("2", "")));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("Number", "Customer"), headers(result));
        assertEquals(List.of(List.of("1", ""), List.of("2", "")), result.getRows());
    }

    @Test
    void selectColumns_droppedColumnInTheMiddle_keepsOrderAndCells() {
        DataGridRows rows = new DataGridRows(
                List.of(column("a", "A", "number"), column("b", "B", "apiKey"), column("c", "C", "customer.name")),
                List.of(List.of("a1", "b1", "c1"), List.of("a2", "b2", "c2")));

        DataGridRows result = support.selectColumns(rows);

        assertEquals(List.of("A", "C"), headers(result));
        assertEquals(List.of(List.of("a1", "c1"), List.of("a2", "c2")), result.getRows());
    }

    @Test
    void formatDataBlock_columnsAndRows_captionAndMarkdownTable() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("number", "Number", null), new Column("customer", "Customer", null)),
                List.of(List.of("1", "Acme"), List.of("2", "")));

        String block = support.formatDataBlock(rows, "Orders — 2 rows");

        assertEquals("""
                Orders — 2 rows

                | Number | Customer |
                | --- | --- |
                | 1 | Acme |
                | 2 |  |""", block);
    }

    @Test
    void formatDataBlock_pipeInCellAndHeader_escaped() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("k", "A|B", null)),
                List.of(List.of("x|y")));

        String block = support.formatDataBlock(rows, "caption");

        assertEquals("""
                caption

                | A\\|B |
                | --- |
                | x\\|y |""", block);
    }

    @Test
    void formatDataBlock_backslashBeforePipe_bothEscaped() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("k", "Text", null)),
                List.of(List.of("a\\|b")));

        String block = support.formatDataBlock(rows, "caption");

        assertEquals("""
                caption

                | Text |
                | --- |
                | a\\\\\\|b |""", block);
    }

    @Test
    void formatDataBlock_lineBreaksInCell_replacedWithSpace() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("k", "Text", null)),
                List.of(List.of("a\r\nb\nc\rd")));

        String block = support.formatDataBlock(rows, "caption");

        assertEquals("""
                caption

                | Text |
                | --- |
                | a b c d |""", block);
    }

    @Test
    void formatDataBlock_emptyHeader_usesKey() {
        DataGridRows rows = new DataGridRows(
                List.of(new Column("amount", "", null)),
                List.of(List.of("5")));

        String block = support.formatDataBlock(rows, "caption");

        assertEquals("""
                caption

                | amount |
                | --- |
                | 5 |""", block);
    }

    @Test
    void formatDataBlock_noColumns_captionOnly() {
        String block = support.formatDataBlock(new DataGridRows(List.of(), List.of()), "caption");

        assertEquals("caption", block);
    }

    @Test
    void buildCaption_currentPage_localizedWithTitleAndCount() {
        String caption = TestAuthentication.callAs("user",
                () -> support.buildCaption("Orders", ExportMode.CURRENT_PAGE, 50));

        assertEquals("Orders — rows of the current page: 50 (a fragment of the list, not all records)", caption);
    }

    @Test
    void buildCaption_selectedRows_localizedWithTitleAndCount() {
        String caption = TestAuthentication.callAs("user",
                () -> support.buildCaption("Orders", ExportMode.SELECTED_ROWS, 12));

        assertEquals("Orders — selected rows: 12 (a fragment of the list, not all records)", caption);
    }

    @Test
    void buildCaption_allRows_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> support.buildCaption("Orders", ExportMode.ALL_ROWS, 12));
    }

    private Column column(String key, String header, String propertyPath) {
        MetaPropertyPath path = metadataTools.resolveMetaPropertyPath(
                metadata.getClass("aitlsui_GridOrder"), propertyPath);
        return new Column(key, header, path);
    }

    private DataGridRows rows(List<Column> columns, List<String> cells) {
        return new DataGridRows(columns, List.of(cells));
    }

    private List<String> headers(DataGridRows rows) {
        return rows.getColumns().stream().map(Column::getHeaderText).toList();
    }
}
