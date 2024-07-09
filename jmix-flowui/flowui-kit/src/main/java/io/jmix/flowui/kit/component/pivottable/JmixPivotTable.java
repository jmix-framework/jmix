/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.component.pivottable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonValue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Tag("jmix-pivot-table")
@JsModule("./src/pivot-table/jmix-pivot-table.js")
public class JmixPivotTable extends Component {
    public JmixPivotTable() {
        Div div = new Div();
        div.setId("div-id");

        SlotUtils.addToSlot(this, "output", div);


    }

    public void sendData() {
        List<Map<String, String>> values = new LinkedList<>();

        Map<String, String> v1 = new HashMap<>();
        v1.put("color", "blue");
        v1.put("shape", "circle");
        values.add(v1);

        Map<String, String> v2 = new HashMap<>();
        v2.put("color", "red");
        v2.put("shape", "triangle");
        values.add(v2);

        Map<String, String> v3 = new HashMap<>();
        v3.put("color", "green");
        v3.put("shape", "square");
        values.add(v3);

        /*String values = "[ " +
                "{color: \"blue\", shape: \"circle\"}, " +
                "{color: \"red\", shape: \"triangle\"}, " +
                "{color: \"green\", shape: \"square\"}  ]";
        String rows = "[\"shape\"]";
        String cols = "[\"color\"]";*/

        List<String> rows = new LinkedList<>();
        rows.add("shape");

        List<String> cols = new LinkedList<>();
        cols.add("color");

        PivotData pivotData = new PivotData();
        //pivotData.setValues(values);
        pivotData.setRows(rows);
        pivotData.setCols(cols);

        JsonValue json = JsonSerializer.toJson(pivotData);

        getElement().executeJs("this.showData($0);", json.toJson());
    }

    public static class PivotData {
        List<Map<String, String>> values;
        List<String> rows;
        List<String> cols;

        public List<Map<String, String>> getValues() {
            return values;
        }

        public void setValues(List<Map<String, String>> values) {
            this.values = values;
        }

        public List<String> getRows() {
            return rows;
        }

        public void setRows(List<String> rows) {
            this.rows = rows;
        }

        public List<String> getCols() {
            return cols;
        }

        public void setCols(List<String> cols) {
            this.cols = cols;
        }
    }



}

