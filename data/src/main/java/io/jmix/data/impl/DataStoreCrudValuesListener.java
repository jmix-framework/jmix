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

package io.jmix.data.impl;

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.datastore.DataStoreBeforeValueLoadEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.data.accesscontext.LoadValuesAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("data_DataStoreCrudValuesListener")
public class DataStoreCrudValuesListener implements DataStoreEventListener {
    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;

    @Override
    public void beforeValueLoad(DataStoreBeforeValueLoadEvent event) {
        ValueLoadContext context = event.getLoadContext();
        LoadValuesAccessContext queryContext =
                new LoadValuesAccessContext(context.getQuery().getQueryString(), queryTransformerFactory, metadata);
        accessManager.applyConstraints(queryContext, context.getAccessConstraints());
        for (Integer index : queryContext.getDeniedSelectedIndexes()) {
            event.addDeniedProperty(index);
        }
    }
}
