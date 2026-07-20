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

package annotated_role_provider

import io.jmix.security.impl.role.provider.AnnotatedRoleCodeMapBuilder
import io.jmix.security.model.ResourceRole
import io.jmix.security.role.DuplicateRoleCodeException
import spock.lang.Specification

import java.util.function.Function

class DuplicateRoleCodeTest extends Specification {

    def "annotated roles with duplicate code fail on build"() {
        given:
        Function<String, ResourceRole> factory = { className -> role('dup') }

        when:
        AnnotatedRoleCodeMapBuilder.build(['com.example.RoleA', 'com.example.RoleB'] as LinkedHashSet,
                factory, 'resource')

        then:
        def e = thrown(DuplicateRoleCodeException)
        e.code == 'dup'
        e.message.contains('com.example.RoleA')
        e.message.contains('com.example.RoleB')
        e.message.contains('resource')
    }

    def "distinct annotated role codes build a map"() {
        given:
        Function<String, ResourceRole> factory = { className ->
            role(className == 'com.example.RoleA' ? 'code-a' : 'code-b')
        }

        when:
        def map = AnnotatedRoleCodeMapBuilder.build(['com.example.RoleA', 'com.example.RoleB'] as LinkedHashSet,
                factory, 'resource')

        then:
        map.size() == 2
        map['code-a'] != null
        map['code-b'] != null
    }

    def "role type is reported in the message"() {
        given:
        Function<String, ResourceRole> factory = { className -> role('dup') }

        when:
        AnnotatedRoleCodeMapBuilder.build(['A', 'B'] as LinkedHashSet, factory, 'row-level')

        then:
        def e = thrown(DuplicateRoleCodeException)
        e.message.contains('row-level')
    }

    private static ResourceRole role(String code) {
        def role = new ResourceRole()
        role.code = code
        return role
    }
}
