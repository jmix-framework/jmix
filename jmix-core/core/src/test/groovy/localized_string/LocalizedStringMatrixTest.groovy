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

package localized_string

import io.jmix.core.CoreConfiguration
import io.jmix.core.LocalizedStringSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.base.TestBaseConfiguration
import test_support.localized_string.TestLocalizedStringConfiguration

/**
 * The reference table of the mechanism: every shape a stored value can have, and the text each locale reads
 * from it. The database side is held against the same table by the eclipselink {@code LocalizedStringMatrixTest},
 * which also names the shapes SQL cannot reproduce; the two tables are meant to be read side by side and must
 * be changed together.
 * <p>
 * This context configures no available locales, so the application default locale is {@code en}.
 */
@ContextConfiguration(classes = [CoreConfiguration, TestBaseConfiguration, TestLocalizedStringConfiguration])
class LocalizedStringMatrixTest extends Specification {

    static final Locale EN = Locale.ENGLISH
    static final Locale DE = Locale.GERMAN

    @Autowired
    LocalizedStringSupport support

    def "every shape of a stored value resolves to the documented text"() {
        expect:
        support.resolve(stored, EN) == en
        support.resolve(stored, DE) == de

        where:
        shape                              | stored                                              || en                     | de
        'a plain literal'                  | 'Plain'                                             || 'Plain'                | 'Plain'
        'a multi-line literal'             | 'Multi one\nMulti two'                              || 'Multi one\nMulti two' | 'Multi one\nMulti two'
        'a default value and both entries' | 'Default three\nen=English three\nde=Deutsch drei'  || 'English three'        | 'Deutsch drei'
        'a default value and one entry'    | 'Default four\nde=Deutsch vier'                     || 'Default four'         | 'Deutsch vier'
        'no default value, canonical'      | '\nen=English five'                                 || 'English five'         | 'English five'
        'no default value, not canonical'  | 'de=Deutsch sechs\nfr=Francais six'                 || ''                     | 'Deutsch sechs'
        'an empty entry counts as absent'  | 'Default seven\nde='                                || 'Default seven'        | 'Default seven'
        'an entry with a line separator'   | 'Default eight\nde=Zeile acht'                 || 'Default eight'        | 'Zeile acht'
        'a message reference'              | 'msg://roles.manager.name'                          || 'Manager'              | 'Leiter'
        'null'                             | null                                                || ''                     | ''
        'an empty string'                  | ''                                                  || ''                     | ''
    }

    def "writing back a parsed value produces the canonical form of the same shape"() {
        expect:
        support.format(support.parse(stored)) == canonical

        where:
        shape                              | stored                                              || canonical
        'a plain literal'                  | 'Plain'                                             || 'Plain'
        'a multi-line literal'             | 'Multi one\nMulti two'                              || 'Multi one\nMulti two'
        'a default value and both entries' | 'Default three\nen=English three\nde=Deutsch drei'  || 'Default three\nen=English three\nde=Deutsch drei'
        'a default value and one entry'    | 'Default four\nde=Deutsch vier'                     || 'Default four\nde=Deutsch vier'
        'no default value, canonical'      | '\nen=English five'                                 || '\nen=English five'
        'no default value, not canonical'  | 'de=Deutsch sechs\nfr=Francais six'                 || '\nde=Deutsch sechs\nfr=Francais six'
        'an empty entry counts as absent'  | 'Default seven\nde='                                || 'Default seven'
        'an entry with a line separator'   | 'Default eight\nde=Zeile acht'                 || 'Default eight\nde=Zeile acht'
        'a message reference'              | 'msg://roles.manager.name'                          || 'msg://roles.manager.name'
        'null'                             | null                                                || ''
        'an empty string'                  | ''                                                  || ''
    }
}
