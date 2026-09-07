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

package data_components

import io.jmix.core.DataManager
import io.jmix.core.common.event.Subscription
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionLoader
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.Foo
import test_support.spec.DataContextSpec

class DataLoaderConditionContributorTest extends DataContextSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    DataComponents factory

    private CollectionLoader<Foo> createLoader() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)
        loader.setContainer(container)
        loader.setQuery('select e from test_Foo e')
        return loader
    }

    def "load context condition is the same instance when no contributors are registered"() {
        CollectionLoader<Foo> loader = createLoader()
        Condition base = PropertyCondition.equal("name", "foo")
        loader.setCondition(base)

        expect: "the pre-contributors behavior is untouched: the very same condition object is used"
        loader.createLoadContext().getQuery().getCondition().is(base)
        loader.getEffectiveCondition().is(base)
    }

    def "contribution is combined with the loader condition on load"() {
        CollectionLoader<Foo> loader = createLoader()
        loader.setCondition(PropertyCondition.equal("name", "base"))
        loader.addConditionContributor { PropertyCondition.contains("name", "extra") }

        when:
        Condition effective = loader.createLoadContext().getQuery().getCondition()

        then: "the query condition is AND[base, contribution]"
        effective instanceof LogicalCondition
        (effective as LogicalCondition).getType() == LogicalCondition.Type.AND
        describe(effective) == "AND[name equal base, name contains extra]"

        and: "the loader condition slot is untouched"
        describe(loader.getCondition()) == "name equal base"
    }

    def "contribution works without a loader condition"() {
        CollectionLoader<Foo> loader = createLoader()
        loader.addConditionContributor { PropertyCondition.contains("name", "extra") }

        expect:
        describe(loader.createLoadContext().getQuery().getCondition()) == "AND[name contains extra]"
    }

    def "a null contribution does not participate"() {
        CollectionLoader<Foo> loader = createLoader()
        Condition base = PropertyCondition.equal("name", "base")
        loader.setCondition(base)
        loader.addConditionContributor { null }

        expect: "with nothing contributed the loader condition is used as is - the same instance"
        loader.createLoadContext().getQuery().getCondition().is(base)
    }

    def "contributor is polled on every load"() {
        CollectionLoader<Foo> loader = createLoader()
        PropertyCondition contribution = PropertyCondition.contains("name", "first")
        loader.addConditionContributor { contribution }

        when:
        Condition first = loader.createLoadContext().getQuery().getCondition()
        contribution.setParameterValue("second")
        Condition second = loader.createLoadContext().getQuery().getCondition()

        then:
        describe(first) == "AND[name contains first]"
        describe(second) == "AND[name contains second]"
    }

    def "composed condition holds copies, not the live nodes"() {
        CollectionLoader<Foo> loader = createLoader()
        PropertyCondition base = PropertyCondition.equal("name", "base")
        PropertyCondition contribution = PropertyCondition.contains("name", "extra")
        loader.setCondition(base)
        loader.addConditionContributor { contribution }

        when: "the owners edit their conditions after a load context is built"
        Condition effective = loader.createLoadContext().getQuery().getCondition()
        base.setParameterValue("changed")
        contribution.setParameterValue("changed")

        then: "the built context is not affected"
        describe(effective) == "AND[name equal base, name contains extra]"
    }

    def "subscription unregisters the contributor"() {
        CollectionLoader<Foo> loader = createLoader()
        Condition base = PropertyCondition.equal("name", "base")
        loader.setCondition(base)
        Subscription subscription = loader.addConditionContributor { PropertyCondition.contains("name", "extra") }

        when:
        subscription.remove()

        then:
        loader.createLoadContext().getQuery().getCondition().is(base)
    }

    def "contributions of all registered contributors are combined in registration order"() {
        CollectionLoader<Foo> loader = createLoader()
        loader.addConditionContributor { PropertyCondition.contains("name", "one") }
        loader.addConditionContributor { PropertyCondition.contains("name", "two") }

        expect:
        describe(loader.createLoadContext().getQuery().getCondition()) ==
                "AND[name contains one, name contains two]"
    }

    def "loaded data respects the contribution"() {
        CollectionLoader<Foo> loader = createLoader()
        Foo one = new Foo(name: "one")
        Foo two = new Foo(name: "two")
        dataManager.save(one, two)
        loader.addConditionContributor { PropertyCondition.equal("name", "two") }

        when:
        loader.load()

        then:
        loader.getContainer().getItems() == [two]

        cleanup:
        deleteRecord(one, two)
    }

    private static String describe(Condition condition) {
        if (condition == null) {
            return "(none)"
        }
        if (condition instanceof LogicalCondition) {
            return condition.getType().toString() +
                    "[" + condition.getConditions().collect { describe(it) }.join(", ") + "]"
        }
        if (condition instanceof PropertyCondition) {
            return "${condition.getProperty()} ${condition.getOperation()} ${condition.getParameterValue()}"
        }
        return condition.toString()
    }
}
