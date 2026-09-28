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

package role_localization

import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder
import io.jmix.security.impl.role.provider.AnnotatedResourceRoleProvider
import io.jmix.security.impl.role.provider.AnnotatedRowLevelRoleProvider
import io.jmix.security.model.ResourceRole
import io.jmix.security.model.ResourceRoleModel
import io.jmix.security.model.RoleModelConverter
import io.jmix.security.model.RowLevelRole
import io.jmix.security.model.RowLevelRoleModel
import io.jmix.security.role.RoleLocalizationSupport
import org.springframework.beans.factory.annotation.Autowired
import test_support.RoleLocalizationSpecification
import test_support.role_localization.TestBlankMessageRole
import test_support.role_localization.TestDefaultLocaleOnlyRole
import test_support.role_localization.TestLiteralRole
import test_support.role_localization.TestMissingKeyRole
import test_support.role_localization.TestShortReferenceRole
import test_support.role_localization.TestShortReferenceRowLevelRole

class RoleLocalizationSupportTest extends RoleLocalizationSpecification {

    static final Locale PT_BR = Locale.forLanguageTag('pt-BR')
    static final Locale SR_LATN = Locale.forLanguageTag('sr-Latn')

    @Autowired
    RoleLocalizationSupport roleLocalizationSupport
    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder
    @Autowired
    AnnotatedResourceRoleProvider annotatedResourceRoleProvider
    @Autowired
    AnnotatedRowLevelRoleProvider annotatedRowLevelRoleProvider
    @Autowired
    RoleModelConverter roleModelConverter
    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools

    /**
     * A design-time role as its provider holds it: with the texts of the default locale and the keys of the messages.
     */
    ResourceRole designTimeRole(String code) {
        annotatedResourceRoleProvider.findRoleByCode(code)
    }

    RowLevelRole designTimeRowLevelRole(String code) {
        annotatedRowLevelRoleProvider.findRoleByCode(code)
    }

    /**
     * A role the way a database role arrives: a default text and localized values, no message key.
     */
    static ResourceRole databaseRole(String localizedNames, String localizedDescriptions = null) {
        def role = new ResourceRole()
        role.code = 'order-manager'
        role.name = 'Manager'
        role.description = 'Manages orders'
        role.localizedNames = localizedNames
        role.localizedDescriptions = localizedDescriptions
        return role
    }

    def "a role with a message key shows the message of the user's locale"() {
        given:
        authenticate(Locale.ENGLISH)
        def role = designTimeRole(TestShortReferenceRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'Short reference role'
        roleLocalizationSupport.getLocalizedDescription(role) == 'A role named through a short reference'
    }

    def "an explicit locale is used instead of the user's"() {
        given:
        authenticate(Locale.GERMAN)
        def designTime = designTimeRole(TestShortReferenceRole.CODE)
        def databaseRole = databaseRole('de=Leiter', 'de=Verwaltet Bestellungen')

        expect:
        roleLocalizationSupport.getLocalizedName(designTime, Locale.ENGLISH) == 'Short reference role'
        roleLocalizationSupport.getLocalizedName(databaseRole, Locale.ENGLISH) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(databaseRole, Locale.ENGLISH) == 'Manages orders'
    }

    def "a message missing or blank in the user's locale falls back to the text of the default locale"() {
        given: "the English lookup reaches only the locale-less bundle: it lacks one message and blanks another"
        authenticate(Locale.ENGLISH)
        def missing = designTimeRole(TestDefaultLocaleOnlyRole.CODE)
        def blank = designTimeRole(TestBlankMessageRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedName(missing) == 'Nur in der Standardsprache'
        roleLocalizationSupport.getLocalizedDescription(missing) == 'Nur in der Standardsprache beschrieben'
        roleLocalizationSupport.getLocalizedName(blank) == 'Mit leerer Übersetzung'
    }

    def "a role that keeps only the keys of its messages is shown with the messages of the default locale"() {
        given: "a role as a custom provider may make it: the keys and the references, but no texts"
        authenticate(Locale.ENGLISH)
        def role = new ResourceRole(code: 'keys-only', name: 'msg://roles.defaultLocaleOnly.name',
                nameMessageKey: 'test_support.role_localization/roles.defaultLocaleOnly.name',
                description: 'msg://roles.defaultLocaleOnly.description',
                descriptionMessageKey: 'test_support.role_localization/roles.defaultLocaleOnly.description')

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'Nur in der Standardsprache'
        roleLocalizationSupport.getLocalizedDescription(role) == 'Nur in der Standardsprache beschrieben'
    }

    def "a message missing in every locale shows the text the role keeps: the key"() {
        given:
        authenticate(Locale.ENGLISH)
        def role = designTimeRole(TestMissingKeyRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'test_support.role_localization/roles.missing.name'
    }

    def "the message key wins over localized names"() {
        given:
        authenticate(Locale.GERMAN)
        def role = annotatedRoleBuilder.createResourceRole(TestShortReferenceRole.name)
        role.localizedNames = 'de=Übersetzt'

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'Rolle mit kurzer Referenz'
    }

    def "a localized value comes from the exact locale key, the language or the default text: #situation"() {
        given: "one role localizes only its name, another only its description, so that each reads values of its own"
        authenticate(locale)
        def named = databaseRole(localized, null)
        def described = databaseRole(null, localized)

        expect:
        roleLocalizationSupport.getLocalizedName(named) == name
        roleLocalizationSupport.getLocalizedDescription(named) == 'Manages orders'
        roleLocalizationSupport.getLocalizedName(described) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(described) == description

        where:
        situation                  | locale        | localized                      || name         | description
        'the exact key'            | PT_BR         | 'pt=Gerente\npt_BR=Gerente BR' || 'Gerente BR' | 'Gerente BR'
        'a key with a script'      | SR_LATN       | 'sr=Менаџер\nsr-Latn=Menadžer' || 'Menadžer'   | 'Menadžer'
        'the language'             | PT_BR         | 'pt=Gerente\nde=Leiter'        || 'Gerente'    | 'Gerente'
        'nothing for the locale'   | Locale.GERMAN | 'ru=Менеджер'                  || 'Manager'    | 'Manages orders'
        'nothing localized at all' | Locale.GERMAN | null                           || 'Manager'    | 'Manages orders'
        'an empty exact value'     | PT_BR         | 'pt_BR=\npt=Gerente'           || 'Gerente'    | 'Gerente'
        'an empty value'           | Locale.GERMAN | 'de='                          || 'Manager'    | 'Manages orders'
        'a value of spaces'        | Locale.GERMAN | 'de=\\ \\ '                    || 'Manager'    | 'Manages orders'
    }

    def "a bundle that cannot be read is dropped as a whole, with the values read before the broken one"() {
        given:
        authenticate(Locale.GERMAN)
        def role = databaseRole('de=Leiter\nru=\\u00', 'de=Verwaltet Bestellungen\nru=\\u00')

        expect:
        roleLocalizationSupport.getLocalizedName(role) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(role) == 'Manages orders'
    }

    def "a role without a description has none in any locale, whichever kind of role it is"() {
        given: "a database role keeps null, a design-time role the empty string its annotation declares by default"
        authenticate(Locale.GERMAN)
        def databaseRole = databaseRole('de=Leiter')
        databaseRole.description = null
        def designTime = designTimeRole(TestLiteralRole.CODE)

        expect:
        roleLocalizationSupport.getLocalizedDescription(databaseRole) == null
        roleLocalizationSupport.getLocalizedDescription(designTime) == null
        roleLocalizationSupport.getLocalizedDescription(roleModelConverter.createResourceRoleModel(designTime)) == null
    }

    def "a role model is localized the way its role is, the description from values of its own"() {
        given: "the user's locale is not the default one, so the design-time role keeps a German text"
        authenticate(Locale.ENGLISH)
        def designTimeModel = roleModelConverter.createResourceRoleModel(designTimeRole(TestShortReferenceRole.CODE))
        def databaseModel = roleModelConverter.createResourceRoleModel(
                databaseRole('en=Leader', 'en=Leads the orders'))

        expect:
        roleLocalizationSupport.getLocalizedName(designTimeModel) == 'Short reference role'
        roleLocalizationSupport.getLocalizedDescription(designTimeModel) == 'A role named through a short reference'
        roleLocalizationSupport.getLocalizedName(databaseModel) == 'Leader'
        roleLocalizationSupport.getLocalizedDescription(databaseModel) == 'Leads the orders'
        roleLocalizationSupport.getLocalizedName(databaseModel, Locale.GERMAN) == 'Manager'
        roleLocalizationSupport.getLocalizedDescription(databaseModel, Locale.GERMAN) == 'Manages orders'
    }

    def "a role without a name is shown with an empty one, never with null"() {
        given:
        authenticate(Locale.GERMAN)
        def role = databaseRole(null)
        role.name = null

        expect:
        roleLocalizationSupport.getLocalizedName(role) == ''
        roleLocalizationSupport.getLocalizedName(roleModelConverter.createResourceRoleModel(role)) == ''
    }

    def "the instance name of both role models is their localized name"() {
        given:
        authenticate(Locale.ENGLISH)
        def designTimeModel = roleModelConverter.createResourceRoleModel(designTimeRole(TestShortReferenceRole.CODE))
        def databaseModel = roleModelConverter.createResourceRoleModel(databaseRole('en=Leader'))
        def designTimeRowLevelModel = roleModelConverter.createRowLevelRoleModel(
                designTimeRowLevelRole(TestShortReferenceRowLevelRole.CODE))
        def databaseRowLevelModel = roleModelConverter.createRowLevelRoleModel(
                new RowLevelRole(code: 'order-reader', name: 'Reader', localizedNames: 'en=Order reader'))

        expect:
        metadataTools.getInstanceName(designTimeModel) == 'Short reference role'
        metadataTools.getInstanceName(databaseModel) == 'Leader'
        metadataTools.getInstanceName(designTimeRowLevelModel) == 'Short reference row-level role'
        metadataTools.getInstanceName(databaseRowLevelModel) == 'Order reader'
    }

    def "the instance name of both role models depends on the properties it is localized from"() {
        expect:
        instanceNameProperties(ResourceRoleModel) == ['name', 'nameMessageKey', 'localizedNames'] as Set
        instanceNameProperties(RowLevelRoleModel) == ['name', 'nameMessageKey', 'localizedNames'] as Set
    }

    Set<String> instanceNameProperties(Class<?> modelClass) {
        metadataTools.getInstanceNameRelatedProperties(metadata.getClass(modelClass))*.name as Set
    }
}
