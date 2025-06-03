/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.component;

import com.codeborne.selenide.SelenideElement;
import io.jmix.masquerade.sys.DialogWindow;
import io.jmix.masquerade.sys.TagNames;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static io.jmix.masquerade.JConditions.ENABLED;
import static io.jmix.masquerade.JConditions.VISIBLE;
import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.Masquerade.$j;
import static io.jmix.masquerade.Masquerade.UI_TEST_ID;
import static io.jmix.masquerade.sys.TagNames.GRID_SORTER;
import static org.openqa.selenium.By.xpath;

/**
 * Web-element wrapper for data grid. Supports selecting all rows, getting {@link Row rows},
 * getting {@link HeaderRow header rows}, getting {@link Cell cells}, getting {@link HeaderCell header cells}
 * and clicking on rows.
 */
public class DataGrid extends AbstractComponent<DataGrid> {

    protected static final String TABLE_BODY_CSS_SELECTOR = "tbody[id='items']";
    protected static final String TABLE_HEADER_CSS_SELECTOR = "thead[id='header']";

    public DataGrid(By by) {
        super(by);
    }

    /**
     * Clicks on the select all checkbox.
     *
     * @return {@code this} to call fluent API
     */
    public DataGrid clickSelectAll() {
        getSelectAllCheckbox().sendKeys(Keys.SPACE);
        return this;
    }

    /**
     * @param index row index to get (indexing starts with {@code 0})
     * @return {@link Row} web-element wrapper
     */
    public Row getRowByIndex(int index) {
        By rowPath = xpath(".//tr[%s]".formatted(index + 1));
        return getRowBy(rowPath);
    }

    /**
     * @param by {@link By} selector to find the row
     * @return {@link Row} web-element wrapper
     */
    public Row getRowBy(By by) {
        SelenideElement rowElement = $(shadowCss(TABLE_BODY_CSS_SELECTOR, getHostCssSelector()))
                .find(by);
        return new Row(rowElement, this);
    }

    /**
     * @param index header row index to get (indexing start with {@code 0})
     * @return {@link HeaderRow} web-element wrapper
     */
    public HeaderRow getHeaderRow(int index) {
        By rowPath = xpath(".//tr[%s]".formatted(index + 1));
        return getHeaderRowBy(rowPath);
    }

    /**
     * @param by {@link By} selector to find the header row
     * @return {@link HeaderRow} web-element wrapper
     */
    public HeaderRow getHeaderRowBy(By by) {
        SelenideElement rowElement = $(shadowCss(TABLE_HEADER_CSS_SELECTOR, getHostCssSelector()))
                .find(by);
        return new HeaderRow(rowElement, this);
    }

    /**
     * @param rowIndex cell row index (indexing start with {@code 0})
     * @param colIndex cell column index (indexing start with {@code 0})
     * @return {@link Cell} web-element wrapper
     */
    public Cell getCellByIndex(int rowIndex, int colIndex) {
        By cellPath = xpath("(.//tr)[%s]//td[%s]".formatted(rowIndex + 1, colIndex + 1));
        return getCellBy(cellPath);
    }

    /**
     * @param by {@link By} select to find the cell
     * @return {@link Cell} web-element wrapper
     */
    public Cell getCellBy(By by) {
        SelenideElement cellElement = $(shadowCss(TABLE_BODY_CSS_SELECTOR, getHostCssSelector()))
                .find(by);
        return new Cell(cellElement, this);
    }

    /**
     * @param rowIndex header cell row index (indexing start with {@code 0})
     * @param colIndex header cell column index (indexing start with {@code 0})
     * @return {@link HeaderCell} web-element wrapper
     */
    public HeaderCell getHeaderCellByIndex(int rowIndex, int colIndex) {
        By cellPath = xpath("(.//tr)[%s]//th[%s]".formatted(rowIndex + 1, colIndex + 1));
        return getHeaderCellBy(cellPath);
    }

    /**
     * @param by {@link By} select to find the header cell
     * @return {@link HeaderCell} web-element wrapper
     */
    public HeaderCell getHeaderCellBy(By by) {
        SelenideElement cellElement = $(shadowCss(TABLE_HEADER_CSS_SELECTOR, getHostCssSelector()))
                .find(by);
        return new HeaderCell(cellElement, this);
    }

    /**
     * Clicks on the {@link Row} if it is possible.
     *
     * @param by {@link By} selector to find {@link Row} web-element
     * @return {@code this} to call fluent API
     */
    public DataGrid clickRow(By by) {
        shouldBe(VISIBLE)
                .shouldBe(ENABLED)
                .getRowBy(by)
                .click();

        return this;
    }

    protected String getHostCssSelector() {
        return "vaadin-grid[%s='%s']".formatted(
                UI_TEST_ID,
                getDelegate().getDomAttribute(UI_TEST_ID));
    }

    protected SelenideElement getSelectAllCheckbox() {
        return $(byChained(getBy(), xpath(".//vaadin-checkbox[@id='selectAllCheckbox']")));
    }

    protected boolean isMultiSelect() {
        return getSelectAllCheckbox().exists();
    }

    /**
     * Web-element wrapper for data grid header row. Supports getting cells.
     */
    public static class HeaderRow extends AbstractRow<HeaderRow, HeaderCell> {

        public HeaderRow(SelenideElement rowWrapper, DataGrid parent) {
            super(rowWrapper, parent);
        }

        /**
         * @param colIndex header cell column index (indexing starts with {@code 0}).
         * @return {@link HeaderCell} web-element wrapper
         */
        public HeaderCell getCellByIndex(int colIndex) {
            By cellPath = xpath("./th[%s]".formatted(colIndex + 1));
            return getCellBy(cellPath);
        }

        @Override
        protected HeaderCell createCell(SelenideElement cellElement, DataGrid parent) {
            return new HeaderCell(cellElement, parent);
        }
    }

    /**
     * Web-element wrapper for data grid header cell. Supports clicking on sorting button and filter button.
     */
    public static class HeaderCell extends AbstractCell<HeaderCell> {

        public HeaderCell(SelenideElement cellWrapper, DataGrid parent) {
            super(cellWrapper, parent);
        }

        /**
         * Clicks on sorting button to change sort direction.
         *
         * @return {@code this} to call fluent API
         */
        public HeaderCell clickSorting() {
            getCellContent().find(GRID_SORTER)
                    .shouldBe(VISIBLE)
                    .click();

            return this;
        }

        /**
         * Clicks on header filter button and returns wired filter dialog web-element wrapper.
         *
         * @param filterDialogClass filter dialog class
         * @param <T>               filter dialog class type
         * @return wired web-element wrapper for opened filter dialog
         */
        public <T extends DialogWindow<T>> T clickFilter(Class<T> filterDialogClass) {
            clickFilter();

            return $j(filterDialogClass);
        }

        /**
         * Clicks on header filter button.
         *
         * @return {@code this} to call fluent API
         */
        public HeaderCell clickFilter() {
            getCellContent()
                    .find(byChained(GRID_SORTER, xpath(".//vaadin-button[@jmix-role='column-filter-button']")))
                    .shouldBe(VISIBLE)
                    .click();
            return this;
        }
    }

    /**
     * Web-element wrapper for data grid row. Supports clicking and getting cells.
     */
    public static class Row extends AbstractRow<Row, Cell> {

        public Row(SelenideElement rowWrapper, DataGrid parent) {
            super(rowWrapper, parent);
        }

        /**
         * Clicks on the row to select, or clicks on the row-selection checkbox if it is possible.
         *
         * @return {@code this} to call fluent API
         */
        public Row click() {
            Cell cell = getCellBy(xpath("./td[@first-column]"));

            if (parent.isMultiSelect()) {
                cell.getCellContent()
                        .find(xpath("./vaadin-checkbox"))
                        .sendKeys(Keys.SPACE);
            } else {
                cell.getCellContent().click();
            }

            return this;
        }

        /**
         * @param colIndex cell column index (indexing starts with {@code 0}).
         * @return {@link Cell} web-element wrapper
         */
        public Cell getCellByIndex(int colIndex) {
            By cellPath = xpath("./td[%s]".formatted(colIndex + 1));
            return getCellBy(cellPath);
        }

        @Override
        protected Cell createCell(SelenideElement cellElement, DataGrid parent) {
            return new Cell(cellElement, parent);
        }
    }

    /**
     * Web-element wrapper for data grid cell.
     */
    public static class Cell extends AbstractCell<Cell> {

        public Cell(SelenideElement cellWrapper, DataGrid parent) {
            super(cellWrapper, parent);
        }
    }

    /**
     * Abstract class for data grid row. Supports getting cells.
     *
     * @param <T> inheritor row class type
     * @param <C> row cell class type
     */
    protected static abstract class AbstractRow<T extends AbstractRow<T, C>, C extends AbstractCell<C>>
            extends AbstractSpecificConditionHandler<T> {

        protected SelenideElement rowWrapper;
        protected DataGrid parent;

        protected AbstractRow(SelenideElement rowWrapper, DataGrid parent) {
            this.rowWrapper = rowWrapper;
            this.parent = parent;
        }

        /**
         * @param by {@link By} selector to find cell
         * @return {@link C cell} web-element wrapper
         */
        public C getCellBy(By by) {
            SelenideElement cellElement = rowWrapper.find(by);
            return createCell(cellElement, parent);
        }

        protected abstract C createCell(SelenideElement cellElement, DataGrid parent);

        @Override
        public SelenideElement getDelegate() {
            return rowWrapper;
        }
    }

    /**
     * Abstract class for data grid cell. Supports getting content (linked by a special slot with a
     * light-DOM web-element).
     *
     * @param <T> inheritor cell class type
     */
    protected static abstract class AbstractCell<T> extends AbstractSpecificConditionHandler<T> {

        public SelenideElement cellWrapper;
        public DataGrid parent;

        protected AbstractCell(SelenideElement cellWrapper, DataGrid parent) {
            this.cellWrapper = cellWrapper;
            this.parent = parent;
        }

        /**
         * @return cell related content in light-DOM
         */
        public SelenideElement getCellContent() {
            String slotName = cellWrapper.find(TagNames.SLOT)
                    .getAttribute("name");
            By xpath = xpath(".//vaadin-grid-cell-content[@slot='%s']"
                    .formatted(slotName));

            return parent.getDelegate().find(xpath);
        }

        @Override
        public SelenideElement getDelegate() {
            return cellWrapper;
        }
    }
}
