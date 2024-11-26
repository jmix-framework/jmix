/*
 * Copyright 2024 Haulmont.
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

package component.genericfilter

import component.genericfilter.view.FilterMetadataToolsTestView
import io.jmix.flowui.component.genericfilter.FilterMetadataTools
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FilterMetadataToolsTest extends FlowuiTestSpecification {

    @Autowired
    FilterMetadataTools filterMetadataTools

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "test JPA entity from main store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.ordersFilter)

        then:
        paths.collect(it -> it.toPathString()).containsAll([
                'id', 'version', 'updatedBy', 'number', 'date', 'description',
                'customer', 'customer.id', 'customer.version', 'customer.updatedBy', 'customer.name',
                'orderLines', 'orderLines.id', 'orderLines.version', 'orderLines.updatedBy', 'orderLines.quantity', 'orderLines.product'
        ])
    }

    def "test JPA entity from main store having references to JPA and DTO entities"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.mainDsEntityFilter)

        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['db1JpaEntity',  'embedded', 'embedded.city', 'id', 'mem1DtoEntity', 'name',
                       'noStoreDtoEntity', 'noStoreDtoEntity.id', 'noStoreDtoEntity.name']
    }

    def "test JPA entity from additional store having reference to JPA entity from main store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.db1JpaEntityFilter)


        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['embedded', 'embedded.city', 'id', 'mainDsEntity', 'name']
    }

    def "test DTO entity from additional store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.mem1DtoEntityFilter)


        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['entity2', 'entity2.id', 'entity2.name', 'id', 'name']
    }

    def "test DTO entity from additional store having reference to DTO entity from another store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.mem2DtoEntityFilter)


        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['id', 'mem1DtoEntity', 'name']
    }

    def "test DTO entity without store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.noStoreDtoEntityFilter)


        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['id', 'name']
    }

    def "test key-value from main store"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.ordersKeyValueFilter)

        then:
        def properties = paths.collect(it -> it.toPathString())
        properties.containsAll([
                'id', 'name', 'customerName',
                'user', 'user.id', 'user.login', 'user.name'
        ])

        and:
        !properties.contains('date')
        !properties.contains('customer.id')
        !properties.contains('customer.name')
    }

    def "test key-value from main store with embedded"() {
        when:
        def view = navigateToView(FilterMetadataToolsTestView)
        def paths = filterMetadataTools.getPropertyPaths(view.mainDsEntityKeyValueFilter)

        then:
        def properties = paths.collect(it -> it.toPathString()).sort()
        properties == ['city', 'id', 'name']
    }

}
