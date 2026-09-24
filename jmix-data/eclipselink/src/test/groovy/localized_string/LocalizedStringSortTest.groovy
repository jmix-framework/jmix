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
import io.jmix.core.Sort
import io.jmix.data.impl.JpqlQueryBuilder
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestLocalizedNameEntity
import test_support.entity.TestLocalizedNameHolder

class LocalizedStringSortTest extends DataSpec {

    static final Locale DE = Locale.GERMAN

    @Autowired
    DataManager dataManager
    @Autowired
    BeanFactory beanFactory

    TestLocalizedNameEntity item(String code, String name) {
        def e = dataManager.create(TestLocalizedNameEntity)
        e.code = code
        e.name = name
        e.description = name
        return e
    }

    def "the sort expression resolves the current locale with fallback and keeps the id tiebreaker"() {
        given:
        authenticateWithLocale(DE)
        def queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_LocalizedNameEntity e')
                .setSort(Sort.by('name'))
                .setEntityName('test_LocalizedNameEntity')

        when: "the transformer re-prints the expression with its own spacing, so the checks ignore whitespace"
        def jpql = queryBuilder.getResultQueryString()
        def compact = jpql.replaceAll(/\s+/, '')

        then: "one coalesce over: de entry, en entry (application default locale), first line; the line break comes " +
                "from char(10). It is the built-in coalesce wrapped in concat(..., ''), which the parser reads in a " +
                "fraction of the time it takes over a bare coalesce or over function('coalesce', ...)"
        compact.startsWith("selectefromtest_LocalizedNameEntityeorderbyconcat(coalesce(")
        compact.count("nullif(trim(leadingfromsubstring(") == 2
        compact.contains("concat(function('char',10),'de=')")
        compact.contains("concat(function('char',10),'en=')")
        compact.contains("substring(concat(e.name,function('char',10)),1," +
                "locate(function('char',10),concat(e.name,function('char',10)))-1)")

        and: "an entry carries no case of its own: the padded column ends with an empty entry of the key, so the " +
                "marker is always found. A case in every entry is what kept the parser backtracking for seconds"
        !compact.contains("nullif(casewhen")
        compact.contains("concat(function('char',10),e.name,function('char',10),'de=',function('char',10))")

        and: "the first line is not taken as the default value when it opens an entry of an available locale, " +
                "the key compared in its canonical case, the only one the parser reads an entry in"
        compact.contains("casewhenlocate('en=',e.name)=1orlocate('de=',e.name)=1" +
                "orlocate('pt_BR=',e.name)=1orlocate('pt=',e.name)=1then''else")
        !jpql.contains('\n')
        jpql.endsWith(' asc, e.id asc')
    }

    def "rows are ordered by the text shown to the user, not by the stored string"() {
        given: "stored strings sort as z.., a.., m.. but the German texts sort as Berta, Dora, Anton"
        dataManager.save(
                item('1', 'zeta\nen=Zeta\nde=Berta'),
                item('2', 'alpha\nen=Alpha\nde=Dora'),
                item('3', 'mu\nen=Mu\nde=Anton'))
        authenticateWithLocale(DE)

        when:
        def asc = dataManager.load(TestLocalizedNameEntity).all().sort(Sort.by(Sort.Order.asc('name'))).list()
        def desc = dataManager.load(TestLocalizedNameEntity).all().sort(Sort.by(Sort.Order.desc('name'))).list()

        then:
        asc*.code == ['3', '1', '2']
        desc*.code == ['2', '1', '3']
    }

    def "a missing or empty entry falls back to the application default locale and then to the first line"() {
        given:
        dataManager.save(
                item('1', 'C default\nen=B English'),
                item('2', 'A default\nde='),
                item('3', 'Plain literal'),
                item('4', '\nen=D only english'))
        authenticateWithLocale(DE)

        when: "de user: 1 -> 'B English', 2 -> 'A default', 3 -> 'Plain literal', 4 -> 'D only english'"
        def list = dataManager.load(TestLocalizedNameEntity).all().sort(Sort.by(Sort.Order.asc('name'))).list()

        then:
        list*.code == ['2', '1', '4', '3']
    }

    def "sorting works through a reference path"() {
        given:
        def first = item('1', 'x\nde=Berta')
        def second = item('2', 'y\nde=Anton')
        def h1 = dataManager.create(TestLocalizedNameHolder)
        h1.item = first
        def h2 = dataManager.create(TestLocalizedNameHolder)
        h2.item = second
        dataManager.save(first, second, h1, h2)
        authenticateWithLocale(DE)

        when:
        def holders = dataManager.load(TestLocalizedNameHolder).all().sort(Sort.by(Sort.Order.asc('item.name'))).list()

        then:
        holders*.id == [h2.id, h1.id]
    }

    def "a @Lob localized property keeps the standard sort expression"() {
        given:
        def queryBuilder = beanFactory.getBean(JpqlQueryBuilder)
        queryBuilder.setQueryString('select e from test_LocalizedNameEntity e')
                .setSort(Sort.by('description'))
                .setEntityName('test_LocalizedNameEntity')

        expect:
        queryBuilder.getResultQueryString() == 'select e from test_LocalizedNameEntity e order by e.description asc, e.id asc'
    }
}
