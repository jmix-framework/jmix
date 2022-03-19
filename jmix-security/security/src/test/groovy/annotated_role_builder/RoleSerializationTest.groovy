/*
 * Copyright 2021 Haulmont.
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

package annotated_role_builder

import io.jmix.security.impl.role.builder.AnnotatedRoleBuilder
import io.jmix.security.model.RowLevelBiPredicate
import io.jmix.security.model.RowLevelRole
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import test_support.SecuritySpecification
import test_support.annotated_role_builder.TestPredicateRoleLevelPolicyRole

class RoleSerializationTest extends SecuritySpecification {

    @Autowired
    AnnotatedRoleBuilder annotatedRoleBuilder

    @Autowired
    ApplicationContext applicationContext

    def "RowLevelBiPredicate must be serialized and deserialized with no errors"() {

        RowLevelRole role = annotatedRoleBuilder.createRowLevelRole(TestPredicateRoleLevelPolicyRole.class.getCanonicalName())
        def policies = role.rowLevelPolicies

        when:

        def serializedPolicyPredicates = []

        policies.forEach(policy -> {
            ByteArrayOutputStream bos = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(bos)
            oos.writeObject(policy.biPredicate)
            oos.flush()
            oos.close()

            serializedPolicyPredicates.add(bos.toByteArray())
        })

        then:

        serializedPolicyPredicates.forEach(bytes -> {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            RowLevelBiPredicate deserializedPredicate = (RowLevelBiPredicate) ois.readObject();
            deserializedPredicate != null
            ois.close();
            bis.close();
        })
    }
}
