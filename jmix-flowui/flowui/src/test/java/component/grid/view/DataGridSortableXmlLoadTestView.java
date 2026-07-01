/*
 * Copyright 2026 Haulmont.
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

package component.grid.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import test_support.entity.datastores.MainDsEntity;

@Route("DataGridSortableXmlLoadTestView")
@ViewController("DataGridSortableXmlLoadTestView")
@ViewDescriptor("data-grid-sortable-xml-load-test-view.xml")
public class DataGridSortableXmlLoadTestView extends StandardView {

    @ViewComponent
    public DataGrid<?> entityDataGrid;

    @ViewComponent
    public DataGrid<?> dataGridInMemory;

    @ViewComponent
    public DataGrid<?> dataGridPersistent;

    @ViewComponent
    public DataGrid<?> dtoDataGrid;

    @ViewComponent
    public CollectionLoader<MainDsEntity> mainDsEntitiesDl;
    @ViewComponent
    public CollectionLoader<MainDsEntity> mainDsEntitiesDl1;
    @ViewComponent
    public CollectionLoader<MainDsEntity> mainDsEntitiesDl2;

    public void setupComparatorForNoPropertyColumn() {
        dataGridInMemory.getColumnByKey("noPropertyColumn")
                .setComparator((o1, o2) -> 0);
    }

    public void setupComparatorForTransientPropertyColumn() {
        dataGridInMemory.getColumnByKey("mem1DtoEntity")
                .setComparator((o1, o2) -> 0);
    }

    public void setupComparatorForPersistentNoPropertyColumn() {
        dataGridPersistent.getColumnByKey("noPropertyColumn")
                .setComparator((o1, o2) -> 0);
    }

    public void setupComparatorForPersistentTransientPropertyColumn() {
        dataGridPersistent.getColumnByKey("mem1DtoEntity")
                .setComparator((o1, o2) -> 0);
    }
}
