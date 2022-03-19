/*
 * Copyright 2020 Haulmont.
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

package query_conditions

import io.jmix.core.CoreConfiguration
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.ConditionXmlLoader
import io.jmix.core.querycondition.JpqlCondition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.data.DataConfiguration
import io.jmix.data.impl.jpql.generator.ConditionGenerationContext
import io.jmix.data.impl.jpql.generator.ConditionJpqlGenerator
import io.jmix.eclipselink.EclipselinkConfiguration
import org.dom4j.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestDataConfiguration

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration, EclipselinkConfiguration, TestDataConfiguration])
class QueryConditionsTest extends Specification {

    @Autowired
    ConditionXmlLoader xmlSerializer
    @Autowired
    ConditionJpqlGenerator jpqlGenerator

    def "JPQL conditions"() {
        String xml = '''
            <and>
                <jpql>
                    <where>u.login like :login</where>
                </jpql>
                <jpql>
                    <join>u.userRoles ur</join>
                    <where>ur.role.name = :roleName</where>
                </jpql>
            </and>
'''
        when:

        Condition condition = xmlSerializer.fromXml(xml)

        then:

        condition instanceof LogicalCondition
        ((LogicalCondition) condition).type == LogicalCondition.Type.AND
        JpqlCondition c1 = ((LogicalCondition) condition).conditions[0]
        c1.getWhere() == 'u.login like :login'
        c1.parameters == ['login']

        JpqlCondition c2 = ((LogicalCondition) condition).conditions[1]
        c2.getJoin() == 'u.userRoles ur'
        c2.getWhere() == 'ur.role.name = :roleName'
        c2.parameters == ['roleName']

        condition.getParameters().toSet() == ['login', 'roleName'].toSet()
    }

    def "some REST conditions"() {
        String xml = '''
            <and>
                <rest>
                    <param>login=${login}</param>
                </rest>
                <rest>
                    <param>role.name=${roleName}</param>
                </rest>
            </and>
'''
        xmlSerializer.addFactory('rest', { Element element ->
            if (element.name == 'rest') {
                return SampleRestCondition.create(element.element('param').text)
            }
            return null
        })

        when:

        Condition condition = xmlSerializer.fromXml(xml)

        then:

        condition instanceof LogicalCondition
        ((LogicalCondition) condition).type == LogicalCondition.Type.AND
        SampleRestCondition c1 = ((LogicalCondition) condition).conditions[0]
        c1.getParam() == 'login=${login}'
        c1.parameters == ['login']

        SampleRestCondition c2 = ((LogicalCondition) condition).conditions[1]
        c2.getParam() == 'role.name=${roleName}'
        c2.parameters == ['roleName']

        condition.getParameters().toSet() == ['login', 'roleName'].toSet()

        cleanup:

        xmlSerializer.removeFactory('rest')
    }

    def "condition actualization"() {
        String xml = '''
            <and>
                <jpql>
                    <where>u.login like :login</where>
                </jpql>
                <jpql>
                    <join>join u.userRoles ur</join>
                    <where>ur.role.name = :roleName</where>
                </jpql>
                <or>
                    <jpql>
                        <join>, test$Foo f</join>
                        <where>f.foo = :foo</where>
                    </jpql>
                    <jpql>
                        <where>u.bar = :bar</where>
                    </jpql>
                </or>
            </and>
'''
        when:

        Condition condition = xmlSerializer.fromXml(xml)
        Condition actualized = condition.actualize(['login', 'roleName', 'foo', 'bar'].toSet())
        String query = jpqlGenerator.processQuery('select u from test$User u', new ConditionGenerationContext(actualized))

        then:

        actualized instanceof LogicalCondition
        ((LogicalCondition) actualized).type == LogicalCondition.Type.AND
        ((LogicalCondition) actualized).conditions.size() == 3

        JpqlCondition c1 = ((LogicalCondition) actualized).conditions[0]
        c1.getWhere() == 'u.login like :login'

        JpqlCondition c2 = ((LogicalCondition) actualized).conditions[1]
        c2.getWhere() == 'ur.role.name = :roleName'

        LogicalCondition or = ((LogicalCondition) actualized).conditions[2]
        or.type == LogicalCondition.Type.OR
        or.conditions.size() == 2

        JpqlCondition c3 = or.conditions[0]
        c3.getWhere() == 'f.foo = :foo'

        JpqlCondition c4 = or.conditions[1]
        c4.getWhere() == 'u.bar = :bar'

        query == 'select u from test$User u join u.userRoles ur, test$Foo f ' +
                'where (u.login like :login and ur.role.name = :roleName and (f.foo = :foo or u.bar = :bar))'

        when:

        actualized = condition.actualize(['login', 'roleName', 'foo'].toSet())
        query = jpqlGenerator.processQuery('select u from test$User u', new ConditionGenerationContext(actualized))

        then:

        actualized instanceof LogicalCondition
        ((LogicalCondition) actualized).type == LogicalCondition.Type.AND
        ((LogicalCondition) actualized).conditions.size() == 3

        JpqlCondition c11 = ((LogicalCondition) actualized).conditions[0]
        c11.getWhere() == 'u.login like :login'

        JpqlCondition c21 = ((LogicalCondition) actualized).conditions[1]
        c21.getWhere() == 'ur.role.name = :roleName'

        JpqlCondition c31 = ((LogicalCondition) actualized).conditions[2]
        c31.getWhere() == 'f.foo = :foo'

        query == 'select u from test$User u join u.userRoles ur, test$Foo f ' +
                'where (u.login like :login and ur.role.name = :roleName and f.foo = :foo)'

        when:

        actualized = condition.actualize(['login', 'roleName'].toSet())
        query = jpqlGenerator.processQuery('select u from test$User u', new ConditionGenerationContext(actualized))

        then:

        actualized instanceof LogicalCondition
        ((LogicalCondition) actualized).type == LogicalCondition.Type.AND
        ((LogicalCondition) actualized).conditions.size() == 2

        JpqlCondition c12 = ((LogicalCondition) actualized).conditions[0]
        c12.getWhere() == 'u.login like :login'

        JpqlCondition c22 = ((LogicalCondition) actualized).conditions[1]
        c22.getWhere() == 'ur.role.name = :roleName'

        query == 'select u from test$User u join u.userRoles ur ' +
                'where (u.login like :login and ur.role.name = :roleName)'

        when:

        actualized = condition.actualize(['roleName'].toSet())
        query = jpqlGenerator.processQuery('select u from test$User u', new ConditionGenerationContext(actualized))

        then:

        actualized instanceof JpqlCondition
        ((JpqlCondition) actualized).getWhere() == 'ur.role.name = :roleName'

        query == 'select u from test$User u join u.userRoles ur where ur.role.name = :roleName'

        when:

        actualized = condition.actualize(Collections.emptySet())
        query = jpqlGenerator.processQuery('select u from test$User u', new ConditionGenerationContext(actualized))

        then:

        actualized == null

        query == 'select u from test$User u'
    }
}
