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

package audition

import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.core.entity.JmixAuditable
import io.jmix.core.security.Authenticator
import io.jmix.core.security.CurrentAuthentication
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.audition.AuditableSubclass
import test_support.entity.audition.CreatableSubclass
import test_support.entity.audition.NotAuditableSubclass

class AuditionTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    TimeSource timeSource

    @Autowired
    Authenticator authenticator

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    InMemoryUserRepository userRepository

    CoreUser admin

    def setup() {
        admin = new CoreUser('admin', '{noop}admin123', 'Admin')
        userRepository.createUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }

    def "entities enhanced properly"() {

        expect: "audition should be applied only for entities with: 1. audit annotations  2. legacy interfaces"
        !(dataManager.create(NotAuditableSubclass).__getEntityEntry() instanceof JmixAuditable)

        dataManager.create(CreatableSubclass).__getEntityEntry() instanceof JmixAuditable
        dataManager.create(AuditableSubclass).__getEntityEntry() instanceof JmixAuditable
    }

    def "audition should work for inherited entities"() {
        setup:
        authenticator.begin()


        when:
        CreatableSubclass creatableEntity = dataManager.create(CreatableSubclass)

        Date beforeSave = timeSource.currentTimestamp()
        creatableEntity = dataManager.save(creatableEntity)
        Date afterSave = timeSource.currentTimestamp()

        JmixAuditable creatableEntityEntry = ((JmixAuditable) creatableEntity.__getEntityEntry())

        then:
        beforeOrEquals(beforeSave, creatableEntity.birthDate)
        afterOrEquals(afterSave, creatableEntity.birthDate)

        currentAuthentication.user.username.equals(creatableEntity.creator)

        creatableEntityEntry.createdByClass.name.equals(String.class.name)
        creatableEntityEntry.createdDateClass.name.equals(Date.class.name)
        creatableEntityEntry.lastModifiedByClass == null
        creatableEntityEntry.lastModifiedDateClass == null


        when:

        AuditableSubclass auditableEntity = dataManager.create(AuditableSubclass)

        Date beforeCreate = timeSource.currentTimestamp()
        auditableEntity = dataManager.save(auditableEntity)
        Date afterCreate = timeSource.currentTimestamp()

        authenticator.end()
        authenticator.begin("admin")

        auditableEntity.title = "Updated entity"

        Date beforeUpdate = timeSource.currentTimestamp()
        auditableEntity = dataManager.save(auditableEntity)
        Date afterUpdate = timeSource.currentTimestamp()

        JmixAuditable auditableEntityEntry = ((JmixAuditable) auditableEntity.__getEntityEntry())

        then:

        beforeOrEquals(beforeCreate, auditableEntity.birthDate)
        afterOrEquals(afterCreate, auditableEntity.birthDate)

        beforeOrEquals(beforeUpdate, auditableEntity.touchDate)
        afterOrEquals(afterUpdate, auditableEntity.touchDate)

        !currentAuthentication.user.username.equals(auditableEntity.creator)
        currentAuthentication.user.username.equals(auditableEntity.touchedBy)

        auditableEntityEntry.createdByClass.name.equals(String.class.name)
        auditableEntityEntry.createdDateClass.name.equals(Date.class.name)
        auditableEntityEntry.lastModifiedByClass.name.equals(String.class.name)
        auditableEntityEntry.lastModifiedDateClass.name.equals(Date.class.name)


        cleanup:
        authenticator.end()
    }

    static boolean beforeOrEquals(Date first, Date second) {
        return first.before(second) || first.equals(second)
    }

    static boolean afterOrEquals(Date first, Date second) {
        return first.after(second) || first.equals(second)
    }


}
