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

package io.jmix.dashboards.model.visualmodel;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.dashboards.utils.DashboardLayoutManager;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JmixEntity(name = "dshbrd_GridLayout")
public class GridLayout extends DashboardLayout implements ContainerLayout {

    @JmixProperty
    protected Integer rows = 0;
    @JmixProperty
    protected Integer columns = 0;
    @JmixProperty
    protected Set<GridArea> areas = new HashSet<>();

    @PostConstruct
    protected void initCaption(DashboardLayoutManager layoutManager) {
        setCaption(layoutManager.getCaption(this));
    }

    public void setAreas(Set<GridArea> areas) {
        this.areas = areas;
    }

    public Set<GridArea> getAreas() {
        return areas;
    }

    public void addArea(GridArea area) {
        areas.add(area);
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer columns) {
        this.columns = columns;
    }

    @Override
    public List<DashboardLayout> getChildren() {
        return null;
    }

    @Override
    public void setChildren(List<DashboardLayout> children) {

    }

    public GridArea getGridArea(Integer col, Integer row) {
        GridArea selectedArea = null;
        Integer area = 0;
        for (GridArea gridArea : areas) {
            Integer col1 = gridArea.getCol();
            Integer col2 = gridArea.getCol2() != null ? gridArea.getCol2() : gridArea.getCol();
            Integer row1 = gridArea.getRow();
            Integer row2 = gridArea.getRow2() != null ? gridArea.getRow2() : gridArea.getRow();
            if (col1 <= col && col <= col2
                    && row1 <= row && row <= row2) {
                Integer newArea = ((row2 + 1) - row1) * ((col2 + 1) - col1);
                if (newArea > area) {
                    selectedArea = gridArea;
                    area = newArea;
                }

            }
        }
        return selectedArea;
    }

    @Override
    public void addChild(DashboardLayout child) {
        throw new UnsupportedOperationException("Use addArea() method");
    }


}
