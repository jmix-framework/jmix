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

package io.jmix.charts.model.export;


import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@StudioElement(
        caption = "ExportMenuItem",
        xmlElement = "item",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class ExportMenuItem extends AbstractChartObject {

    private static final long serialVersionUID = 7821740492043242236L;

    private ExportFormat format;

    private String label;

    private String title;

    private String fileName;

    private PageOrientation pageOrientation;

    private Boolean pageOrigin;

    private PageSize pageSize;

    private Double quality;

    private Double multiplier;

    private Double delay;

    private Boolean lossless;

    private String delimiter;

    private Boolean quotes;

    private Boolean escape;

    private Boolean withHeader;

    private String dateFormat;

    private Boolean stringify;

    public ExportFormat getFormat() {
        return format;
    }

    @StudioProperty(type = PropertyType.ENUMERATION)
    public ExportMenuItem setFormat(ExportFormat format) {
        this.format = format;
        return this;
    }

    public String getLabel() {
        return label;
    }

    @StudioProperty
    public ExportMenuItem setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getTitle() {
        return title;
    }

    @StudioProperty
    public ExportMenuItem setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets a file name that is used for generated export files.
     *
     * @param fileName file name
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * @return page orientation of the generated PDF file
     */
    public PageOrientation getPageOrientation() {
        return pageOrientation;
    }

    /**
     * Sets page orientation of the generated PDF file. Default is portrait.
     *
     * @param pageOrientation page orientation
     * @return export menu item
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "PORTRAIT")
    public ExportMenuItem setPageOrientation(PageOrientation pageOrientation) {
        this.pageOrientation = pageOrientation;
        return this;
    }

    /**
     * @return true if the origin of the generated PDF is shown
     */
    public Boolean getPageOrigin() {
        return pageOrigin;
    }

    /**
     * Set pageOrigin to false if the origin of the generated PDF should be hidden (pdf format only).
     *
     * @param pageOrigin pageOrigin option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setPageOrigin(Boolean pageOrigin) {
        this.pageOrigin = pageOrigin;
        return this;
    }

    /**
     * @return format of PDF page
     */
    public PageSize getPageSize() {
        return pageSize;
    }

    /**
     * Sets the format of PDF page. Default value is A4.
     *
     * @param pageSize page size
     * @return export menu item
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "A4")
    public ExportMenuItem setPageSize(PageSize pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * @return a quality of the resulting JPG image
     */
    public Double getQuality() {
        return quality;
    }

    /**
     * Sets a quality of the resulting JPG image. Default value is 1. Available values 0 - 1.
     *
     * @param quality quality
     * @return export menu item
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public ExportMenuItem setQuality(Double quality) {
        this.quality = quality;
        return this;
    }

    /**
     * @return scale factor for the generated image
     */
    public Double getMultiplier() {
        return multiplier;
    }

    /**
     * Sets scale factor for the generated image.
     *
     * @param multiplier multiplier
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * @return delay by number of seconds
     */
    public Double getDelay() {
        return delay;
    }

    /**
     * Sets delay by number of seconds.
     *
     * @param delay delay
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setDelay(Double delay) {
        this.delay = delay;
        return this;
    }

    /**
     * @return true if lossless is enabled
     */
    public Boolean getLossless() {
        return lossless;
    }

    /**
     * Set lossless to true if you want enable image optimization when printing.
     *
     * @param lossless lossless option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setLossless(Boolean lossless) {
        this.lossless = lossless;
        return this;
    }

    /**
     * @return column delimiter
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets a string that is used as a column delimiter. Default value is ",".
     *
     * @param delimiter delimiter
     * @return export menu item
     */
    @StudioProperty(defaultValue = ",")
    public ExportMenuItem setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * @return true if double quotes are enabled
     */
    public Boolean getQuotes() {
        return quotes;
    }

    /**
     * Set whether to enclose strings in double quotes.
     *
     * @param quotes quotes option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setQuotes(Boolean quotes) {
        this.quotes = quotes;
        return this;
    }

    /**
     * @return true if strings isn't escaped
     */
    public Boolean getEscape() {
        return escape;
    }

    /**
     * Set escape to false if you want to escape strings.
     *
     * @param escape escape option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setEscape(Boolean escape) {
        this.escape = escape;
        return this;
    }

    /**
     * @return true if header row with column names is added
     */
    public Boolean getWithHeader() {
        return withHeader;
    }

    /**
     * Set withHeader to false if you don't want to add header row with column names. Work for CSV and XLSX formats.
     *
     * @param withHeader withHeader option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setWithHeader(Boolean withHeader) {
        this.withHeader = withHeader;
        return this;
    }

    /**
     * @return the date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets the date format. Work for XLSX format. Do not forget to set parseDates to true in {@link CategoryAxis}.
     *
     * @param dateFormat date format
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * @return true if all cell content is converted to strings
     */
    public Boolean getStringify() {
        return stringify;
    }

    /**
     * Set stringify to true if you want to convert all cell content to strings. Work for XLSX format.
     *
     * @param stringify stringify option
     * @return export menu item
     */
    @StudioProperty
    public ExportMenuItem setStringify(Boolean stringify) {
        this.stringify = stringify;
        return this;
    }
}