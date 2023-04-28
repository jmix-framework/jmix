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

package entity_extension

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import test_support.DataSpec

class OrmXmlTest extends DataSpec {

    private GPathResult getEntityMappings() {
        Resource resource = new ClassPathResource("test_support/orm.xml");
        File ormFile = resource.getFile();
        return new XmlSlurper().parse(ormFile)
    }

    def "check classes and mappings"() {

        when:
        def entityMappings = getEntityMappings()

        then:
        //classes count
        entityMappings.'*'.size() == 10
        entityMappings.'*'.findAll { it.name() == 'mapped-superclass' }.size() == 1
        entityMappings.'*'.findAll { it.name() == 'entity' }.size() == 8
        entityMappings.'*'.findAll { it.name() == 'embeddable' }.size() == 1

        //classes
        entityMappings.'*'.find { it.name() == 'mapped-superclass' }.'@class' == "test_support.entity.entity_extension.Vehicle"
        entityMappings.'*'.find { it.name() == 'embeddable' }.'@class' == "test_support.entity.entity_extension.Address"
        entityMappings.'*'.find { it.@name == 'exttest_Plant' }.'@class' == "test_support.entity.entity_extension.Plant"
        entityMappings.'*'.find { it.@name == 'exttest_Driver' }.'@class' == "test_support.entity.entity_extension.Driver"
        entityMappings.'*'.find { it.@name == 'exttest_DriverAllocation' }.'@class' == "test_support.entity.entity_extension.DriverAllocation"
        entityMappings.'*'.find { it.@name == 'exttest_DriverCallsign' }.'@class' == "test_support.entity.entity_extension.DriverCallsign"
        entityMappings.'*'.find { it.@name == 'exttest_Waybill' }.'@class' == "test_support.entity.entity_extension.Waybill"
        entityMappings.'*'.find { it.@name == 'exttest_Bus' }.'@class' == "test_support.entity.entity_extension.Bus"
        entityMappings.'*'.find { it.@name == 'exttest_Doc' }.'@class' == "test_support.entity.entity_extension.Doc"
        entityMappings.'*'.find { it.@name == 'exttest_Station' }.'@class' == "test_support.entity.entity_extension.Station"


        //fetch
        entityMappings.'*'.find { it.'@name' == 'exttest_Plant' }.'attributes'.'*'.find { it.'@name' == 'doc' }.'@fetch' == 'LAZY'//overriden (EAGER->LAZY)
        entityMappings.'*'.find { it.'@name' == 'exttest_Waybill' }.'attributes'.'*'.find { it.'@name' == 'places' }.'@fetch' == 'EAGER'//overriden (LAZY->EAGER)

        entityMappings.'*'.find { it.'@name' == 'exttest_Plant' }.'attributes'.'*'.find { it.'@name' == 'models' }.'@fetch' == 'LAZY'//default (M2M)
        entityMappings.'*'.find { it.'@name' == 'exttest_DriverAllocation' }.'attributes'.'*'.find { it.'@name' == 'driver' }.'@fetch' == 'EAGER'//default (M2O)
        entityMappings.'*'.find { it.'@name' == 'exttest_Bus' }.'attributes'.'*'.find { it.'@name' == 'waybills' }.'@fetch' == 'LAZY'//default (O2M)
        entityMappings.'*'.find { it.'@name' == 'exttest_Waybill' }.'attributes'.'*'.find { it.'@name' == 'doc' }.'@fetch' == 'EAGER'//default (O2O)


        //mapped-by
        entityMappings.'*'.find { it.'@name' == 'exttest_Bus' }.'attributes'.'*'.find { it.'@name' == 'waybills' }.'@mapped-by' == 'bus'//one-to-many
        entityMappings.'*'.find { it.'@name' == 'exttest_Doc' }.'attributes'.'*'.find { it.'@name' == 'waybill' }.'@mapped-by' == 'doc'//one-to-one
        entityMappings.'*'.find { it.'@name' == 'exttest_DriverCallsign' }.'attributes'.'*'.find { it.'@name' == 'driver' }.'@mapped-by' == 'callsign'
        entityMappings.'*'.find { it.'@name' == 'exttest_Station' }.'attributes'.'*'.find { it.'@name' == 'waybills' }.'@mapped-by' == 'stations'//many-to-many

        entityMappings.'*'.find { it.'@name' == 'exttest_Plant' }.'attributes'.'*'.find { it.'@name' == 'models' }.attributes().get('@mapped-by') == null
        entityMappings.'*'.find { it.'@class' == "test_support.entity.entity_extension.Address" }
                .'attributes'.'*'.find { it.'@name' == 'place' }.attributes().get('@mapped-by') == null


        def addressManyToOne = entityMappings.'*'.find { it.'@class' == "test_support.entity.entity_extension.Address" }
                .'attributes'.'many-to-one'


        //target-entity
        addressManyToOne.'@target-entity' == "test_support.entity.entity_extension.ExtPlace"//many-to-one
        entityMappings.'*'.find { it.'@name' == 'exttest_DriverCallsign' }.'attributes'.'*'.find { it.'@name' == 'driver' }
                .'@target-entity' == 'test_support.entity.entity_extension.ExtDriver'//one-to-one
        entityMappings.'*'.find { it.'@name' == 'exttest_Station' }.'attributes'.'*'.find { it.'@name' == 'waybills' }
                .'@target-entity' == 'test_support.entity.entity_extension.CompanyWaybill'//many-to-many
        entityMappings.'*'.find { it.'@name' == 'exttest_Bus' }.'attributes'.'*'.find { it.'@name' == 'waybills' }
                .'@target-entity' == 'test_support.entity.entity_extension.CompanyWaybill'//one-to-many
        entityMappings.'*'.find { it.'@name' == 'exttest_Waybill' }.'attributes'.'*'.find { it.'@name' == 'bus' }
                .'@target-entity' == 'test_support.entity.entity_extension.BrandedBus' //2nd level extension


        //cascade
        def cascadeTypes = addressManyToOne.'*'.find { it.name() == 'cascade' }
        cascadeTypes.children().size() == 2
        cascadeTypes.children().find { it.name() == "cascade-persist" }.name() == "cascade-persist"
        cascadeTypes.children().find { it.name() == "cascade-merge" }.name() == "cascade-merge"
    }

    def "check other annotations"() {
        when:
        def entityMappings = getEntityMappings()
        def plant_doc = entityMappings.'*'.find { it.'@name' == 'exttest_Plant' }.'attributes'.'*'.find { it.'@name' == 'doc' }.'join-column'[0]
        def waybill_bus = entityMappings.'*'.find { it.'@name' == 'exttest_Waybill' }.'attributes'.'*'.find { it.'@name' == 'bus' }.'join-column'[0]
        def driver_platformEntity = entityMappings.'*'.find { it.'@name' == 'exttest_Driver' }.'attributes'.'*'.find { it.'@name' == 'platformEntity' }.'join-column'[0]

        def plant_models = entityMappings.'*'.find { it.'@name' == 'exttest_Plant' }.'attributes'.'*'.find { it.'@name' == 'models' }.'join-table'[0]
        def waybill_places = entityMappings.'*'.find { it.'@name' == 'exttest_Waybill' }.'attributes'.'*'.find { it.'@name' == 'places' }.'join-table'[0]

        def bus_waybill_orderBy = entityMappings.'*'.find { it.'@name' == 'exttest_Bus' }.'attributes'.'*'.find { it.'@name' == 'waybills' }.'order-by'[0]

        then:

        //join-column
        plant_doc.'@name' == 'DOC_ID'
        plant_doc.attributes().size() == 1

        waybill_bus.'@name' == 'BUS_ID'
        waybill_bus.'@insertable' == false
        waybill_bus.'@updatable' == false
        waybill_bus.'@nullable' == false

        driver_platformEntity.'@name' == 'PLATFORM_ENTITY_ID'
        driver_platformEntity.'@unique' == true
        driver_platformEntity.attributes().size() == 2

        //join-table

        plant_models.'@name' == 'EXTTEST_PLANT_MODEL_LINK'
        plant_models.'join-column'.'@name' == 'PLANT_ID'
        plant_models.'inverse-join-column'.'@name' == 'MODEL_ID'

        //inverse-join-column
        waybill_places.'inverse-join-column'.'@name' == 'PLACE_ID'
        waybill_places.'inverse-join-column'.'@insertable' == false
        waybill_places.'inverse-join-column'.'@updatable' == false
        waybill_places.'inverse-join-column'.'@nullable' == false

        //order-by
        bus_waybill_orderBy.text() == 'createdDate'
    }


}
