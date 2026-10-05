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

import io.jmix.security.impl.role.RoleLocalizedValuesUtils
import spock.lang.Specification

class RoleLocalizedValuesUtilsTest extends Specification {

    def "a bundle is read into its entries, and no bundle into none"() {
        expect:
        RoleLocalizedValuesUtils.read(bundle) == entries

        where:
        bundle                                         || entries
        'de=Leiter\nsr-Latn=Menadžer\npt_BR=Gerente' || [de: 'Leiter', 'sr-Latn': 'Menadžer', pt_BR: 'Gerente']
        null                                           || [:]
        ''                                             || [:]
    }

    def "written entries are read back as they were, with their text as it is and no comment line"() {
        given: "values that the format has to escape: separators, a comment mark, line breaks and leading spaces"
        def entries = [ru: 'Администратор', de: 'Leiter: Einkauf = Verkauf', en: '#1 manager\nof orders',
                       fr: '  Gestionnaire']

        when:
        def bundle = RoleLocalizedValuesUtils.write(entries)

        then:
        RoleLocalizedValuesUtils.read(bundle) == entries
        bundle.contains('Администратор')
        bundle.readLines().every { !it.startsWith('#') }

        and: "no entries are written as no bundle"
        RoleLocalizedValuesUtils.write([:]) == null
    }
}
