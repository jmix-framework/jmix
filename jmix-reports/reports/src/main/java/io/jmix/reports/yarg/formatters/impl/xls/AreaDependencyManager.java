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

import io.jmix.reports.yarg.formatters.impl.xls.Area;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaDependencyManager {
    private Map<Area, List<Area>> areasDependency = new HashMap<Area, List<Area>>();

    public Map<Area, List<Area>> getAreasDependency() {
        return areasDependency;
    }

    /**
     * Adds area dependency for formula calculations
     *
     * @param main      Main area
     * @param dependent Dependent area
     */
    public void addDependency(Area main, Area dependent) {
        List<Area> set = areasDependency.get(main);

        if (set == null) {
            set = new ArrayList<Area>();
            areasDependency.put(main, set);
        }
        set.add(dependent);
    }

    public void updateRefPtg(Area original, Area dependent, RefPtg refPtg) {
        Area areaWhichContainsPtg = getTemplateAreaByCoordinate(refPtg.getColumn(), refPtg.getRow());

        if (areaWhichContainsPtg == original) {//ptg referes inside the band - shift
            int horizontalOffset = dependent.getTopLeft().getCol() - original.getTopLeft().getCol();
            int verticalOffset = dependent.getTopLeft().getRow() - original.getTopLeft().getRow();

            refPtg.setRow(refPtg.getRow() + verticalOffset);
            refPtg.setColumn(refPtg.getColumn() + horizontalOffset);
        } else {//ptg referes outside the band - calculate
            List<Area> allDependentAreas = areasDependency.get(areaWhichContainsPtg);
            if (CollectionUtils.isEmpty(allDependentAreas)) return;

            Area dependentFromAreaWhichContainsPtg = allDependentAreas.get(0);

            int horizontalOffset = dependentFromAreaWhichContainsPtg.getTopLeft().getCol() - areaWhichContainsPtg.getTopLeft().getCol();
            int verticalOffset = dependentFromAreaWhichContainsPtg.getTopLeft().getRow() - areaWhichContainsPtg.getTopLeft().getRow();

            refPtg.setRow(refPtg.getRow() + verticalOffset);
            refPtg.setColumn(refPtg.getColumn() + horizontalOffset);
        }
    }

    public void updateAreaPtg(Area original, Area dependent, AreaPtg areaPtg) {
        boolean ptgIsInsideBand = original.getTopLeft().getRow() <= areaPtg.getFirstRow() && original.getBottomRight().getRow() >= areaPtg.getLastRow() &&
                original.getTopLeft().getCol() <= areaPtg.getFirstColumn() && original.getBottomRight().getCol() >= areaPtg.getLastColumn();

        //If areaPtg refers to cells inside the band - shift areaPtg bounds
        //If areaPtg refers to cells outside the band (refers to another band) - grow areaPtg bounds
        if (ptgIsInsideBand) {
            shiftPtgBounds(original, dependent, areaPtg);
        } else {
            growPtgBounds(original, areaPtg);
        }
    }

    public void shiftPtgBounds(Area original, Area dependent, AreaPtg areaPtg) {
        int horizontalOffset = dependent.getTopLeft().getCol() - original.getTopLeft().getCol();
        int verticalOffset = dependent.getTopLeft().getRow() - original.getTopLeft().getRow();
        areaPtg.setFirstRow(areaPtg.getFirstRow() + verticalOffset);
        areaPtg.setLastRow(areaPtg.getLastRow() + verticalOffset);
        areaPtg.setFirstColumn(areaPtg.getFirstColumn() + horizontalOffset);
        areaPtg.setLastColumn(areaPtg.getLastColumn() + horizontalOffset);
    }

    public void growPtgBounds(Area original, AreaPtg areaPtg) {
        Area ptgAreaReference = getTemplateAreaByCoordinate(areaPtg.getFirstColumn(), areaPtg.getFirstRow());

        List<Area> allDependentAreas = areasDependency.get(ptgAreaReference);

        if (CollectionUtils.isEmpty(allDependentAreas)) return;

        //find summary bounds of dependent areas
        int minRow = Integer.MAX_VALUE;
        int maxRow = -1;
        int minColumn = Integer.MAX_VALUE;
        int maxColumn = -1;

        for (Area currentArea : allDependentAreas) {
            int upperBound = currentArea.getTopLeft().getRow();
            int lowerBound = currentArea.getBottomRight().getRow();
            int leftBound = currentArea.getTopLeft().getCol();
            int rightBound = currentArea.getBottomRight().getCol();

            if (upperBound < minRow) minRow = upperBound;
            if (lowerBound > maxRow) maxRow = lowerBound;

            if (leftBound < minColumn) minColumn = leftBound;
            if (rightBound > maxColumn) maxColumn = rightBound;
        }

        //if area is horizontal - grow it vertically otherwise grow it horozontally (cause horizontal bands grow down and vertical grow left)
        if (Area.AreaAlign.HORIZONTAL == original.getAlign()) {
            areaPtg.setFirstRow(minRow);
            areaPtg.setLastRow(maxRow);
        } else {
            areaPtg.setFirstColumn(minColumn);
            areaPtg.setLastColumn(maxColumn);
        }
    }

    public Area getTemplateAreaByCoordinate(int col, int row) {
        for (Area areaReference : areasDependency.keySet()) {
            if (areaReference.getTopLeft().getCol() > col) continue;
            if (areaReference.getTopLeft().getRow() > row) continue;
            if (areaReference.getBottomRight().getCol() < col) continue;
            if (areaReference.getBottomRight().getRow() < row) continue;

            return areaReference;
        }

        return null;
    }
}