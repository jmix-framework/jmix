/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.impl.repository.query;


import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.PartTree;

import jakarta.annotation.Nonnull;
import java.lang.reflect.Method;

public class JmixCountQuery extends JmixStructuredQuery {

    public JmixCountQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory, PartTree qryTree) {
        super(dataManager, jmixMetadata, method, metadata, factory, qryTree);
    }

    @Override
    @Nonnull
    public Object execute(Object[] parameters) {
        String entityName = jmixMetadata.getClass(metadata.getDomainType()).getName();

        String queryString = String.format("select %s e from %s e", distinct ? "distinct" : "", entityName);

        LoadContext<?> context = new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType()))
                .setQuery(new LoadContext.Query(queryString)
                        .setCondition(conditions)
                        .setParameters(buildNamedParametersMap(parameters)))
                .setHints(queryHints);

        return dataManager.getCount(context);
    }


}
