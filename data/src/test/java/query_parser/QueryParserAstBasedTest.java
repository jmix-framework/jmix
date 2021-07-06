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

package query_parser;

import io.jmix.data.impl.jpql.DomainModel;
import io.jmix.data.impl.jpql.JpqlSyntaxException;
import io.jmix.data.impl.jpql.QueryParserAstBased;
import io.jmix.data.impl.jpql.model.EntityBuilder;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.data.impl.jpql.transform.QueryTransformerAstBased;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class QueryParserAstBasedTest {

    @Test
    public void testPathVariable() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select c from sec_GroupHierarchy h join h.parent.constraints c where h.group.id = ?1"
        );
        assertEquals("sec_GroupHierarchy", parser.getEntityName());
        assertEquals("h", parser.getEntityAlias());

    }

    @Test
    public void testMainEntity() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select h from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertEquals("sec_GroupHierarchy", parser.getEntityName());
        assertEquals("h", parser.getEntityAlias());

        parser = new QueryParserAstBased(model,
                "select h.group from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertEquals("sec_GroupHierarchy", parser.getEntityName());
        assertEquals("h", parser.getEntityAlias());

        parser = new QueryParserAstBased(model,
                "select u from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertEquals("sec_Constraint", parser.getEntityName());
        assertEquals("u", parser.getEntityAlias());
    }

    @Test
    public void testError() throws Exception {
        DomainModel model = prepareDomainModel();
        try {
            QueryParserAstBased parser = new QueryParserAstBased(model,
                    "select u from sec_Constraint"
            );
            parser.getEntityAlias();
            fail();
        } catch (JpqlSyntaxException e) {
            //OK
        }

        try {
            QueryParserAstBased parser = new QueryParserAstBased(model,
                    "select u from sec_GroupHierarchy where u.createdBy = 'createdBy'"
            );
            parser.getEntityAlias();
            fail();
        } catch (JpqlSyntaxException e) {
            //OK
        }

        try {
            QueryParserAstBased parser = new QueryParserAstBased(model,
                    "select u from sec_GroupHierarchy u where u.createdBy != 'createdBy'"
            );
            parser.getEntityAlias();
            fail();
        } catch (JpqlSyntaxException e) {
            //OK
        }
    }

    @Test
    public void testGetParamNames() throws Exception {
        QueryParserAstBased parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        Set<String> paramNames = parser.getParamNames();
        assertEquals(1, paramNames.size());
        assertTrue(paramNames.contains("par"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u, sec_GroupHierarchy h " +
                        "where " +
                        "h.userGroup = :par and " +
                        "h.createdBy like :par2 and " +
                        "h.parent <> :par3"
        );

        paramNames = parser.getParamNames();
        assertEquals(3, paramNames.size());
        assertTrue(paramNames.contains("par"));
        assertTrue(paramNames.contains("par2"));
        assertTrue(paramNames.contains("par3"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select c from sec_Constraint c join sec_Group g on g.name = :par");
        paramNames = parser.getParamNames();
        assertEquals(1, paramNames.size());
        assertTrue(paramNames.contains("par"));
    }

    @Test
    public void testErrorsInJoin() {
        try {
            QueryParserAstBased parser = new QueryParserAstBased(prepareDomainModel(),
                    "select h, sum(u.int1) from sec_User u join u.group g join g.hierarchy hwhere u.constraint.id = :storeSelect group by h");
            parser.getEntityName();
            fail();
        } catch (JpqlSyntaxException e) {
            //Do nothing
        }

        try {
            QueryParserAstBased parser = new QueryParserAstBased(prepareDomainModel(),
                    "select u from sec_User u join fetch u.group g");
            parser.getEntityName();
            fail();
        } catch (JpqlSyntaxException e) {
            //Do nothing
        }
    }

    @Test
    public void testUsedEntityNames() throws Exception {
        QueryParserAstBased parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u"
        );
        Set<String> entityNames = parser.getAllEntityNames();
        assertEquals(1, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u where u.group = :param"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u, sec_GroupHierarchy h"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u, sec_GroupHierarchy h where u.group = h"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select u from sec_Constraint u, sec_GroupHierarchy h where h.group.id = :par and u.group = h"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(3, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));
        assertTrue(entityNames.contains("sec_Group"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select c from sec_Constraint c join c.group g"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));


        parser = new QueryParserAstBased(prepareDomainModel(),
                "select c from sec_Constraint c join sec_Group g on c.group.group = g"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(3, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_GroupHierarchy"));
        assertTrue(entityNames.contains("sec_Group"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select c from sec_Constraint c join sec_Group g on g.name = :par"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_Group"));

        parser = new QueryParserAstBased(prepareDomainModel(),
                "select c from sec_Constraint c join sec_Group g"
        );
        entityNames = parser.getAllEntityNames();
        assertEquals(2, entityNames.size());
        assertTrue(entityNames.contains("sec_Constraint"));
        assertTrue(entityNames.contains("sec_Group"));
    }

    @Test
    public void testEntityAlias() throws Exception {
        QueryParserAstBased parser = new QueryParserAstBased(prepareDomainModel(),
                "select h from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertEquals("h", parser.getEntityAlias());
        assertEquals("u", parser.getEntityAlias("sec_Constraint"));
    }

    @Test
    public void testIsEntitySelect() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select h from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertTrue(parser.isEntitySelect("sec_GroupHierarchy"));

        parser = new QueryParserAstBased(model,
                "select h.createdBy, h.parent from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );

        assertFalse(parser.isEntitySelect("sec_GroupHierarchy"));
    }

    @Test
    public void testHasIsNullCondition() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model, "select c from ref_Car c");
        assertFalse(parser.hasIsNullCondition("colour"));

        parser = new QueryParserAstBased(model, "select c from ref_Car c where c.colour = ?1");
        assertFalse(parser.hasIsNullCondition("colour"));

        parser = new QueryParserAstBased(model, "select c from ref_Car c where c.colour is null");
        assertTrue(parser.hasIsNullCondition("colour"));

        parser = new QueryParserAstBased(model, "select c from ref_Car c where c.model.manufacturer is null");
        assertTrue(parser.hasIsNullCondition("model.manufacturer"));

        parser = new QueryParserAstBased(model, "select c from ref_Car c where c.model = (select a from ref_Other a where a.model is null)");
        assertFalse(parser.hasIsNullCondition("model"));
    }

    @Test
    public void testNewKeyword() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select r from sec$Role r where r.type = @enum(com.haulmont.cuba.security.entity.RoleType.NEW)");
        parser.getEntityName();
    }

    @Test
    public void testEnumMacro() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select r from sec_Role r where r.type = @enum(com.haulmont.cuba.security.entity.RoleType.STANDARD)");
        parser.getEntityName();

        parser = new QueryParserAstBased(model,
                "select r from sec_Role r where (select r1.type from sec_Role r1 where r1.id = r.id) = @enum(com.haulmont.cuba.security.entity.RoleType.STANDARD)");
        parser.getEntityName();

        parser = new QueryParserAstBased(model,
                "select r from sec_Role r where r.type in (@enum(com.haulmont.cuba.security.entity.RoleType.STANDARD), @enum(com.haulmont.cuba.security.entity.RoleType.SUPER))");
        parser.getEntityName();
    }

    @Test
    public void testGetNestedEntityNameIfNestedSelected() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select h.group from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );
        assertEquals("sec_Group", parser.getOriginalEntityName());
        assertEquals("h.group", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select g.group from sec_GroupHierarchy h join h.group g"
        );
        assertNull(parser.getOriginalEntityName());
        assertNull(parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select g from sec_GroupHierarchy h join h.group g"
        );
        assertNotNull(parser.getOriginalEntityName());
        assertNotNull(parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select h.parent.other from sec_GroupHierarchy h where h.userGroup = :par"
        );
        assertEquals("sec_GroupHierarchy", parser.getOriginalEntityName());
        assertEquals("h.parent.other", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select h.parent.other.group from sec_GroupHierarchy h where h.userGroup = :par"
        );
        assertEquals("sec_Group", parser.getOriginalEntityName());
        assertEquals("h.parent.other.group", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select g from sec_GroupHierarchy h join h.group g where h.userGroup = :par"
        );
        assertEquals("sec_Group", parser.getOriginalEntityName());
        assertEquals("g", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select p from sec_GroupHierarchy h join h.parent p where h.userGroup = :par"
        );
        assertEquals("sec_GroupHierarchy", parser.getOriginalEntityName());
        assertEquals("p", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select h from sec_Constraint u, sec_GroupHierarchy h where h.userGroup = :par"
        );
        assertNull(parser.getOriginalEntityName());
        assertNull(parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select h.parent.other.createdBy from sec_GroupHierarchy h where h.userGroup = :par"
        );
        assertNull(parser.getOriginalEntityName());
        assertNull(parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select c from sec_GroupHierarchy h, sec_Constraint c where h.userGroup = :par"
        );
        assertNull(parser.getOriginalEntityName());
        assertNull(parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select c.group from sec_GroupHierarchy h, sec_Constraint c where h.userGroup = :par"
        );
        assertEquals("sec_GroupHierarchy", parser.getOriginalEntityName());
        assertEquals("c.group", parser.getOriginalEntityPath());

        parser = new QueryParserAstBased(model,
                "select u.group, u.login from sec_User u where u.name like :mask"
        );
        assertNull(parser.getOriginalEntityName());
        assertNull(parser.getOriginalEntityPath());
    }

    @Test
    public void testNestedEntityGroupBy() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryTransformerAstBased transformer = new QueryTransformerAstBased(model,
                "select c.group, count(c.id) from sec_Constraint c group by c.group"
        );
        transformer.replaceWithSelectEntityVariable("tempEntityAlias");
        transformer.addFirstSelectionSource(String.format("%s tempEntityAlias", "sec_Group"));
        transformer.addWhereAsIs(String.format("tempEntityAlias.id = %s.id", "c.group"));
        transformer.addEntityInGroupBy("tempEntityAlias");
        System.out.println(transformer.getResult());
    }

    @Test
    public void testHasJoins() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select h.group from sec_Constraint u, sec_GroupHierarchy h"
        );
        assertTrue(parser.isQueryWithJoins());

        parser = new QueryParserAstBased(model,
                "select g.group from sec_GroupHierarchy h join h.group g"
        );
        assertTrue(parser.isQueryWithJoins());

        parser = new QueryParserAstBased(model,
                "select h.parent.other from sec_GroupHierarchy h"
        );
        assertFalse(parser.isQueryWithJoins());
    }

    @Test
    public void testScalarExpressionInSelect() throws Exception {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased transformer = new QueryParserAstBased(model,
                "select c.int1 + c.int2 * c.int1 from sec_User u"
        );
        transformer.getParamNames();
    }

    @Test
    public void testSameAliasSeveralTimes() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select g.group from sec_GroupHierarchy h join h.group g join h.group g");
        try {
            parser.getEntityAlias();
            fail();
        } catch (JpqlSyntaxException e) {
            //success
        }
    }

    @Test
    public void testPathVariableInSubQuery() {
        DomainModel model = prepareDomainModel();
        QueryParserAstBased parser = new QueryParserAstBased(model,
                "select h from sec_GroupHierarchy h where exists(select c from h.constraints as c)");
        parser.getEntityAlias();

        parser = new QueryParserAstBased(model,
                "select h from sec_GroupHierarchy h where exists(select c from h.constraints c)");
        parser.getEntityAlias();
    }

    private DomainModel prepareDomainModel() {
        EntityBuilder builder = EntityBuilder.create();
        builder.startNewEntity("sec_GroupHierarchy");
        builder.addReferenceAttribute("group", "sec_Group");
        builder.addReferenceAttribute("as", "sec_Group");
        builder.addStringAttribute("createdBy");
        builder.addReferenceAttribute("parent", "sec_GroupHierarchy");
        builder.addReferenceAttribute("other", "sec_GroupHierarchy");
        builder.addCollectionReferenceAttribute("constraints", "sec_Constraint");
        JpqlEntityModel groupHierarchy = builder.produce();

        builder = EntityBuilder.create();
        builder.startNewEntity("sec_Constraint");
        builder.addReferenceAttribute("group", "sec_GroupHierarchy");
        JpqlEntityModel constraintEntity = builder.produce();


        JpqlEntityModel groupEntity = builder.produceImmediately("sec_Group", "name", "group");

        builder = EntityBuilder.create();
        builder.startNewEntity("sec_User");
        builder.addStringAttribute("login");
        builder.addSingleValueAttribute(Integer.class,"int1");
        builder.addSingleValueAttribute(Integer.class,"int2");
        builder.addReferenceAttribute("group", "sec_Group");
        JpqlEntityModel userEntity = builder.produce();

        return new DomainModel(groupHierarchy, constraintEntity, userEntity, groupEntity);
    }
}