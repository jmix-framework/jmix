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

package view_template

import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.flowui.view.template.ViewTemplateHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.viewtemplate.ViewTemplateFilteringEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DefaultViewTemplateHelperTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    @Autowired
    ViewTemplateHelper templateHelper

    MetaClass entityMetaClass

    @Override
    void setup() {
        entityMetaClass = metadata.getClass(ViewTemplateFilteringEntity)
    }

    def "Exclude technical and unsupported properties by default"() {
        when:
        List<String> propertyNames = templateHelper.getProperties(entityMetaClass, [], [])*.name

        then:
        propertyNames.containsAll(['name', 'active', 'customer'])
        !propertyNames.contains('id')
        !propertyNames.contains('version')
        !propertyNames.contains('createTs')
        !propertyNames.contains('createdBy')
        !propertyNames.contains('updateTs')
        !propertyNames.contains('updatedBy')
        !propertyNames.contains('deleteTs')
        !propertyNames.contains('deletedBy')
        !propertyNames.contains('secretToken')
        !propertyNames.contains('systemValue')
        !propertyNames.contains('address')
        !propertyNames.contains('tags')
        propertyNames == entityMetaClass.properties.findAll { propertyNames.contains(it.name) }*.name
    }

    def "Include properties restores supported direct properties only"() {
        when:
        List<String> propertyNames = templateHelper.getProperties(entityMetaClass, ['createdBy', 'address', 'tags'], [])*.name

        then:
        propertyNames.contains('createdBy')
        !propertyNames.contains('address')
        !propertyNames.contains('tags')
        propertyNames == entityMetaClass.properties.findAll { propertyNames.contains(it.name) }*.name
    }

    def "Exclude properties removes matching properties after inclusion"() {
        when:
        List<String> propertyNames = templateHelper.getProperties(entityMetaClass, ['createdBy', 'active'], ['active', 'customer'])*.name

        then:
        propertyNames.contains('createdBy')
        !propertyNames.contains('active')
        !propertyNames.contains('customer')
    }

    def "Fail on property path in parameters"() {
        when:
        templateHelper.getProperties(entityMetaClass, ['customer.name'], [])

        then:
        thrown(IllegalArgumentException)
    }
}
