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
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.sort.DataGridSortBuilder;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.datastores.MainDsEntity;
import test_support.entity.datastores.Mem1DtoEntity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Route("DataGridSortBuilderDelegateTestView")
@ViewController("DataGridSortBuilderDelegateTestView")
@ViewDescriptor("data-grid-sort-builder-delegate-test-view.xml")
public class DataGridSortBuilderDelegateTestView extends StandardView {

    @ViewComponent
    public DataGrid<MainDsEntity> jpaDataGridInMemory;

    @ViewComponent
    public DataGrid<MainDsEntity> jpaDataGridPersistent;

    @ViewComponent
    public DataGrid<KeyValueEntity> keyValueDataGridInMemory;

    @ViewComponent
    public DataGrid<KeyValueEntity> keyValueDataGridPersistent;

    @ViewComponent
    public CollectionLoader<MainDsEntity> mainDsEntitiesDl1;
    @ViewComponent
    public CollectionLoader<MainDsEntity> mainDsEntitiesDl2;
    @ViewComponent
    public CollectionContainer<MainDsEntity> mainDsEntitiesDc1;
    @ViewComponent
    public CollectionContainer<MainDsEntity> mainDsEntitiesDc2;

    @ViewComponent
    public KeyValueCollectionLoader keyValueEntitiesDl1;
    @ViewComponent
    public KeyValueCollectionLoader keyValueEntitiesDl2;

    protected int jpaTransientComparatorCallCount;
    protected int keyValueComparatorCallCount;

    public void setupJpaSortBuilderDelegate() {
        jpaDataGridPersistent.setSortBuilderDelegate(sortContext ->
                DataGridSortBuilder.create(sortContext)
                        .replaceSort("mem1DtoEntity", "e.mem1DtoEntityId")
                        .build());
    }

    public void setupJpaTransientInMemorySortBuilderDelegate() {
        jpaTransientComparatorCallCount = 0;

        jpaDataGridInMemory.setSortBuilderDelegate(sortContext ->
                DataGridSortBuilder.create(sortContext)
                        .replaceSort("mem1DtoEntity", (o1, o2) -> {
                            jpaTransientComparatorCallCount++;
                            return 0;
                        }).build());
    }

    public int getJpaTransientComparatorCallCount() {
        return jpaTransientComparatorCallCount;
    }

    public void setupColumnComparator(DataGrid<MainDsEntity> dataGrid, String columnKey) {
        dataGrid.getColumnByKey(columnKey).setComparator(createNameDescComparator());
    }

    public void setupJpaSortBuilderDelegate(DataGrid<MainDsEntity> dataGrid,
                                            String columnKey,
                                            boolean replaceComparator,
                                            boolean replaceExpression) {
        dataGrid.setSortBuilderDelegate(sortContext -> {
            DataGridSortBuilder<MainDsEntity> sortBuilder = DataGridSortBuilder.create(sortContext);
            if (replaceComparator) {
                sortBuilder.replaceSort(columnKey, createNameDescComparator());
            }
            if (replaceExpression) {
                sortBuilder.replaceSort(columnKey, "e.name");
            }
            return sortBuilder.build();
        });
    }

    public void setupTransientValues() {
        setupTransientValues(mainDsEntitiesDc1);
        setupTransientValues(mainDsEntitiesDc2);
    }

    public void setupKeyValueInMemorySortBuilderDelegate() {
        keyValueComparatorCallCount = 0;

        keyValueDataGridInMemory.setSortBuilderDelegate(sortContext ->
                DataGridSortBuilder.create(sortContext)
                        .replaceSort("name", (o1, o2) -> {
                            keyValueComparatorCallCount++;
                            return 0;
                        }).build());
    }

    public int getKeyValueComparatorCallCount() {
        return keyValueComparatorCallCount;
    }

    public void setupKeyValuePersistentSortBuilderDelegate() {
        keyValueDataGridPersistent.setSortBuilderDelegate(sortContext ->
                DataGridSortBuilder.create(sortContext)
                        .replaceSort("name", "e.mem1DtoEntityId")
                        .build());
    }

    private void setupTransientValues(CollectionContainer<MainDsEntity> container) {
        for (MainDsEntity entity : container.getMutableItems()) {
            entity.setMem1DtoEntity(createTransientEntity(entity.getName()));
        }
    }

    private Mem1DtoEntity createTransientEntity(String name) {
        Mem1DtoEntity entity = new Mem1DtoEntity();
        entity.setName(name);
        entity.setId(switch (name) {
            case "a" -> UUID.fromString("00000000-0000-0000-0000-000000000001");
            case "b" -> UUID.fromString("00000000-0000-0000-0000-000000000002");
            default -> UUID.fromString("00000000-0000-0000-0000-000000000003");
        });
        return entity;
    }

    private Comparator<MainDsEntity> createNameDescComparator() {
        return Comparator.comparing(MainDsEntity::getName, Comparator.nullsFirst(String::compareTo))
                .reversed()
                .thenComparing(MainDsEntity::getId, Comparator.nullsFirst(UUID::compareTo));
    }
}
