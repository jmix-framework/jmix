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

package io.jmix.reportsui.action.list;

import io.jmix.ui.component.Table;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.meta.StudioAction;

@StudioAction(category = "Reports list actions", description = "Prints the reports associated with current editor screen")
@ActionType(TablePrintFormAction.ID)
public class TablePrintFormAction extends ListPrintFormAction {

    public static final String ID = "tablePrintForm";

    public TablePrintFormAction(Table table) {
        this(ID, table);
    }

    public TablePrintFormAction(String id, Table table) {
        super(id);

        this.target = table;
    }
}