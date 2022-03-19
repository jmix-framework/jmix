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
import io.jmix.charts.model.Color;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import java.util.ArrayList;
import java.util.List;

@StudioElement(
        caption = "Export",
        xmlElement = "export",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Export extends AbstractChartObject {

    private static final long serialVersionUID = -8908356283007782587L;

    private static final String DEFAULT_FILE_NAME = "chart";

    private Color backgroundColor;

    private Boolean enabled = true;

    private String fileName = DEFAULT_FILE_NAME;

    private ExportLibs libs;

    private List<ExportMenuItem> menu;

    private ExportPosition position;

    private Boolean removeImages;

    private Boolean exportTitles;

    private Boolean exportSelection;

    private String dataDateFormat;

    private String dateFormat;

    private Boolean keyListener;

    private Boolean fileListener;

    /**
     * @return true if export functionality is enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled to false if you want to disable export functionality.
     *
     * @param enabled enabled option
     * @return export
     */
    @StudioProperty
    public Export setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return 3rd party required library settings
     */
    public ExportLibs getLibs() {
        return libs;
    }

    /**
     * Sets 3rd party required library settings.
     *
     * @param libs export libs
     * @return export
     */
    @StudioElement
    public Export setLibs(ExportLibs libs) {
        this.libs = libs;
        return this;
    }

    /**
     * @return a list of menu or submenu items
     */
    public List<ExportMenuItem> getMenu() {
        return menu;
    }

    /**
     * Sets a list of menu or submenu items.
     *
     * @param menu list of menu
     * @return export
     */
    @StudioElementsGroup(caption = "Menu", xmlElement = "menu")
    public Export setMenu(List<ExportMenuItem> menu) {
        this.menu = menu;
        return this;
    }

    /**
     * Adds menu item.
     *
     * @param menuItem menu item
     * @return export
     */
    public Export addMenuItem(ExportMenuItem menuItem) {
        if (menu == null) {
            menu = new ArrayList<>();
        }
        menu.add(menuItem);
        return this;
    }

    /**
     * @return the color for the background of the exported image
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the color for the background of the exported image.
     *
     * @param backgroundColor color
     * @return export
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Export setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets a file name that used for generated export files (an extension will be appended to it based on the export
     * format).
     *
     * @param fileName file name
     * @return export
     */
    @StudioProperty
    public Export setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * @return a position of export icon
     */
    public ExportPosition getPosition() {
        return position;
    }

    /**
     * Sets a position of export icon. Possible values: "top-left", "top-right" (default), "bottom-left",
     * "bottom-right".
     *
     * @param position position
     * @return export
     */
    @StudioProperty(type = PropertyType.ENUMERATION)
    public Export setPosition(ExportPosition position) {
        this.position = position;
        return this;
    }

    /**
     * @return true if removeImages is enabled
     */
    public Boolean getRemoveImages() {
        return removeImages;
    }

    /**
     * If true, export checks for and removes "tainted" images that area loaded from different domains.
     *
     * @param removeImages removeImages option
     * @return export
     */
    @StudioProperty
    public Export setRemoveImages(Boolean removeImages) {
        this.removeImages = removeImages;
        return this;
    }

    /**
     * @return true if exportTitles is enabled
     */
    public Boolean getExportTitles() {
        return exportTitles;
    }

    /**
     * Set exportTitles to true if the data field names should be replaced with it's dedicated title (data export only).
     *
     * @param exportTitles exportTitles option
     * @return export
     */
    @StudioProperty
    public Export setExportTitles(Boolean exportTitles) {
        this.exportTitles = exportTitles;
        return this;
    }

    /**
     * @return true if only current data selection is exported
     */
    public Boolean getExportSelection() {
        return exportSelection;
    }

    /**
     * Set exportSelection to true if you want to export the current data selection only (data export only).
     *
     * @param exportSelection exportSelection option
     * @return export
     */
    @StudioProperty
    public Export setExportSelection(Boolean exportSelection) {
        this.exportSelection = exportSelection;
        return this;
    }

    /**
     * @return data date format
     */
    public String getDataDateFormat() {
        return dataDateFormat;
    }

    /**
     * Sets data date format to convert date strings to date objects. Uses by default charts dataDateFormat (data
     * export only).
     *
     * @param dataDateFormat data date format
     * @return export
     */
    @StudioProperty
    public Export setDataDateFormat(String dataDateFormat) {
        this.dataDateFormat = dataDateFormat;
        return this;
    }

    /**
     * @return date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Formats the category field in given date format (data export only).
     *
     * @param dateFormat date format
     * @return export
     */
    @StudioProperty
    public Export setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        return this;
    }

    /**
     * @return true if keyListener is enabled
     */
    public Boolean getKeyListener() {
        return keyListener;
    }

    /**
     * If true it observes the pressed keys to undo/redo the annotations.
     *
     * @param keyListener keyListener option
     * @return export
     */
    @StudioProperty
    public Export setKeyListener(Boolean keyListener) {
        this.keyListener = keyListener;
        return this;
    }

    /**
     * @return true if fileListener is enabled
     */
    public Boolean getFileListener() {
        return fileListener;
    }

    /**
     * If true it observes the drag and drop feature and loads the dropped image file into the annotation.
     *
     * @param fileListener fileListener option
     * @return export
     */
    @StudioProperty
    public Export setFileListener(Boolean fileListener) {
        this.fileListener = fileListener;
        return this;
    }
}