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

package io.jmix.searchflowui.component;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

/**
 * The component filters by the ids the search returned. A store that cannot run JPQL rejects the JPQL condition, so
 * such an entity is filtered with a property condition on its primary key instead.
 */
public class FullTextFilterConditionTest {

    @Test
    void jpaEntityKeepsTheJpqlCondition() {
        TestFullTextFilter filter = new TestFullTextFilter(metadataTools(true));

        filter.setDataLoader(dataLoader());

        Assertions.assertInstanceOf(JpqlCondition.class, filter.getQueryCondition());
        Assertions.assertEquals("{E}.id in :testParameter",
                ((JpqlCondition) filter.getQueryCondition()).getWhere());
    }

    @Test
    void nonJpaEntityIsFilteredByAnInListConditionOnThePrimaryKey() {
        TestFullTextFilter filter = new TestFullTextFilter(metadataTools(false));

        filter.setDataLoader(dataLoader());

        Condition condition = filter.getQueryCondition();
        Assertions.assertInstanceOf(PropertyCondition.class, condition);
        PropertyCondition propertyCondition = (PropertyCondition) condition;
        Assertions.assertEquals("id", propertyCondition.getProperty());
        Assertions.assertEquals(PropertyCondition.Operation.IN_LIST, propertyCondition.getOperation());
    }

    @Test
    void nonJpaConditionIsSkippedWhileTheFilterIsEmpty() {
        TestFullTextFilter filter = new TestFullTextFilter(metadataTools(false));
        filter.setDataLoader(dataLoader());

        // A condition that is skipped leaves the loader unrestricted, which is what an unfilled filter must do.
        Assertions.assertNull(filter.getQueryCondition().actualize(Set.of(), false));
    }

    @Test
    void clearingTheFilterRestoresTheSkippedNonJpaCondition() {
        TestFullTextFilter filter = new TestFullTextFilter(metadataTools(false));
        filter.setDataLoader(dataLoader());
        PropertyCondition condition = (PropertyCondition) filter.getQueryCondition();

        // A search that found nothing leaves the condition always false: applied, with no ids to match.
        condition.setSkipNullOrEmpty(false);
        Assertions.assertSame(condition, condition.actualize(Set.of(), false));

        filter.updateQueryCondition(null);

        Assertions.assertNull(condition.actualize(Set.of(), false),
                "clearing the filter must stop the previous empty result from hiding every row");
    }

    private MetadataTools metadataTools(boolean jpaEntity) {
        MetadataTools metadataTools = Mockito.mock(MetadataTools.class);
        Mockito.when(metadataTools.isJpaEntity(Mockito.any(MetaClass.class))).thenReturn(jpaEntity);
        Mockito.when(metadataTools.getPrimaryKeyName(Mockito.any(MetaClass.class))).thenReturn("id");
        return metadataTools;
    }

    private CollectionLoader<?> dataLoader() {
        CollectionContainer<?> container = Mockito.mock(CollectionContainer.class);
        Mockito.when(container.getEntityMetaClass()).thenReturn(Mockito.mock(MetaClass.class));

        CollectionLoader<?> dataLoader = Mockito.mock(CollectionLoader.class);
        Mockito.when(dataLoader.getContainer()).thenAnswer(invocation -> container);
        Mockito.when(dataLoader.getCondition()).thenReturn(LogicalCondition.and());
        return dataLoader;
    }

    /**
     * Builds the component without a Spring context, which {@code initComponent()} would need.
     */
    private static class TestFullTextFilter extends FullTextFilter {

        TestFullTextFilter(MetadataTools metadataTools) {
            this.metadataTools = metadataTools;
            this.queryCondition = createQueryCondition();
            setParameterName("testParameter");
        }

        @Override
        public void updateQueryCondition(String newValue) {
            super.updateQueryCondition(newValue);
        }
    }
}
