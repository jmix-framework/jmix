/*
 * Copyright 2019 Haulmont.
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

package jpql_transform

import io.jmix.data.impl.jpql.DomainModel
import io.jmix.data.impl.jpql.model.EntityBuilder
import io.jmix.data.impl.jpql.transform.QueryTransformerAstBased
import spock.lang.Specification

class JpqlQueryTransformationTest extends Specification {

    DomainModel domainModel

    void setup() {
        def user = EntityBuilder.create()
                .startNewEntity('sec_User')
                .addStringAttribute("login")
                .addSingleValueAttribute(Long.class, "version")
                .produce()


        def groupHierarchy = EntityBuilder.create()
                .startNewEntity('sec$GroupHierarchy')
                .addStringAttribute('group')
                .addStringAttribute("createdBy")
                .addReferenceAttribute("parent", 'sec$GroupHierarchy')
                .addCollectionReferenceAttribute("constraints", 'sec$Constraint')
                .produce()


        def constraint = EntityBuilder.create()
                .startNewEntity('sec$Constraint')
                .addReferenceAttribute("group", 'sec$GroupHierarchy')
                .produce()

        domainModel = new DomainModel(user, groupHierarchy, constraint)
    }


    def "transform case insensitive parameter"() {

        when: "with JPQL function and string arguments"

        def transformer = new QueryTransformerAstBased(domainModel, "select u from sec_User u where concat(u.name, ' ', u.login) = :name")
        transformer.handleCaseInsensitiveParam("name")

        def result = transformer.getResult()

        then:

        result == "select u from sec_User u where concat( lower ( u.name), ' ', lower ( u.login)) = :name"

        when: "with JPQL function and number arguments"

        transformer = new QueryTransformerAstBased(domainModel, "select u from sec_User u where concat(u.name, ' ', u.version) = :name")
        transformer.handleCaseInsensitiveParam("name")

        result = transformer.getResult()

        then:

        result == "select u from sec_User u where concat( lower ( u.name), ' ', u.version) = :name"
    }

    def "replace is null and not null statements"() {

        when: "is null statement and null parameter value"

        def transformer = new QueryTransformerAstBased(domainModel, 'select c from sec$GroupHierarchy h join h.parent.constraints c where :par1 is null')

        transformer.replaceIsNullStatements("par1", true)
        def result = transformer.getResult()

        then:
        result == 'select c from sec$GroupHierarchy h join h.parent.constraints c where 1=1'

        when: "is null statement and not null parameter value"

        transformer = new QueryTransformerAstBased(domainModel, 'select c from sec$GroupHierarchy h join h.parent.constraints c where :par1 is null')

        transformer.replaceIsNullStatements("par1", false)
        result = transformer.getResult()

        then:
        result == 'select c from sec$GroupHierarchy h join h.parent.constraints c where 1=0'

        when: "is not null statement and several parameters"

        transformer = new QueryTransformerAstBased(domainModel, 'select c from sec$GroupHierarchy h join h.parent.constraints c where :par1 is not null and :par2 is not null')

        transformer.replaceIsNullStatements("par1", false)
        transformer.replaceIsNullStatements("par2", true)
        result = transformer.getResult()

        then:
        result == 'select c from sec$GroupHierarchy h join h.parent.constraints c where 1=1 and 1=0'

        when: "witout is not null and is null statements"

        transformer = new QueryTransformerAstBased(domainModel, 'select c from sec$GroupHierarchy h join h.parent.constraints c where 2 = 2')

        transformer.replaceIsNullStatements("par1", true)
        result = transformer.getResult()

        then:
        result == 'select c from sec$GroupHierarchy h join h.parent.constraints c where 2 = 2'
    }
}
