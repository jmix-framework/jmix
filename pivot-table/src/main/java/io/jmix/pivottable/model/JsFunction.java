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

package io.jmix.pivottable.model;

import java.io.Serializable;

/**
 * JavaScript function definition. <br>
 * Code example:
 * <pre>
 *  &#64;Autowired
 *  private SerialChart serialChart;
 *  ...
 *  Graph graph = serialChart.getGraphs().get(0);
 *  graph.setBalloonFunction(new JsFunction(
 *      "function(event){"
 *          + "var result = 'Income in ' + event.category + ': '"
 *          + "+ event.serialDataItem.dataContext.expenses;"
 *          + "return result;}"));
 * </pre>
 */
public class JsFunction implements Serializable {

    private static final long serialVersionUID = 7614774685832973416L;

    private String code;

    public JsFunction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}