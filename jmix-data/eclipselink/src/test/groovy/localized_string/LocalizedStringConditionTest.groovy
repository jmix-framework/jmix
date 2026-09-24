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
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.data.impl.JpqlQueryBuilder
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestLocalizedNameEntity
import test_support.entity.TestLocalizedNameGroup
import test_support.entity.TestLocalizedNameHolder

class LocalizedStringConditionTest extends DataSpec {

    static final Locale DE = Locale.GERMAN

    @Autowired
    DataManager dataManager
    @Autowired
    BeanFactory beanFactory

    void setup() {
        dataManager.save(
                item('1', 'Report\nde=Bericht'),
                item('2', 'Plain literal'),
                item('3', 'Draft\nen=Draft 50%\nde=Entwurf_1'),
                item('4', '\nen=Only english'))
        authenticateWithLocale(DE)
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

    String jpqlOf(Condition condition) {
        def queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_LocalizedNameEntity e')
                .setEntityName('test_LocalizedNameEntity')
                .setCondition(condition)
                .setQueryParameters([:])
        return queryBuilder.getResultQueryString()
    }

    def "EQUAL and NOT_EQUAL compare the text of the current locale"() {
        expect:
        codes(PropertyCondition.equal('name', 'Bericht')) == ['1']
        codes(PropertyCondition.equal('name', 'Report')) == []
        codes(PropertyCondition.equal('name', 'Plain literal')) == ['2']
        codes(PropertyCondition.equal('name', 'Only english')) == ['4']
        codes(PropertyCondition.notEqual('name', 'Bericht')) == ['2', '3', '4']
    }

    def "CONTAINS, STARTS_WITH, ENDS_WITH, NOT_CONTAINS are case-insensitive and honour escaped wildcards"() {
        expect:
        codes(PropertyCondition.contains('name', 'beri')) == ['1']
        codes(PropertyCondition.contains('name', 'LITERAL')) == ['2']
        codes(PropertyCondition.startsWith('name', 'entw')) == ['3']
        codes(PropertyCondition.createWithValue('name', PropertyCondition.Operation.ENDS_WITH, 'ENGLISH')) == ['4']

        and: "'i' is in every resolved text but Entwurf_1"
        codes(PropertyCondition.createWithValue('name', PropertyCondition.Operation.NOT_CONTAINS, 'i')) == ['3']

        and: "the text of another locale is not searched"
        codes(PropertyCondition.contains('name', 'Draft')) == []
    }

    def "an escaped underscore is a literal character, an unescaped one is a wildcard"() {
        given: "row 3 carries the underscore itself; this row has any character in its place"
        def sketch = dataManager.create(TestLocalizedNameEntity)
        sketch.code = '0'
        sketch.name = 'Sketch\nde=Entwurf 1'
        dataManager.save(sketch)

        expect:
        codes(PropertyCondition.contains('name', 'f\\_1')) == ['3']
        codes(PropertyCondition.contains('name', 'f_1')) == ['0', '3']
    }

    def "IN_LIST and NOT_IN_LIST compare the text of the current locale"() {
        expect:
        codes(PropertyCondition.inList('name', ['Bericht', 'Plain literal'])) == ['1', '2']
        codes(PropertyCondition.createWithValue('name', PropertyCondition.Operation.NOT_IN_LIST, ['Bericht'])) == ['2', '3', '4']
    }

    def "IN_LIST searches the listed values instead of taking the resolved text as the operand of IN"() {
        when:
        def jpql = jpqlOf(PropertyCondition.inList('name', ['Bericht']))

        then: "IN accepts only a function invocation as its operand, which takes the parser seconds at this size"
        !jpql.contains("function('coalesce'")
        !jpql.contains(' in ')

        and: "the search starts with the literal, not with locate: inside the parentheses a query with a where " +
                "clause puts around the condition, a numeric function first is read as arithmetic and re-parsed"
        jpql.replaceAll(/\s+/, '').contains('0<locate(')
    }

    def "IN_LIST matches a whole text only"() {
        expect: "a part of a text, a text that contains it and a text in another case are not the text"
        codes(PropertyCondition.inList('name', ['Beric', 'richt', 'Plain', 'literal'])) == []
        codes(PropertyCondition.inList('name', ['Bericht und mehr'])) == []
        codes(PropertyCondition.inList('name', ['bericht'])) == []

        and: "nor is the text of another locale"
        codes(PropertyCondition.inList('name', ['Report', 'Draft'])) == []
    }

    def "a listed value that can match nothing is left out of the search"() {
        expect: "a resolved text never holds a line break, and never is null when it is compared"
        codes(PropertyCondition.inList('name', ['Bericht\nPlain literal'])) == []
        codes(PropertyCondition.inList('name', ['x\nBericht', 'Plain literal'])) == ['2']
        codes(PropertyCondition.inList('name', [null, 'Bericht'])) == ['1']
        codes(PropertyCondition.createWithValue('name', PropertyCondition.Operation.NOT_IN_LIST, ['x\nBericht'])) ==
                ['1', '2', '3', '4']
    }

    def "an empty list matches nothing for IN_LIST and everything for NOT_IN_LIST"() {
        given: "an empty list is kept on purpose: by default actualization drops such a condition from the query"
        def inEmpty = PropertyCondition.inList('name', [])
        inEmpty.skipNullOrEmpty = false
        def notInEmpty = PropertyCondition.createWithValue('name', PropertyCondition.Operation.NOT_IN_LIST, [])
        notInEmpty.skipNullOrEmpty = false

        expect:
        codes(inEmpty) == []
        codes(notInEmpty) == ['1', '2', '3', '4']
    }

    def "IS_SET checks the column itself"() {
        given:
        def empty = dataManager.create(TestLocalizedNameEntity)
        empty.code = '5'
        dataManager.save(empty)

        expect:
        codes(PropertyCondition.isSet('name', true)) == ['1', '2', '3', '4']
        codes(PropertyCondition.isSet('name', false)) == ['5']
    }

    def "a condition through a reference path uses the join alias"() {
        given:
        def holder = dataManager.create(TestLocalizedNameHolder)
        holder.item = dataManager.load(TestLocalizedNameEntity).condition(PropertyCondition.equal('code', '1')).one()
        dataManager.save(holder)

        when:
        def holders = dataManager.load(TestLocalizedNameHolder)
                .condition(PropertyCondition.equal('item.name', 'Bericht'))
                .list()

        then:
        holders*.id == [holder.id]
    }

    def "a condition through a to-many collection path is generated as a subquery"() {
        given:
        def group = dataManager.create(TestLocalizedNameGroup)
        group.code = 'g1'
        group.items = [dataManager.load(TestLocalizedNameEntity).condition(PropertyCondition.equal('code', '1')).one()]
        def other = dataManager.create(TestLocalizedNameGroup)
        other.code = 'g2'
        other.items = [dataManager.load(TestLocalizedNameEntity).condition(PropertyCondition.equal('code', '2')).one()]
        def empty = dataManager.create(TestLocalizedNameGroup)
        empty.code = 'g3'
        dataManager.save(group, other, empty)

        when:
        def groups = dataManager.load(TestLocalizedNameGroup)
                .condition(PropertyCondition.equal('items.name', 'Bericht'))
                .list()

        then:
        groups*.code == ['g1']

        when: "an unset value also matches a group whose collection is empty, as it does for a plain property"
        def unset = dataManager.load(TestLocalizedNameGroup)
                .condition(PropertyCondition.isSet('items.name', false))
                .list()

        then:
        unset*.code == ['g3']
    }

    def "a value with no default value is not read as if its first line were the default value"() {
        given: "the value carries no default value and was stored without the leading line break"
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '6'
        entity.name = 'de=Bericht\nfr=Rapport'
        dataManager.save(entity)

        expect: "a de user reads the entry"
        codes(PropertyCondition.equal('name', 'Bericht')) == ['1', '6']

        when: "an en user has no entry of their own"
        authenticateWithLocale(Locale.ENGLISH)

        then: "the text is empty, the way LocalizedStringSupport reads it, not the first line"
        codes(PropertyCondition.equal('name', 'de=Bericht')) == []
        codes(PropertyCondition.equal('name', '')) == ['6']
    }

    def "an entry written with the language alone is recognised on the first line too"() {
        given: "'pt' is the language of the available locale pt_BR and the second step of the resolution chain"
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '7'
        entity.name = 'pt=Relatório'
        dataManager.save(entity)

        when:
        authenticateWithLocale(Locale.ENGLISH)

        then:
        codes(PropertyCondition.equal('name', 'pt=Relatório')) == []
        codes(PropertyCondition.equal('name', '')) == ['7']

        when: "a pt_BR user falls back to the language entry"
        authenticateWithLocale(Locale.forLanguageTag('pt-BR'))

        then:
        codes(PropertyCondition.equal('name', 'Relatório')) == ['7']
    }

    def "a case-insensitive search finds an entry whose key carries a region"() {
        given: "a mixed-case key, looked for in the column as stored before the resolved text is lower-cased"
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '8'
        entity.name = 'Report\npt_BR=Relatório mensal'
        dataManager.save(entity)

        when:
        authenticateWithLocale(Locale.forLanguageTag('pt-BR'))

        then:
        codes(PropertyCondition.contains('name', 'MENSAL')) == ['8']
        codes(PropertyCondition.startsWith('name', 'relatório')) == ['8']
    }

    def "a locale that cannot open an entry does not break the query"() {
        when: "the key carries a quote, which no entry key may contain"
        authenticateWithLocale(new Locale('en', 'US', "o'brien"))

        then:
        codes(PropertyCondition.equal('name', 'Plain literal')) == ['2']
        codes(PropertyCondition.contains('name', 'plain')) == ['2']
    }

    def "a @Lob localized property is compared as the stored string"() {
        given:
        def entity = dataManager.create(TestLocalizedNameEntity)
        entity.code = '9'
        entity.name = 'x'
        entity.description = 'Desc\nde=Beschreibung'
        dataManager.save(entity)

        expect: "the localized generator declines @Lob properties, so the marker text of the stored form matches"
        codes(PropertyCondition.contains('description', 'de=')) == ['9']
    }

    def "localized and plain conditions combine in one logical condition"() {
        expect: "the stored string of item 1 is not 'Bericht', so compared as stored it would match; and each " +
                "condition rejects a row the other one accepts"
        codes(LogicalCondition.and(
                PropertyCondition.notEqual('name', 'Bericht'),
                PropertyCondition.notEqual('code', '3'))) == ['2', '4']
    }

    def "operations that make no sense for a string are refused with a clear message"() {
        when:
        dataManager.load(TestLocalizedNameEntity)
                .condition(PropertyCondition.createWithValue('name', operation, value))
                .list()

        then:
        def e = thrown(UnsupportedOperationException)
        e.message.contains("'$operation'") && e.message.contains("'name'")

        where:
        operation                                          | value
        PropertyCondition.Operation.DATE_EQUALS            | new Date()
        PropertyCondition.Operation.IS_COLLECTION_EMPTY    | true
    }
}
