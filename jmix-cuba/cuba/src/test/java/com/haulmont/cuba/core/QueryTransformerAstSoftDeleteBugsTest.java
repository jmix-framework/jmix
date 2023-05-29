/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core;

import io.jmix.data.impl.jpql.DomainModel;
import io.jmix.data.impl.jpql.model.EntityBuilder;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.data.impl.jpql.transform.QueryTransformerAstBased;
import junit.framework.TestCase;
import org.antlr.runtime.RecognitionException;

public class QueryTransformerAstSoftDeleteBugsTest extends TestCase {

    // #PL-1998 (fixed)
    public void testAddWhereDeleteTs() throws Exception {
        DomainModel model = createDomainModel();
        QueryTransformerAstBased transformer = createTransformer("select u from sec$User u where u.active = true or u.active = true");
        transformer.addWhere("{E}.deleteTs is null");
        String res = transformer.getResult();
        assertEquals(
                "select u from sec$User u where (u.active = true or u.active = true) and (u.deleteTs is null)",
                res);

        transformer = createTransformer("select u from sec$User u where u.active = true");
        transformer.addWhere("{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u where (u.active = true) and (u.deleteTs is null)",
                res);

        transformer = createTransformer(
                "select u from sec$User u where (u.active = true)");
        transformer.addWhere("{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u where ((u.active = true)) and (u.deleteTs is null)",
                res);

        transformer = createTransformer(
                "select u from sec$User u where u.active = true and u.active = true");
        transformer.addWhere("{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u where (u.active = true and u.active = true) and (u.deleteTs is null)",
                res);

        transformer = createTransformer(
                "select u from sec$User u");
        transformer.addWhere("{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u where u.deleteTs is null",
                res);

        // addJoinAndWhere

        transformer = createTransformer(
                "select u from sec$User u where u.active = true or u.active = true");
        transformer.addJoinAndWhere("join u.group g", "{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u join u.group g where (u.active = true or u.active = true) and (u.deleteTs is null)",
                res);

        transformer = createTransformer(
                "select u from sec$User u where (u.active = true)");
        transformer.addJoinAndWhere("join u.group g", "{E}.deleteTs is null");
        res = transformer.getResult();
        assertEquals(
                "select u from sec$User u join u.group g where ((u.active = true)) and (u.deleteTs is null)",
                res);
    }

    private QueryTransformerAstBased createTransformer(String query) throws RecognitionException {
        return new QueryTransformerAstBased(createDomainModel(),
                query);
    }

    private DomainModel createDomainModel() {
        EntityBuilder builder = EntityBuilder.create();
        JpqlEntityModel debtorEntity = builder.produceImmediately("dn$Debtor", "id", "id");
        JpqlEntityModel agentEntity = builder.produceImmediately("dn$Agent", "id", "id");
        JpqlEntityModel userEntity = builder.produceImmediately("sec$User", "active", "deleteTs");
        JpqlEntityModel agentAllocation = builder.produceImmediately("dn$AgentAllocation", "agent", "debtor");
        JpqlEntityModel scheduleOperation = builder.produceImmediately("dn$ScheduleOperation");
        JpqlEntityModel bailiffDaySchedule = builder.produceImmediately("dn$BailiffDaySchedule", "day");
        scheduleOperation.addReferenceAttribute("dn$BailiffDaySchedule", "bailiffDaySchedule", "bailiffDaySchedule", false);
        scheduleOperation.addReferenceAttribute("dn$Debtor", "debtor", "debtor", false);

        return new DomainModel(userEntity, agentAllocation, scheduleOperation, bailiffDaySchedule, debtorEntity);
    }
}