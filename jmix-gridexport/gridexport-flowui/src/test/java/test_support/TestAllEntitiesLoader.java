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

package test_support;

import io.jmix.core.DataManager;
import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.exporter.EntityExportContext;
import io.jmix.gridexportflowui.exporter.entitiesloader.AllEntitiesLoader;
import org.jspecify.annotations.Nullable;
import test_support.entity.Product;

import java.util.List;

/**
 * Custom pagination strategy registered by the application, standing in for a loader written for a data source
 * the built-in strategies cannot handle. Exports the products in the reverse position order, so that its output
 * is distinguishable from the built-in strategies, and remembers the batch size it was asked for.
 */
public class TestAllEntitiesLoader implements AllEntitiesLoader {

    public static final String PAGINATION_STRATEGY = "test";

    protected final DataManager dataManager;

    @Nullable
    protected Integer lastLoadBatchSize;

    public TestAllEntitiesLoader(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public String getPaginationStrategy() {
        return PAGINATION_STRATEGY;
    }

    @SuppressWarnings("removal")
    @Override
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor) {
        // The batch size is not used by this loader, so any positive value will do.
        loadAll(dataUnit, exportedEntityVisitor, 1);
    }

    @Override
    public void loadAll(DataUnit dataUnit, ExportedEntityVisitor exportedEntityVisitor, int loadBatchSize) {
        lastLoadBatchSize = loadBatchSize;

        List<Product> products = dataManager.load(Product.class)
                .query("select e from test_Product e order by e.position desc")
                .list();
        int entityNumber = 0;
        for (Product product : products) {
            if (!exportedEntityVisitor.visitEntity(new EntityExportContext(product, ++entityNumber))) {
                break;
            }
        }
    }

    /**
     * Returns the batch size passed to the last {@link #loadAll(DataUnit, ExportedEntityVisitor, int)} call.
     *
     * @return batch size, or {@code null} if the loader has not been called yet
     */
    @Nullable
    public Integer getLastLoadBatchSize() {
        return lastLoadBatchSize;
    }
}
