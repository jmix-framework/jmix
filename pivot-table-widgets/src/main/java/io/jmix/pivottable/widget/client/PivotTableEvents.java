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

package io.jmix.pivottable.widget.client;

import io.jmix.pivottable.widget.client.events.JsCellClickEvent;
import io.jmix.pivottable.widget.client.events.JsRefreshEvent;

import java.util.function.Consumer;

public class PivotTableEvents {

    private Consumer<JsRefreshEvent> refreshHandler;
    private Consumer<JsCellClickEvent> cellClickHandler;

    public Consumer<JsRefreshEvent> getRefreshHandler() {
        return refreshHandler;
    }

    public void setRefreshHandler(Consumer<JsRefreshEvent> refreshHandler) {
        this.refreshHandler = refreshHandler;
    }

    public Consumer<JsCellClickEvent> getCellClickHandler() {
        return cellClickHandler;
    }

    public void setCellClickHandler(Consumer<JsCellClickEvent> cellClickHandler) {
        this.cellClickHandler = cellClickHandler;
    }
}
