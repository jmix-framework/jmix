/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.gui.actions;

import com.haulmont.cuba.gui.components.Table;
import io.jmix.ui.screen.Screen;

public class TablePrintFormAction extends ListPrintFormAction {

    /**
     * @deprecated Use {@link TablePrintFormAction#TablePrintFormAction(Table)} instead.
     */
    @Deprecated
    public TablePrintFormAction(Screen screen, Table table) {
        this("tableReport", screen, table);
    }

    /**
     * @deprecated Use {@link TablePrintFormAction#TablePrintFormAction(String, Table)} instead.
     */
    @Deprecated
    public TablePrintFormAction(String id, Screen screen, Table table) {
        super(id, table);
    }

    public TablePrintFormAction(Table table) {
        this("tableReport", table);
    }

    public TablePrintFormAction(String id, Table table) {
        super(id, table);
    }
}