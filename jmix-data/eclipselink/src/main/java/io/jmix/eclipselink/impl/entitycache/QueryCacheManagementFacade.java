/*
 * Copyright 2020 Haulmont.
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

package io.jmix.eclipselink.impl.entitycache;

import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages query cache", objectName = "jmix.eclipselink:type=QueryCache")
@Component("eclipselink_QueryCacheManagementFacade")
public class QueryCacheManagementFacade {

    @Autowired
    private QueryCache queryCache;

    @Autowired
    private QueryCacheManager queryCacheMgr;

    @Autowired
    private Metadata metadata;

    @ManagedAttribute(description = "Current number of cached queries")
    public long getSize() {
        return queryCache.size();
    }

    @ManagedOperation(description = "Discard all cached queries")
    public String evictAll() {
        queryCacheMgr.invalidateAll();
        return "Done";
    }

    @ManagedOperation(description = "Discard cached queries for entity")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "entityName", description = "Entity name, e.g. demo_User")
    })
    public String evict(String entityName) {
        Preconditions.checkNotEmptyString(entityName, "Entity name is not specified");
        Preconditions.checkNotNullArgument(metadata.findClass(entityName), "Entity " + entityName + " doesn't exist");

        queryCacheMgr.invalidate(entityName);
        return "Done";
    }
}
