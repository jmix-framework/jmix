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

package entity_annotations

import io.jmix.core.DataManager
import io.jmix.core.FetchPlan
import io.jmix.core.Id
import io.jmix.core.InstanceNameProvider
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.instance_name.GrandChildOne
import test_support.entity.instance_name.GrandChildTwo

class InstanceNameFallbackTest extends DataSpec {

    private GrandChildOne grandChildOne;
    private GrandChildTwo grandChildTwo;

    @Autowired
    private DataManager dataManager

    @Autowired
    private InstanceNameProvider instanceNameProvider

    @Autowired
    SystemAuthenticator authenticator

    void setup() {
        grandChildOne = dataManager.create(GrandChildOne)
        grandChildOne.code = "code" //BaseEntity
        grandChildOne.name = "GrandChildOne" //Parent
        grandChildOne.childName = "childName" //ChildOne
        grandChildOne.grandChildName = "grandChildName" //GrandChildOne

        grandChildOne = dataManager.save(grandChildOne)

        grandChildTwo = dataManager.create(GrandChildTwo)
        grandChildTwo.code = "code" //BaseEntity
        grandChildTwo.name = "GrandChildTwo" //Parent
        grandChildTwo.childCode = "childCode" //ChildTwo
        grandChildTwo.grandChildCode = "grandChildCode" //GrandChildTwo
        grandChildTwo.number = 2 //GrandChildTwo

        grandChildTwo = dataManager.save(grandChildTwo)

        authenticator.begin()
    }


    void "test simple fallback for property-based instance name"() {
        when:
        def fullyLoaded = dataManager.load(GrandChildOne)
                .id(Id.of(grandChildOne))
                .fetchPlan(FetchPlan.LOCAL)
                .one()

        def partiallyLoaded = dataManager.load(GrandChildOne)
                .id(Id.of(grandChildOne))
                .fetchPlan(b -> b.addAll("code", "name", "childName"))
                .one()

        def fullyLoadedEntityInstanceName = instanceNameProvider.getInstanceName(fullyLoaded)
        def partiallyLoadedEntityInstanceName = instanceNameProvider.getInstanceName(partiallyLoaded)

        then:
        fullyLoadedEntityInstanceName == "grandChildName"
        partiallyLoadedEntityInstanceName == "childName"
    }


    void "test simple fallback for method-based instance name"() {
        when:
        def fullyLoaded = dataManager.load(GrandChildTwo)
                .id(Id.of(grandChildTwo))
                .fetchPlan(FetchPlan.LOCAL)
                .one()

        def loaded = dataManager.load(GrandChildTwo)
                .id(Id.of(grandChildTwo))
                .fetchPlan(b -> b.addAll("code", "name", "childCode"))
                .one()

        def fullyLoadedEntityInstanceName = instanceNameProvider.getInstanceName(fullyLoaded)
        def partiallyLoadedInstanceName = instanceNameProvider.getInstanceName(loaded)

        then:
        fullyLoadedEntityInstanceName == "[GrandChildTwo-grandChildCode-2]"
        partiallyLoadedInstanceName == "[ChildTwo-childCode]"
    }

    void "test double fallback"() {
        when:
        def loadedOne = dataManager.load(GrandChildOne)
                .id(Id.of(grandChildOne))
                .fetchPlan(b -> b.addAll("code", "name"))
                .one()


        def instanceNameOne = instanceNameProvider.getInstanceName(loadedOne)

        then:
        instanceNameOne == "code"
    }


    void "test unsuccessful fallback"() {
        when:
        def loadedTwo = dataManager.load(GrandChildTwo)
                .id(Id.of(grandChildTwo))
                .fetchPlan(b -> b.addAll("id"))
                .one()

        instanceNameProvider.getInstanceName(loadedTwo)

        then:
        def exception = thrown(RuntimeException)
        exception.getMessage().startsWith("Error getting instance name for test_support.entity.instance_name.GrandChildTwo-")
        exception.getCause().getMessage().startsWith("Cannot get unfetched attribute [code]")
    }

    void cleanup() {
        authenticator.end()
        jdbc.update("delete from TEST_INSTANCE_NAME_BASE_ENTITY")
    }
}
