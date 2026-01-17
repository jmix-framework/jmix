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

package io.jmix.search.index.impl.dynattr

import io.jmix.core.JmixModuleDescriptor
import io.jmix.core.JmixModules
import io.jmix.core.metamodel.model.MetaPropertyPath
import org.springframework.beans.factory.ObjectProvider
import spock.lang.Specification

import static io.jmix.search.index.impl.dynattr.DynamicAttributesSupport.DYNAMIC_ATTRIBUTES_MODULE_ID

class DynamicAttributesSupportTest extends Specification {

    def "IsDynamicAttributeName. Module is absent"() {
        given:
        ObjectProvider proxyProvider = Mock()
        proxyProvider.getIfAvailable() >> null

        and:
        DynamicAttributesSupport support = new DynamicAttributesSupport(Mock(JmixModules), proxyProvider)

        when:
        def isDynamic = support.isDynamicAttributeName("anyName")

        then:
        !isDynamic
    }

    def "IsDynamicAttribute. Module is absent"() {
        given:
        ObjectProvider proxyProvider = Mock()
        proxyProvider.getIfAvailable() >> null

        and:
        DynamicAttributesSupport support = new DynamicAttributesSupport(Mock(JmixModules), proxyProvider)

        and:
        MetaPropertyPath path = Mock()

        when:
        def isDynamic = support.isDynamicAttribute(path)

        then:
        !isDynamic
    }

    def "IsDynamicAttributeName. Module is present"() {
        when:
        DynamicAttributesSupportDelegate proxy = Mock()
        proxy.isDynamicAttributeName(attributeName) >> isDynamic

        ObjectProvider proxyProvider = Mock()
        proxyProvider.getIfAvailable() >> proxy

        DynamicAttributesSupport support = new DynamicAttributesSupport(Mock(JmixModules), proxyProvider)

        def result = support.isDynamicAttributeName(attributeName)

        then:
        result == isDynamic

        where:
        attributeName          || isDynamic
        "dynamicAttributeName" || true
        "staticAttributeName"  || false
    }

    def "IsDynamicAttribute. Module is present"() {
        when:
        DynamicAttributesSupportDelegate proxy = Mock()
        proxy.isDynamicAttributeName(attributeName) >> isDynamic

        ObjectProvider proxyProvider = Mock()
        proxyProvider.getIfAvailable() >> proxy

        MetaPropertyPath path = Mock()
        path.getFirstPropertyName() >> attributeName

        DynamicAttributesSupport support = new DynamicAttributesSupport(Mock(JmixModules), proxyProvider)

        def result = support.isDynamicAttribute(path)

        then:
        result == isDynamic

        where:
        attributeName          || isDynamic
        "dynamicAttributeName" || true
        "staticAttributeName"  || false
    }

    def "isModulePresent"() {
        given:
        def jmixModules = Mock(JmixModules)

        and:
        DynamicAttributesSupport support = new DynamicAttributesSupport(jmixModules, Mock(ObjectProvider))

        when:
        jmixModules.get(DYNAMIC_ATTRIBUTES_MODULE_ID) >> dyanttrModule
        def result = support.isModulePresent()

        then:
        result == expectedResult

        where:
        dyanttrModule              || expectedResult
        Mock(JmixModuleDescriptor) || true
        null                       || false
    }
}
