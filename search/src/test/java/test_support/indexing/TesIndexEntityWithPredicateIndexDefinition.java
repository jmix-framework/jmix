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

package test_support.indexing;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.search.index.annotation.AutoMappedField;
import io.jmix.search.index.annotation.IndexablePredicate;
import io.jmix.search.index.annotation.JmixEntitySearchIndex;
import test_support.entity.TestEnum;
import test_support.entity.indexing.TestEntityForPredicate;

import java.util.function.Predicate;

@JmixEntitySearchIndex(entity = TestEntityForPredicate.class)
public interface TesIndexEntityWithPredicateIndexDefinition {

    @AutoMappedField(includeProperties = "name")
    void mapping();

    @IndexablePredicate
    default Predicate<TestEntityForPredicate> indexOpenOnlyPredicate(DataManager dataManager) {
        return (instance) -> {
            Id<TestEntityForPredicate> id = Id.of(instance);
            TestEntityForPredicate reloadedInstance = dataManager.load(id)
                    .fetchPlanProperties("enumValue")
                    .one();
            TestEnum enumValue = reloadedInstance.getEnumValue();
            return TestEnum.OPEN.equals(enumValue);
        };
    }

    @IndexablePredicate
    default Predicate<? extends TestEntityForPredicate> extendedWildcardPredicate() {
        return (instance) -> true;
    }

    @IndexablePredicate
    default Predicate<Object> objectPredicate() {
        return (instance) -> true;
    }

    @IndexablePredicate
    default Predicate<?> wildcardPredicate() {
        return (instance) -> true;
    }

    @IndexablePredicate
    default Predicate rawPredicate() {
        return (instance) -> true;
    }
}
