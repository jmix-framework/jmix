/*
 * Copyright 2025 Haulmont.
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

package lazy_loading

import io.jmix.core.DataManager
import io.jmix.core.accesscontext.AccessContext
import io.jmix.core.constraint.AccessConstraint
import io.jmix.eclipselink.impl.lazyloading.SingleValueMappedByPropertyHolder
import io.jmix.eclipselink.impl.lazyloading.ValueHoldersSupport
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import spock.lang.IgnoreIf
import test_support.DataSpec
import test_support.entity.lazyloading.instantiated_vh_wrapping.Second
import test_support.entity.lazyloading.instantiated_vh_wrapping.Third
import test_support.entity.lazyloading.instantiated_vh_wrapping.First


@TestPropertySource(properties = [
        "eclipselink.cache.shared.ivw_First=true",
        "eclipselink.cache.shared.ivw_Second=true",
        "eclipselink.cache.shared.ivw_Third=true"])
@IgnoreIf({ Boolean.valueOf(System.getenv("JMIX_ECLIPSELINK_DISABLELAZYLOADING")) })
class InstantiatedVHWrappingLLTest extends DataSpec {
    @Autowired
    DataManager dataManager


    def "Instantiated ValueHolder wrapping test"() {
        setup:
        def newFirst = dataManager.create(First)
        newFirst.name = RandomStringUtils.insecure().nextAlphabetic(10)

        def newSecond = dataManager.create(Second)
        newSecond.first = newFirst
        newFirst.second = newSecond

        def newThird = dataManager.create(Third)
        newThird.second = newSecond
        newThird.secondForCollection = newSecond
        newSecond.third = newThird
        newSecond.thirds = List.of(newThird)

        dataManager.save(newFirst, newSecond, newThird)

        when: "Lazy-loading nested entities with back references and cache enabled"
        def first = dataManager.load(First).id(newFirst.id).one()

        SingleValueMappedByPropertyHolder testEntityVH = ValueHoldersSupport.getSingleValueHolder(first, "second")
        //forces value holder to reload value which makes eclipselink return value holder already instantiated by cached entity
        testEntityVH.getLoadOptions().getAccessConstraints().add(new TestDummyAccessConstraint())

        def loadedSecond = first.second
        def loadedThird = loadedSecond.third
        def loadedSecondBackReference = loadedThird.second
        def loadedSecondBackReferenceForCollection = loadedThird.secondForCollection

        then: "Already instantiated (from cache) value holders transformed correctly to entity value"
        noExceptionThrown()
        loadedSecondBackReference != null
        loadedSecondBackReferenceForCollection != null
    }


    static class TestDummyAccessContext implements AccessContext {
    }

    static class TestDummyAccessConstraint implements AccessConstraint<TestDummyAccessContext> {
        @Override
        Class<TestDummyAccessContext> getContextType() {
            return TestDummyAccessContext.class
        }

        @Override
        void applyTo(TestDummyAccessContext context) {
        }
    }
}
