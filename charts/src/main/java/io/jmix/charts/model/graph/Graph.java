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

package io.jmix.charts.model.graph;

import io.jmix.ui.meta.StudioElement;

/**
 * See documentation for properties of AmGraph JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmGraph">http://docs.amcharts.com/3/javascriptcharts/AmGraph</a>
 */
@StudioElement(
        caption = "Graph",
        xmlElement = "graph",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Graph extends AbstractGraph<Graph> {

    private static final long serialVersionUID = -3464633427729211728L;
}