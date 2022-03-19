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

package io.jmix.dashboards.utils;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboards.model.visualmodel.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper class to create/get info about {@link DashboardLayout} objects.
 */
@Internal
@Component("dshbrd_DashboardLayoutManager")
public class DashboardLayoutManager {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Messages messages;

    /**
     * Creates a {@link GridLayout} with specified number of rows and columns.
     * Children {@link GridArea} are created as well.
     * @param cols number of columns
     * @param rows number of rows
     * @return created grid layout
     */
    public GridLayout createGridLayout(int cols, int rows) {
        GridLayout gridLayout = metadata.create(GridLayout.class);
        gridLayout.setColumns(cols);
        gridLayout.setRows(rows);

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                GridArea gridArea = createGridArea(i, j, gridLayout);
                gridArea.setRow2(j);
                gridArea.setCol2(i);
                gridLayout.addArea(gridArea);
            }
        }
        return gridLayout;
    }

    /**
     * Creates a {@link GridArea} for a cell located in the specified column and row of specified {@link GridLayout}.
     * Nested component {@link GridCellLayout} is created as well.
     * @param col column
     * @param row row
     * @param parent parent
     * @return created grid area
     */
    public GridArea createGridArea(int col, int row, GridLayout parent) {
        GridArea gridArea = metadata.create(GridArea.class);
        gridArea.setRow(row);
        gridArea.setCol(col);

        GridCellLayout component = createGridCellLayout(col, row, parent);
        gridArea.setComponent(component);
        return gridArea;
    }

    protected GridCellLayout createGridCellLayout(int col, int row, GridLayout parent) {
        GridCellLayout gcl = metadata.create(GridCellLayout.class);
        gcl.setRow(row);
        gcl.setColumn(col);
        gcl.setCaption(getCaption(gcl));
        gcl.setParent(parent);
        return gcl;
    }

    /**
     * Creates a {@link CssLayout} with specified parameters.
     * @param responsive specified whether layout should be responsive or not
     * @param styleName style name
     * @return created CSS layout
     */
    public CssLayout createCssLayout(Boolean responsive, String styleName) {
        CssLayout cssLayout = metadata.create(CssLayout.class);
        cssLayout.setResponsive(responsive);
        cssLayout.setStyleName(styleName);
        return cssLayout;
    }

    /**
     * Creates a {@link WidgetLayout} with specified widget.
     * @param widget widget
     * @param fromTemplate specifies whether new layout is created from {@link WidgetTemplateLayout} or not
     * @return created widget layout
     */
    public WidgetLayout createWidgetLayout(Widget widget, boolean fromTemplate) {
        WidgetLayout widgetLayout = metadata.create(WidgetLayout.class);
        Widget newWidget;
        if (fromTemplate) {
            newWidget = metadataTools.copy(widget);
            newWidget.setId(UUID.randomUUID());
        } else {
            newWidget = metadata.create(widget.getClass());
            newWidget.setFragmentId(widget.getFragmentId());
            newWidget.setName(widget.getName());
        }
        newWidget.setDashboard(widget.getDashboard());
        widgetLayout.setWidget(newWidget);
        return widgetLayout;
    }

    /**
     * Creates a {@link ResponsiveLayout} with specified parameters
     * @param xs number of columns for extra small devices
     * @param sm number of columns for small devices
     * @param md number of columns for medium devices
     * @param lg number of columns for large devices
     * @return created responsive layout
     */
    public ResponsiveLayout createResponsiveLayout(int xs, int sm, int md, int lg) {
        ResponsiveLayout responsiveLayout = metadata.create(ResponsiveLayout.class);
        responsiveLayout.setXs(xs);
        responsiveLayout.setSm(sm);
        responsiveLayout.setMd(md);
        responsiveLayout.setLg(lg);
        return responsiveLayout;
    }

    /**
     * Creates a {@link ResponsiveArea} with specified component
     * @param component layout located in new responsive area
     * @return created responsive area
     */
    public ResponsiveArea createResponsiveArea(DashboardLayout component) {
        ResponsiveArea responsiveArea = metadata.create(ResponsiveArea.class);
        responsiveArea.setComponent(component);
        return responsiveArea;
    }

    /**
     * @param layout dashboard layout
     * @return localized caption for specified layout
     */
    public String getCaption(DashboardLayout layout) {
        if (layout instanceof RootLayout) {
            return messages.getMessage("Layout.root");
        } else if (layout instanceof CssLayout) {
            return messages.getMessage("Layout.css");
        } else if (layout instanceof GridCellLayout) {
            GridCellLayout gcl = (GridCellLayout) layout;
            return messages.formatMessage("", "Layout.gridCell", gcl.getRow() + 1, gcl.getColumn() + 1);
        } else if (layout instanceof GridLayout) {
            return messages.getMessage("Layout.grid");
        } else if (layout instanceof HorizontalLayout) {
            return messages.getMessage("Layout.horizontal");
        } else if (layout instanceof VerticalLayout) {
            return messages.getMessage("Layout.vertical");
        } else if (layout instanceof ResponsiveLayout) {
            return messages.getMessage("Layout.responsive");
        } else if (layout instanceof WidgetLayout) {
            Widget widget = ((WidgetLayout) layout).getWidget();
            return widget != null ? widget.getCaption() : StringUtils.EMPTY;
        }
        return StringUtils.EMPTY;
    }
}
