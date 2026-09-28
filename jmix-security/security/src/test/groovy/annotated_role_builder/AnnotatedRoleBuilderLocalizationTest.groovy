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

package annotated_role_builder

import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider
import io.jmix.security.impl.role.provider.AnnotatedRowLevelRoleProvider
import io.jmix.security.role.ResourceRoleRepository
import io.jmix.security.role.RowLevelRoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import test_support.RoleLocalizationSpecification
import test_support.role_localization.PrefixingRoleBuilder
import test_support.role_localization.TestBlankDefaultMessageRole
import test_support.role_localization.TestGroupReferenceRole
import test_support.role_localization.TestLiteralRole
import test_support.role_localization.TestMainGroupReferenceRole
import test_support.role_localization.TestMissingKeyRole
import test_support.role_localization.TestShortReferenceRole
import test_support.role_localization.TestShortReferenceRowLevelRole

/**
 * The roles are built and provided for an English user, so that the text they keep is told apart from the text of
 * the current locale.
 */
class AnnotatedRoleBuilderLocalizationTest extends RoleLocalizationSpecification {

    static final String OWN_GROUP = 'test_support.role_localization'
    static final String OTHER_GROUP = 'test_support.other_group'

    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder
    @Autowired
    ResourceRoleRepository resourceRoleRepository
    @Autowired
    RowLevelRoleRepository rowLevelRoleRepository
    @Autowired
    AnnotatedResourceRoleProvider annotatedResourceRoleProvider
    @Autowired
    AnnotatedRowLevelRoleProvider annotatedRowLevelRoleProvider
    @Autowired
    ApplicationContext applicationContext

    void setup() {
        authenticate(Locale.ENGLISH)
    }

    def "building a role keeps the keys of its references and takes the texts of the default locale"() {
        when:
        def role = annotatedRoleBuilder.createResourceRole(TestShortReferenceRole.name)

        then:
        role.nameMessageKey == "$OWN_GROUP/roles.shortReference.name"
        role.name == 'Rolle mit kurzer Referenz'
        role.descriptionMessageKey == "$OWN_GROUP/roles.shortReference.description"
        role.description == 'Eine über eine kurze Referenz benannte Rolle'
    }

    def "a role is keyed by the form of its reference and provided with the text of the default locale: #code"() {
        when:
        def role = resourceRoleRepository.getRoleByCode(code)

        then:
        role.nameMessageKey == nameMessageKey
        role.name == name

        where:
        code                            || nameMessageKey                           | name
        TestShortReferenceRole.CODE     || "$OWN_GROUP/roles.shortReference.name"   | 'Rolle mit kurzer Referenz'
        TestGroupReferenceRole.CODE     || "$OTHER_GROUP/roles.groupReference.name" | 'Rolle mit Gruppenreferenz'
        TestMainGroupReferenceRole.CODE || 'roles.mainGroupReference.name'          | 'Rolle der Hauptgruppe'
        TestMissingKeyRole.CODE         || "$OWN_GROUP/roles.missing.name"          | "$OWN_GROUP/roles.missing.name"
        TestLiteralRole.CODE            || null                                     | 'Literal role'
    }

    def "a description is keyed and provided the way a name is"() {
        expect:
        with(resourceRoleRepository.getRoleByCode(TestShortReferenceRole.CODE)) {
            descriptionMessageKey == "$OWN_GROUP/roles.shortReference.description"
            description == 'Eine über eine kurze Referenz benannte Rolle'
        }
        with(resourceRoleRepository.getRoleByCode(TestLiteralRole.CODE)) {
            descriptionMessageKey == null
            description == ''
        }
    }

    def "a row-level role is localized the same way"() {
        when:
        def role = rowLevelRoleRepository.getRoleByCode(TestShortReferenceRowLevelRole.CODE)

        then:
        role.nameMessageKey == "$OWN_GROUP/roles.shortReferenceRowLevel.name"
        role.name == 'Zeilenrolle mit kurzer Referenz'
        role.descriptionMessageKey == "$OWN_GROUP/roles.shortReferenceRowLevel.description"
        role.description == 'Eine über eine kurze Referenz benannte Zeilenrolle'
    }

    def "a name whose message is blank in the default locale is the key, as the name of a missing message is"() {
        expect:
        resourceRoleRepository.getRoleByCode(TestBlankDefaultMessageRole.CODE).name ==
                "$OWN_GROUP/roles.blankDefaultMessage.name"
    }

    def "a builder that overrides the hook of the base parameters still has it called"() {
        given:
        def builder = applicationContext.autowireCapableBeanFactory.createBean(PrefixingRoleBuilder)

        when:
        def role = builder.createResourceRole(TestShortReferenceRole.name)

        then:
        role.name == '[custom] Rolle mit kurzer Referenz'
        role.nameMessageKey == "$OWN_GROUP/roles.shortReference.name"
    }

    def "roles rebuilt by hot deploy carry the texts of the default locale"() {
        when:
        annotatedResourceRoleProvider.refreshRoles()
        annotatedRowLevelRoleProvider.refreshRoles()

        then:
        annotatedResourceRoleProvider.findRoleByCode(TestShortReferenceRole.CODE).name == 'Rolle mit kurzer Referenz'
        annotatedRowLevelRoleProvider.findRoleByCode(TestShortReferenceRowLevelRole.CODE).name ==
                'Zeilenrolle mit kurzer Referenz'
    }
}
