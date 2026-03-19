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
import io.jmix.flowui.component.grid.sort.DataGridSort;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.KeyValueCollectionLoader;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.datastores.MainDsEntity;

import java.util.List;

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
    public KeyValueCollectionLoader keyValueEntitiesDl1;
    @ViewComponent
    public KeyValueCollectionLoader keyValueEntitiesDl2;

    protected int jpaTransientComparatorCallCount;
    protected int keyValueComparatorCallCount;

    public void setupJpaSortBuilderDelegate() {
        jpaDataGridPersistent.setSortBuilderDelegate(sortContext -> {
            List<DataGridSort.SortInfo> sortInfos = DataGridSort.SortInfo.of(sortContext.getSortInfos())
                    .stream()
                    .map(sortInfo -> {
                        if ("mem1DtoEntity".equals(sortInfo.getProperty())) {
                            sortInfo.withExpression("e.mem1DtoEntityId");
                        }
                        return sortInfo;
                    })
                    .toList();

            return DataGridSort.by(sortInfos, DataGridSort.InMemorySortInfo.of(sortContext.getSortInfos()));
        });
    }

    public void setupJpaTransientInMemorySortBuilderDelegate() {
        jpaTransientComparatorCallCount = 0;

        jpaDataGridInMemory.setSortBuilderDelegate(sortContext -> {
            List<DataGridSort.InMemorySortInfo> inMemorySortInfos = DataGridSort.InMemorySortInfo.of(sortContext.getSortInfos())
                    .stream()
                    .map(sortInfo -> {
                        if ("mem1DtoEntity".equals(sortInfo.getProperty())) {
                            sortInfo.withComparator((o1, o2) -> {
                                jpaTransientComparatorCallCount++;
                                return 0;
                            });
                        }
                        return sortInfo;
                    })
                    .toList();

            return DataGridSort.by(DataGridSort.SortInfo.of(sortContext.getSortInfos()), inMemorySortInfos);
        });
    }

    public int getJpaTransientComparatorCallCount() {
        return jpaTransientComparatorCallCount;
    }

    public void setupKeyValueInMemorySortBuilderDelegate() {
        keyValueComparatorCallCount = 0;

        keyValueDataGridInMemory.setSortBuilderDelegate(sortContext -> {
            List<DataGridSort.InMemorySortInfo> inMemorySortInfos = DataGridSort.InMemorySortInfo.of(sortContext.getSortInfos())
                    .stream()
                    .map(sortInfo -> {
                        if ("name".equals(sortInfo.getProperty())) {
                            sortInfo.withComparator((o1, o2) -> {
                                keyValueComparatorCallCount++;
                                return 0;
                            });
                        }
                        return sortInfo;
                    })
                    .toList();

            return DataGridSort.by(DataGridSort.SortInfo.of(sortContext.getSortInfos()), inMemorySortInfos);
        });
    }

    public int getKeyValueComparatorCallCount() {
        return keyValueComparatorCallCount;
    }

    public void setupKeyValuePersistentSortBuilderDelegate() {
        keyValueDataGridPersistent.setSortBuilderDelegate(sortContext -> {
            List<DataGridSort.SortInfo> sortInfos = DataGridSort.SortInfo.of(sortContext.getSortInfos())
                    .stream()
                    .map(sortInfo -> {
                        if ("name".equals(sortInfo.getProperty())) {
                            sortInfo.withExpression("e.mem1DtoEntityId");
                        }
                        return sortInfo;
                    })
                    .toList();

            return DataGridSort.by(sortInfos, DataGridSort.InMemorySortInfo.of(sortContext.getSortInfos()));
        });
    }
}
