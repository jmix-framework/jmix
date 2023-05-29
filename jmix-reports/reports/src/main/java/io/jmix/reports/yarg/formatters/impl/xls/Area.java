/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.formatters.impl.xls;

import io.jmix.reports.yarg.formatters.impl.xls.Cell;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;

public class Area {

    private Cell topLeft;
    private Cell bottomRight;

    private AreaAlign align;
    private String name;

    public Area(int left, int top, int right, int bottom) {
        topLeft = new Cell(left, top);
        bottomRight = new Cell(right, bottom);
    }

    public Area(AreaReference areaReference) {
        topLeft = new Cell(areaReference.getFirstCell());
        bottomRight = new Cell(areaReference.getLastCell());
    }

    public Area(String name, AreaAlign align, AreaReference areaReference) {
        this(areaReference);
        this.name = name;
        this.align = align;
    }

    public Cell getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Cell topLeft) {
        this.topLeft = topLeft;
    }

    public Cell getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Cell bottomRight) {
        this.bottomRight = bottomRight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AreaAlign getAlign() {
        return align;
    }

    public void setAlign(AreaAlign align) {
        this.align = align;
    }

    public AreaReference toAreaReference() {
        return new AreaReference(topLeft.toCellReference(), bottomRight.toCellReference(), SpreadsheetVersion.EXCEL97);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof Area) {
            if (!getTopLeft().equals(((Area) obj).getTopLeft())) return false;
            if (!getBottomRight().equals(((Area) obj).getBottomRight())) return false;

            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + getTopLeft() + ":" + getBottomRight() + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public enum AreaAlign {
        HORIZONTAL,
        VERTICAL
    }
}
