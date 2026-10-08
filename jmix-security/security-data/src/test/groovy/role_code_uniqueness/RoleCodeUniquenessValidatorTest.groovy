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

package role_code_uniqueness

import io.jmix.core.Metadata
import io.jmix.core.UnconstrainedDataManager
import io.jmix.security.role.DuplicateRoleCodeException
import io.jmix.securitydata.entity.ResourceRoleEntity
import io.jmix.securitydata.entity.RowLevelRoleEntity
import io.jmix.securitydata.impl.role.RoleCodeUniquenessValidator
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityDataSpecification
import test_support.role.TestDefaultConstraintsRole
import test_support.role.TestFullAccessRole

class RoleCodeUniquenessValidatorTest extends SecurityDataSpecification {

    @Autowired
    RoleCodeUniquenessValidator validator
    @Autowired
    UnconstrainedDataManager dataManager
    @Autowired
    Metadata metadata

    def "database resource role colliding with an annotated role fails validation"() {
        given: "a database resource role with the code of an annotated design-time role"
        ResourceRoleEntity entity = metadata.create(ResourceRoleEntity)
        entity.code = TestFullAccessRole.CODE
        entity.name = 'Colliding runtime role'
        dataManager.save(entity)

        when:
        validator.validate()

        then:
        def e = thrown(DuplicateRoleCodeException)
        e.code == TestFullAccessRole.CODE
    }

    def "database row-level role colliding with an annotated role fails validation"() {
        given:
        RowLevelRoleEntity entity = metadata.create(RowLevelRoleEntity)
        entity.code = TestDefaultConstraintsRole.NAME
        entity.name = 'Colliding runtime row-level role'
        dataManager.save(entity)

        when:
        validator.validate()

        then:
        def e = thrown(DuplicateRoleCodeException)
        e.code == TestDefaultConstraintsRole.NAME
    }

    def "unique database role code passes validation"() {
        given:
        ResourceRoleEntity entity = metadata.create(ResourceRoleEntity)
        entity.code = 'unique-runtime-code'
        entity.name = 'Unique runtime role'
        dataManager.save(entity)

        when:
        validator.validate()

        then:
        noExceptionThrown()
    }
}
