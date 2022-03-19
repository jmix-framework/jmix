/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.core.data_manager

import com.haulmont.cuba.core.model.beanvalidation.EmbeddedValidatedEntity
import com.haulmont.cuba.core.model.beanvalidation.ValidatedEntity
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import io.jmix.core.validation.EntityValidationException
import com.haulmont.cuba.core.Persistence
import org.springframework.jdbc.core.JdbcTemplate
import spec.haulmont.cuba.core.CoreTestSpecification

import org.springframework.beans.factory.annotation.Autowired

class EntityValidationTest extends CoreTestSpecification {
    @Autowired
    private Persistence persistence
    @Autowired
    private DataManager dataManager

    void setup() {
        persistence.createTransaction().commit()
    }


    void cleanup() {
        def jdbcTemplate = new JdbcTemplate(persistence.dataSource)
        jdbcTemplate.update('delete from TEST_VALIDATED_ENTITY')
    }

    def "ALWAYS_VALIDATE test"() {
        when:
        def context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.ALWAYS_VALIDATE)
        def validatedEntity = new ValidatedEntity(name: "1")
        context.addInstanceToCommit(validatedEntity)
        dataManager.commit(context)

        then:
        thrown(EntityValidationException)
    }

    def "NEVER_VALIDATE test"() {
        when:
        def context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.NEVER_VALIDATE)
        def validatedEntity = new ValidatedEntity(name: "1")
        context.addInstanceToCommit(validatedEntity)
        def committedEntity = dataManager.commit(context).iterator().next()

        then:
        committedEntity == validatedEntity
    }

    def "Default validation test"() {
        when:
        def validatedEntity = new ValidatedEntity(name: "1")
        dataManager.commit(validatedEntity)

        then:
        noExceptionThrown()
    }

    def "Entity update test"() {
        when:
        def validatedEntity = new ValidatedEntity(name: "11111")
        def context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.ALWAYS_VALIDATE)
        context.addInstanceToCommit(validatedEntity)
        def entitySet = dataManager.commit(context)
        def committedEntity = entitySet.iterator().next()

        then:
        committedEntity == validatedEntity

        when:
        committedEntity.setName("1")
        context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.ALWAYS_VALIDATE)
        context.addInstanceToCommit(committedEntity)
        dataManager.commit(context)

        then:
        thrown(EntityValidationException)
    }

    def "Entity remove test"() {
        when:
        def context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.NEVER_VALIDATE)
        def validatedEntity = new ValidatedEntity(name: "1")
        context.addInstanceToCommit(validatedEntity)
        def committedEntity = dataManager.commit(context).iterator().next()

        then:
        committedEntity == validatedEntity

        when:
        context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.ALWAYS_VALIDATE)
        context.addInstanceToRemove(committedEntity)
        dataManager.commit(context)

        then: "ok"
    }

    def "Embedded entity test"() {
        when:
        def context = new CommitContext()
        context.setValidationMode(CommitContext.ValidationMode.ALWAYS_VALIDATE)
        def validatedEntity = new ValidatedEntity(name: "11111")
        validatedEntity.setEmbeddedValidatedEntity(new EmbeddedValidatedEntity(name: "1"))
        context.addInstanceToCommit(validatedEntity)
        dataManager.commit(context)

        then:
        thrown(EntityValidationException)
    }
}
