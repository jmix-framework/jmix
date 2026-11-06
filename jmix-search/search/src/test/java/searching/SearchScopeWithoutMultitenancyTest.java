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

package searching;

import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.IndexLayout;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AsyncEnqueueingTestConfiguration;

import java.util.List;

/**
 * The index layout of an application that has no multitenancy add-on - which is how the module is used most of
 * the time.
 * <p>
 * This is the chain a search walks to learn which index to ask: the scope provider takes the tenant of the
 * current user from the adapter and asks the layout for the index of that configuration and that tenant. The unit
 * tests of both ends stub the layout, so neither can tell whether the name is right. Here nothing is stubbed -
 * {@code NoopMultitenancyAdapter}, {@code StandardIndexLayout}, {@code StandardIndexNameGenerator} and the
 * application properties are the real ones, and the names are the names an engine would be asked for.
 * <p>
 * What it does not cover: the permission filter of the scope provider, which is a question about roles rather
 * than about tenants.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AsyncEnqueueingTestConfiguration.class})
public class SearchScopeWithoutMultitenancyTest {

    /** An ordinary entity: no tenant attribute at all. */
    private static final String PLAIN_ENTITY = "test_RootEntity";
    private static final String PLAIN_INDEX = "search_index_test_rootentity";

    /** An entity that does carry {@code @TenantId} - and is still not split, because there is no add-on. */
    private static final String TENANT_AWARE_ENTITY = "test_TenantEntity";
    private static final String TENANT_AWARE_INDEX = "search_index_test_tenantentity";

    @Autowired
    IndexLayout indexLayout;
    @Autowired
    IndexConfigurationManager indexConfigurationManager;
    @Autowired
    MultitenancyAdapter multitenancyAdapter;

    @Test
    @DisplayName("Without the add-on there is no tenant to search by")
    public void noTenantToSearchBy() {
        Assertions.assertFalse(multitenancyAdapter.isMultitenancyActive());
        Assertions.assertNull(multitenancyAdapter.getCurrentUserTenantId(),
                "this is what the scope provider passes to the layout");
        Assertions.assertFalse(indexLayout.isSplitByTenantsEnabled());
    }

    @Test
    @DisplayName("An ordinary entity is searched in the index it has always had")
    public void plainEntityKeepsItsIndex() {
        IndexConfiguration configuration = configurationOf(PLAIN_ENTITY);

        Assertions.assertFalse(indexLayout.isSplitByTenants(configuration));
        Assertions.assertEquals(PLAIN_INDEX, indexLayout.indexName(configuration, null));
        Assertions.assertEquals(List.of(PLAIN_INDEX), indexNames(configuration));
    }

    @Test
    @DisplayName("A tenant attribute alone does not split anything: the add-on has to be there too")
    public void tenantAttributeAloneSplitsNothing() {
        IndexConfiguration configuration = configurationOf(TENANT_AWARE_ENTITY);

        Assertions.assertTrue(configuration.isTenantAware(), "the entity does carry the attribute");
        Assertions.assertFalse(indexLayout.isSplitByTenants(configuration),
                "but without the add-on its data is in one index, as it was before this feature existed");
        Assertions.assertEquals(TENANT_AWARE_INDEX, indexLayout.indexName(configuration, null));
        Assertions.assertEquals(List.of(TENANT_AWARE_INDEX), indexNames(configuration));
    }

    @Test
    @DisplayName("A tenant named by mistake does not send the search anywhere else")
    public void anamedTenantChangesNothing() {
        Assertions.assertEquals(PLAIN_INDEX, indexLayout.indexName(configurationOf(PLAIN_ENTITY), "acme"));
        Assertions.assertEquals(TENANT_AWARE_INDEX,
                indexLayout.indexName(configurationOf(TENANT_AWARE_ENTITY), "acme"));
    }

    protected IndexConfiguration configurationOf(String entityName) {
        return indexConfigurationManager.getIndexConfigurationByEntityName(entityName);
    }

    protected List<String> indexNames(IndexConfiguration configuration) {
        return indexLayout.allIndexes(configuration).stream().map(IndexLayout.TenantIndex::indexName).toList();
    }
}
