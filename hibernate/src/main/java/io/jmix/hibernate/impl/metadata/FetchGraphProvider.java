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
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.graph.internal.RootGraphImpl;
import org.hibernate.graph.spi.GraphImplementor;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.graph.spi.SubGraphImplementor;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("hibernate_FetchGraphProvider")
public class FetchGraphProvider {

    @Autowired
    protected Metadata metadata;

    public RootGraphImplementor createNamedEntityGraph(FetchPlan fetchPlan, SessionFactoryImplementor sessionFactoryImplementor) {
        EntityTypeDescriptor entityTypeDescriptor = findEntityDescriptor(fetchPlan, sessionFactoryImplementor);
        if (entityTypeDescriptor != null) {
            RootGraphImplementor rootGraph = new RootGraphImpl(fetchPlan.getName(), entityTypeDescriptor, sessionFactoryImplementor);
            fillGraph(rootGraph, fetchPlan);
            return rootGraph;
        } else {
            return null;
        }
    }

    public void fillGraph(GraphImplementor rootGraph, FetchPlan fetchPlan) {
        MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
            if (rootGraph.getGraphedType().findAttribute(fetchPlanProperty.getName()) != null) {
                rootGraph.addAttributeNode(fetchPlanProperty.getName());
                if (fetchPlanProperty.getFetchPlan() != null
                        && !isManyToMany(metaClass.getProperty(fetchPlanProperty.getName()))) {
                    SubGraphImplementor subGraph = rootGraph.addSubGraph(fetchPlanProperty.getName());
                    fillGraph(subGraph, fetchPlanProperty.getFetchPlan());
                }
            }
        }
    }

    private boolean isManyToMany(MetaProperty metaProperty) {
        return Range.Cardinality.MANY_TO_MANY == metaProperty.getRange().getCardinality();
    }

    private EntityTypeDescriptor findEntityDescriptor(FetchPlan fetchPlan, SessionFactoryImplementor sessionFactoryImplementor) {
        return sessionFactoryImplementor.getMetamodel().getEntityTypeByName(fetchPlan.getEntityClass().getName());
    }
}
