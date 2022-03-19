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

package io.jmix.pivottable.model.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PivotData implements Serializable {

    protected Integer dataNumRows;
    protected Integer dataNumCols;

    protected List<PivotDataRow> headRows;
    protected List<PivotDataRow> bodyRows;

    public Integer getDataNumRows() {
        return dataNumRows;
    }

    public void setDataNumRows(Integer dataNumRows) {
        this.dataNumRows = dataNumRows;
    }

    public Integer getDataNumCols() {
        return dataNumCols;
    }

    public void setDataNumCols(Integer dataNumCols) {
        this.dataNumCols = dataNumCols;
    }

    public List<PivotDataRow> getHeadRows() {
        return headRows;
    }

    public void setHeadRows(List<PivotDataRow> headRows) {
        this.headRows = headRows;
    }

    public List<PivotDataRow> getBodyRows() {
        return bodyRows;
    }

    public void setBodyRows(List<PivotDataRow> bodyRows) {
        this.bodyRows = bodyRows;
    }

    public List<PivotDataRow> getAllRows() {
        List<PivotDataRow> pivotDataRows = new ArrayList<>();
        pivotDataRows.addAll(headRows);
        pivotDataRows.addAll(bodyRows);
        return pivotDataRows;
    }
}
