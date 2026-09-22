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

import io.jmix.core.DataManager
import io.jmix.core.LocalizedStringSupport
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestLocalizedNameEntity

/**
 * The database half of the reference table of the mechanism. The shapes and the expected texts are the ones of
 * the core {@code LocalizedStringMatrixTest}: the first feature holds the database against that table, the
 * second one names every shape the database cannot reproduce. The two tables are meant to be read side by side
 * and must be changed together.
 * <p>
 * The application default locale is {@code en} and the available locales are {@code en,de,pt_BR}.
 */
class LocalizedStringMatrixTest extends DataSpec {

    static final Locale EN = Locale.ENGLISH
    static final Locale DE = Locale.GERMAN

    @Autowired
    DataManager dataManager
    @Autowired
    LocalizedStringSupport localizedStringSupport

    void setup() {
        dataManager.save(
                item('01', 'Plain'),
                item('02', 'Multi one\nMulti two'),
                item('03', 'Default three\nen=English three\nde=Deutsch drei'),
                item('04', 'Default four\nde=Deutsch vier'),
                item('05', '\nen=English five'),
                item('06', 'de=Deutsch sechs\nfr=Francais six'),
                item('07', 'Default seven\nde='),
                item('08', 'msg://roles.manager.name'),
                item('09', null),
                item('10', ''))
    }

    TestLocalizedNameEntity item(String code, String name) {
        def e = dataManager.create(TestLocalizedNameEntity)
        e.code = code
        e.name = name
        return e
    }

    List<String> codes(Condition condition) {
        dataManager.load(TestLocalizedNameEntity).condition(condition).list()*.code.sort()
    }

    def "the database compares the same text LocalizedStringSupport resolves"() {
        when:
        authenticateWithLocale(locale)

        then:
        codes(PropertyCondition.equal('name', text)) == [code]

        where:
        shape                              | code | locale || text
        'a plain literal'                  | '01' | EN     || 'Plain'
        'a plain literal'                  | '01' | DE     || 'Plain'
        'a default value and both entries' | '03' | EN     || 'English three'
        'a default value and both entries' | '03' | DE     || 'Deutsch drei'
        'a default value and one entry'    | '04' | EN     || 'Default four'
        'a default value and one entry'    | '04' | DE     || 'Deutsch vier'
        'no default value, canonical'      | '05' | EN     || 'English five'
        'no default value, canonical'      | '05' | DE     || 'English five'
        'no default value, not canonical'  | '06' | DE     || 'Deutsch sechs'
        'an empty entry counts as absent'  | '07' | EN     || 'Default seven'
        'an empty entry counts as absent'  | '07' | DE     || 'Default seven'
    }

    def "a multi-line value is compared by its first line only"() {
        when: "the one divergence the value format itself carries"
        authenticateWithLocale(DE)

        then:
        codes(PropertyCondition.equal('name', 'Multi one')) == ['02']
        codes(PropertyCondition.equal('name', 'Multi one\nMulti two')) == []
    }

    def "a message reference is compared as the reference, not as the text it resolves to"() {
        when: "the message bundles are not reachable from SQL"
        authenticateWithLocale(DE)

        then: "the reference resolves to a text of its own, and that text is not what the database compares"
        localizedStringSupport.resolve('msg://roles.manager.name', DE) == 'Leiter'
        codes(PropertyCondition.equal('name', 'Leiter')) == []
        codes(PropertyCondition.equal('name', 'msg://roles.manager.name')) == ['08']
    }

    def "an entry written with whitespace around the key is read by the parser but not by the database"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '11'
        entity.name = 'Report eleven\nen = Book eleven'
        dataManager.save(entity)

        when:
        authenticateWithLocale(EN)

        then: "the parser tolerates the whitespace, the database marker matches the canonical form only"
        localizedStringSupport.resolve(entity.name, EN) == 'Book eleven'
        codes(PropertyCondition.equal('name', 'Book eleven')) == []
        codes(PropertyCondition.equal('name', 'Report eleven')) == ['11']
    }

    def "an entry whose key is not in the canonical case is read by a case-insensitive search, not by the parser"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '12'
        entity.name = 'Default twelve\nDE=Deutsch zwoelf'
        dataManager.save(entity)

        when:
        authenticateWithLocale(DE)

        then: "the parser keeps the key as written and looks it up case-sensitively, so it reads the default value"
        localizedStringSupport.resolve(entity.name, DE) == 'Default twelve'

        and: "an operation that compares the stored case agrees with the parser"
        codes(PropertyCondition.equal('name', 'Default twelve')) == ['12']

        and: "a case-insensitive operation lower-cases the column and the marker alike, so the entry wins there: " +
                "the row is found by a text the user never sees and not by the one they do"
        codes(PropertyCondition.contains('name', 'zwoelf')) == ['12']
        codes(PropertyCondition.contains('name', 'twelve')) == []
    }

    def "an empty text is null in the database when the column is null"() {
        when: "LocalizedStringSupport reads both an empty column and a null one as an empty text"
        authenticateWithLocale(EN)

        then: "in the database the null column stays null, so the store's null handling applies to it. This " +
                "holds where the empty string is a value of its own, as it is on HSQLDB, which the suite runs " +
                "on; on Oracle the empty string is null and both rows answer null, which tests/coverage.md " +
                "records as an unrun per-database check rather than a regression"
        codes(PropertyCondition.equal('name', '')) == ['06', '10']
        codes(PropertyCondition.isSet('name', false)) == ['09']
    }
}
