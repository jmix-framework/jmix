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

package io.jmix.reports.yarg.formatters.impl.xlsx.hints;

import io.jmix.reports.yarg.structure.BandData;
import org.xlsx4j.sml.Cell;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractXlsxHint implements XlsxHint {

    protected static class DataObject {
        protected final Cell templateCell;
        protected final Cell resultCell;
        protected final BandData bandData;
        protected final List<String> params;

        public DataObject(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
            this.templateCell = templateCell;
            this.resultCell = resultCell;
            this.bandData = bandData;
            this.params = params;
        }
    }

    protected List<DataObject> data = new ArrayList<DataObject>();

    @Override
    public void add(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
        data.add(new DataObject(templateCell, resultCell, bandData, params));
    }
}
