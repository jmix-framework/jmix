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

package io.jmix.search.listener;

import io.jmix.multitenancy.event.TenantEvent;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import static io.jmix.multitenancy.event.TenantEvent.Type.CREATED;
import static io.jmix.multitenancy.event.TenantEvent.Type.DELETED;

/**
 * Brings the indexes of a tenant in line with the index configurations when the tenant appears.
 * <p>
 * The appearance of a tenant is treated as the appearance of new indexes, so it is handled by the same
 * {@link io.jmix.search.index.IndexSchemaManagementStrategy} as the initial synchronization on startup.
 * <p>
 * Indexes of a removed tenant are left in place. Removing a tenant is a soft delete and can be undone, dropping
 * an index cannot, and a restore is not announced at all - it clears the deletion date and so arrives as an
 * update, which this listener does not handle. Nothing else in the framework removes anything of a removed
 * tenant either: its records, its users, its queue items and its enqueueing sessions all stay.
 * <p>
 * This class references the Multitenancy add-on classes, so it must not be loaded when the add-on is absent: the
 * starter registers it conditionally.
 */
public class SearchTenantEventListener {

    private static final Logger log = LoggerFactory.getLogger(SearchTenantEventListener.class);

    protected final SearchProperties searchProperties;
    protected final IndexManager indexManager;
    protected final IndexConfigurationManager indexConfigurationManager;

    public SearchTenantEventListener(SearchProperties searchProperties,
                                     IndexManager indexManager,
                                     IndexConfigurationManager indexConfigurationManager) {
        this.searchProperties = searchProperties;
        this.indexManager = indexManager;
        this.indexConfigurationManager = indexConfigurationManager;
    }

    @EventListener
    public void onTenantEventOccurred(final TenantEvent event) {
        if (event.getType() == CREATED) {
            log.info("Synchronize search indexes of the created tenant '{}' according to strategy '{}'",
                    event.getTenantId(), searchProperties.getIndexSchemaManagementStrategy());
            indexManager.synchronizeIndexSchemas(
                    indexConfigurationManager.getAllIndexConfigurations(), event.getTenantId());
        }
        if (event.getType() == DELETED) {
            log.info("Search indexes of the deleted tenant '{}' are kept: deleting them is an explicit administrative"
                    + " operation", event.getTenantId());
        }
    }
}
