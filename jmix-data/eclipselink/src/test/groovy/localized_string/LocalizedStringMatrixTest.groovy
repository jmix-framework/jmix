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
                item('10', ''),
                item('17', 'Default seventeen\nde=Zeile siebzehn'))
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
        'an entry with a line separator'   | '17' | EN     || 'Default seventeen'
        'an entry with a line separator'   | '17' | DE     || 'Zeile siebzehn'
    }

    def "a multi-line default value is compared by its first line only"() {
        when: "a divergence the value format itself carries"
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

    def "a line written with whitespace around the key is text to the parser and to the database alike"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '11'
        entity.name = 'Report eleven\nen = Book eleven'
        dataManager.save(entity)

        when:
        authenticateWithLocale(EN)

        then: "neither side reads an entry: the parser shows the whole value and the database compares its first " +
                "line, as it does for any multi-line value"
        localizedStringSupport.resolve(entity.name, EN) == 'Report eleven\nen = Book eleven'
        codes(PropertyCondition.equal('name', 'Book eleven')) == []
        codes(PropertyCondition.equal('name', 'Report eleven')) == ['11']
    }

    def "a first line whose key is not in the canonical case is the default value, not an entry"() {
        given: "the key in another case than the canonical one"
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '16'
        entity.name = 'DE=Bericht sechzehn'
        dataManager.save(entity)

        when:
        authenticateWithLocale(EN)

        then: "the parser and the first-line guard both read an entry in the canonical case only"
        localizedStringSupport.resolve(entity.name, EN) == 'DE=Bericht sechzehn'
        codes(PropertyCondition.equal('name', 'DE=Bericht sechzehn')) == ['16']
        codes(PropertyCondition.contains('name', 'sechzehn')) == ['16']
    }

    def "a space after the sign is stripped by the parser and by the database alike"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '13'
        entity.name = 'Report thirteen\nde=  Bericht dreizehn'
        dataManager.save(entity)

        when:
        authenticateWithLocale(DE)

        then:
        localizedStringSupport.resolve(entity.name, DE) == 'Bericht dreizehn'
        codes(PropertyCondition.equal('name', 'Bericht dreizehn')) == ['13']
    }

    def "a tab after the sign is stripped by the parser but not by the database"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '14'
        entity.name = 'Report fourteen\nde=\tBericht vierzehn'
        dataManager.save(entity)

        when:
        authenticateWithLocale(DE)

        then: "TRIM strips spaces only, while the parser strips any whitespace"
        localizedStringSupport.resolve(entity.name, DE) == 'Bericht vierzehn'
        codes(PropertyCondition.equal('name', 'Bericht vierzehn')) == []
        codes(PropertyCondition.equal('name', '\tBericht vierzehn')) == ['14']
    }

    def "a multi-line entry is compared by its first line only"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '15'
        entity.name = 'Report fifteen\nde=Zeile eins\nZeile zwei'
        dataManager.save(entity)

        when:
        authenticateWithLocale(DE)

        then: "the parser appends a continuation line to the entry, the database ends the entry at the line break"
        localizedStringSupport.resolve(entity.name, DE) == 'Zeile eins\nZeile zwei'
        codes(PropertyCondition.equal('name', 'Zeile eins')) == ['15']
        codes(PropertyCondition.equal('name', 'Zeile eins\nZeile zwei')) == []
    }

    def "a line whose key is not in the canonical case continues the value on both sides"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '12'
        entity.name = 'Default twelve\nDE=Deutsch zwoelf'
        dataManager.save(entity)

        when:
        authenticateWithLocale(DE)

        then: "the parser reads the line as a continuation of the default value"
        localizedStringSupport.resolve(entity.name, DE) == 'Default twelve\nDE=Deutsch zwoelf'

        and: "the database finds no entry either and compares the first line, as it does for any multi-line " +
                "value. A collation that ignores case, or MySQL, where the column is lower-cased for a " +
                "case-insensitive search, lets the marker find the line and read an entry instead"
        codes(PropertyCondition.equal('name', 'Default twelve')) == ['12']
        codes(PropertyCondition.contains('name', 'twelve')) == ['12']
        codes(PropertyCondition.contains('name', 'zwoelf')) == []
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
