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

package export_all.view;

import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FluentLoader;
import io.jmix.core.LoadContext;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shows products in two grids: one loaded from the database as usual, and one loaded through a delegate that,
 * like an external API, supports only a query with pagination and cannot load data by primary key.
 */
@Route("export-all-test-view")
@ViewController("ExportAllTestView")
@ViewDescriptor("export-all-test-view.xml")
public class ExportAllTestView extends StandardView {

    @Autowired
    protected DataManager dataManager;

    protected final List<String> databasePageRequests = new ArrayList<>();
    protected final List<String> externalPageRequests = new ArrayList<>();

    @Install(to = "productsDl", target = Target.DATA_LOADER)
    public List<Product> productsDlLoadDelegate(LoadContext<Product> loadContext) {
        LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());
        databasePageRequests.add(query.getFirstResult() + "/" + query.getMaxResults());

        return dataManager.loadList(loadContext);
    }

    @Install(to = "externalProductsDl", target = Target.DATA_LOADER)
    public List<Product> externalProductsDlLoadDelegate(LoadContext<Product> loadContext) {
        LoadContext.Query query = Objects.requireNonNull(loadContext.getQuery());
        externalPageRequests.add(query.getFirstResult() + "/" + query.getMaxResults());

        //noinspection ConstantValue
        if (query.getCondition() != null) {
            throw new UnsupportedOperationException("The external API cannot filter its data");
        }

        // The sort of the query is ignored: the external API knows only its own order.
        FluentLoader.ByQuery<Product> loader = dataManager.load(Product.class)
                .query("select e from test_Product e order by e.position")
                .firstResult(query.getFirstResult());
        if (query.getMaxResults() > 0) {
            loader.maxResults(query.getMaxResults());
        }
        return loader.list();
    }

    /**
     * Returns the pages requested from the database since the last {@link #clearPageRequests()}.
     *
     * @return pages in the {@code firstResult/maxResults} form
     */
    public List<String> getDatabasePageRequests() {
        return databasePageRequests;
    }

    /**
     * Returns the pages requested from the external data source since the last {@link #clearPageRequests()}.
     *
     * @return pages in the {@code firstResult/maxResults} form
     */
    public List<String> getExternalPageRequests() {
        return externalPageRequests;
    }

    public void clearPageRequests() {
        databasePageRequests.clear();
        externalPageRequests.clear();
    }
}
