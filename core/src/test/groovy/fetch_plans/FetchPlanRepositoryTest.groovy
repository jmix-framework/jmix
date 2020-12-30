/*
 * Copyright 2019 Haulmont.
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

package fetch_plans

import io.jmix.core.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.addon1.entity.TestAddon1Entity
import test_support.app.TestAppConfiguration
import test_support.app.entity.Pet
import test_support.app.entity.fetch_plans.ChildTestEntity
import test_support.app.entity.fetch_plans.ParentTestEntity
import test_support.app.entity.fetch_plans.spaceport.Waybill
import test_support.app.entity.fetch_plans.spaceport.WaybillItem

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class FetchPlanRepositoryTest extends Specification {

    @Autowired
    FetchPlanRepository repository

    @Autowired
    MetadataTools metadataTools

    @Autowired
    Metadata metadata

    def "fetchPlan is deployed from add-on's fetch-plans.xml file"() {

        when:

        def fetchPlan = repository.getFetchPlan(TestAddon1Entity, 'test-fp-1')

        then:

        fetchPlan.containsProperty('name')
    }

    def "fetchplan overwrite mechanism works correctly"() {

        setup:

        def childPlan = repository.getFetchPlan(ChildTestEntity, "childTestEntity.overwrite")
        def parentPlan = repository.getFetchPlan(ParentTestEntity, "parentTestEntity.common")


        expect:

        childPlan.containsProperty("birthDate")
        !childPlan.containsProperty("name")

        parentPlan.getProperty("firstborn").fetchPlan.containsProperty("birthDate")
        !parentPlan.getProperty("firstborn").fetchPlan.containsProperty("name")

        parentPlan.getProperty("youngerChildren").fetchPlan.containsProperty("birthDate")
        !parentPlan.getProperty("youngerChildren").fetchPlan.containsProperty("name")
    }

    def "fetch plan correctly complemented by additional properties"() {

        setup:

        def plane = repository.getFetchPlan(Waybill, "fetchPlan.extend")


        expect:

        plane.getProperty("items").getFetchPlan().getProperties().size() == 7;
        plane.getProperty("items").getFetchPlan().containsProperty("dim");

    }

    def "local fetch plan contains system properties"() {

        given:

        def localFetchPlan = repository.getFetchPlan(Pet.class, FetchPlan.LOCAL)

        expect:

        localFetchPlan.containsProperty("id")
        localFetchPlan.containsProperty("version");
        localFetchPlan.containsProperty("createTs");
        localFetchPlan.containsProperty("createdBy");
        localFetchPlan.containsProperty("updateTs");
        localFetchPlan.containsProperty("updatedBy");
        localFetchPlan.containsProperty("deleteTs");
        localFetchPlan.containsProperty("deletedBy");

    }

    def "base fetch plan contains embedded properties"() {
        given:

        def baseFetchPlan = repository.getFetchPlan(WaybillItem.class, FetchPlan.BASE)

        expect:

        baseFetchPlan.containsProperty("id")
        baseFetchPlan.containsProperty("number")
        baseFetchPlan.containsProperty("weight")
        baseFetchPlan.containsProperty("dim")

        def embeddedFetchPlan = baseFetchPlan.getProperty("dim").getFetchPlan()
        embeddedFetchPlan.containsProperty("length")
        embeddedFetchPlan.containsProperty("width")
        embeddedFetchPlan.containsProperty("height")
    }

    def "base fetch plan contains references properties with base fetch plan"() {
        given:

        def baseFetchPlan = repository.getFetchPlan(WaybillItem.class, FetchPlan.BASE)

        expect:

        baseFetchPlan.containsProperty("id")
        baseFetchPlan.containsProperty("number")
        baseFetchPlan.containsProperty("weight")
        baseFetchPlan.containsProperty("waybillCategory")

        def referenceFetchPlan = baseFetchPlan.getProperty("waybillCategory").getFetchPlan()
        referenceFetchPlan.containsProperty("name")
        referenceFetchPlan.containsProperty("code")
    }
}

