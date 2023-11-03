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

package pessimisticlock

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.security.SystemAuthenticator
import io.jmix.pessimisticlock.LockManager
import io.jmix.pessimisticlock.entity.LockInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.PessimisticLockTestConfiguration
import test_support.entity.LockableChildEntity
import test_support.entity.LockableParentEntity

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull

@ContextConfiguration(classes = [CoreConfiguration, PessimisticLockTestConfiguration])
class LockManagerTest extends Specification {

    @Autowired
    Metadata metadata

    @Autowired
    LockManager lockManager

    @Autowired
    SystemAuthenticator authenticator

    void setup() {
        authenticator.begin()
    }

    void cleanup() {
        authenticator.end()
    }

    def "Test entity lock defined by annotation"() {
        LockableParentEntity entity = metadata.create(LockableParentEntity.class)
        entity.setName("Test name")

        when:
        LockInfo lockInfo = lockManager.lock(entity)
        then:
        assertNull(lockInfo)

        when:
        lockInfo = lockManager.lock(entity)
        then:
        assertNotNull(lockInfo)

        when:
        lockManager.unlock(entity)
        lockInfo = lockManager.getLockInfo("psmlock_LockableParentEntity", entity.getId().toString())
        then:
        assertNull(lockInfo)
    }

    def "Test propagation of entity lock"() {
        LockableChildEntity entity = metadata.create(LockableChildEntity.class)
        entity.setName("Test name")

        when:
        LockInfo lockInfo = lockManager.lock(entity)
        then:
        assertNull(lockInfo)

        when:
        lockInfo = lockManager.lock(entity)
        then:
        assertNotNull(lockInfo)

        when:
        lockManager.unlock(entity)
        lockInfo = lockManager.getLockInfo("psmlock_LockableChildEntity", entity.getId().toString())
        then:
        assertNull(lockInfo)
    }
}
