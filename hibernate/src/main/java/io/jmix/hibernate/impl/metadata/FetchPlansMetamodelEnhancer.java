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

package io.jmix.hibernate.impl.metadata;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("hibernate_FetchPlansMetadataEnhancer")
public class FetchPlansMetamodelEnhancer implements MetamodelEnhancer {

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected FetchGraphProvider fetchGraphProvider;

    @Autowired
    protected Metadata metadata;

    @Override
    public void enhance(SessionFactoryImplementor sessionFactoryImplementor) {
        MetamodelImplementor metamodel = sessionFactoryImplementor.getMetamodel();
        for (MetaClass metaClass : metadata.getClasses()) {
            for (String fetchPlanName : fetchPlanRepository.getFetchPlanNames(metaClass)) {
                FetchPlan fetchPlan = fetchPlanRepository.findFetchPlan(metaClass, fetchPlanName);
                RootGraphImplementor namedEntityGraph = fetchGraphProvider.createNamedEntityGraph(fetchPlan, sessionFactoryImplementor);
                if (namedEntityGraph != null) {
                    metamodel.addNamedEntityGraph(fetchPlanName, namedEntityGraph);
                }
            }
        }
    }
}
