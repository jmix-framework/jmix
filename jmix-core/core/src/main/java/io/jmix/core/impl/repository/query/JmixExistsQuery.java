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
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.QueryStringProcessor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.util.List;

public class JmixExistsQuery extends JmixCountQuery {
    public JmixExistsQuery(DataManager dataManager,
                           Metadata jmixMetadata,
                           FetchPlanRepository fetchPlanRepository,
                           List<QueryStringProcessor> queryStringProcessors,
                           Method method,
                           RepositoryMetadata metadata,
                           ProjectionFactory factory,
                           PartTree qryTree) {
        super(dataManager, jmixMetadata, fetchPlanRepository, queryStringProcessors, method, metadata, factory, qryTree);
    }

    @Override
    public Object execute(Object[] parameters) {
        return (long) super.execute(parameters) > 0;
    }
}
