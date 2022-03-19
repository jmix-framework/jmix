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

package entity_annotations

import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.core.entity.EntityEntryAuditable
import io.jmix.core.security.CurrentAuthentication
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.data.PersistenceHints
import io.jmix.data.impl.converters.AuditConversionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.DataSpec
import test_support.entity.auditing.AuditableSubclass
import test_support.entity.auditing.CreatableSubclass
import test_support.entity.auditing.IrregularAuditTypesEntity
import test_support.entity.auditing.NotAuditableSubclass

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

class AuditingTest extends DataSpec {

    @Autowired
    protected DataManager dataManager

    @Autowired
    protected TimeSource timeSource

    @Autowired
    protected SystemAuthenticator authenticator

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected InMemoryUserRepository userRepository

    @Autowired
    protected AuditConversionService auditConversion

    UserDetails admin

    def setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }

    def "entities enhanced properly"() {

        expect: "auditing should be applied only for entities with: 1. audit annotations  2. legacy interfaces"
        !(dataManager.create(NotAuditableSubclass).__getEntityEntry() instanceof EntityEntryAuditable)

        dataManager.create(CreatableSubclass).__getEntityEntry() instanceof EntityEntryAuditable
        dataManager.create(AuditableSubclass).__getEntityEntry() instanceof EntityEntryAuditable
    }

    def "auditing should work for inherited entities"() {
        setup:
        authenticator.begin()


        when:
        CreatableSubclass creatableEntity = dataManager.create(CreatableSubclass)

        Date beforeSave = timeSource.currentTimestamp()
        creatableEntity = dataManager.save(creatableEntity)
        Date afterSave = timeSource.currentTimestamp()

        EntityEntryAuditable creatableEntityEntry = ((EntityEntryAuditable) creatableEntity.__getEntityEntry())

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

        EntityEntryAuditable auditableEntityEntry = ((EntityEntryAuditable) auditableEntity.__getEntityEntry())

        then:

        beforeOrEquals(beforeCreate, auditableEntity.birthDate)
        afterOrEquals(afterCreate, auditableEntity.birthDate)

        beforeOrEquals(beforeUpdate, auditableEntity.touchDate)
        afterOrEquals(afterUpdate, auditableEntity.touchDate)

        !currentAuthentication.user.username.equals(auditableEntity.creator)
        currentAuthentication.user.username.equals(auditableEntity.touchedBy)

        auditableEntity.version == 2

        auditableEntityEntry.createdByClass.name.equals(String.class.name)
        auditableEntityEntry.createdDateClass.name.equals(Date.class.name)
        auditableEntityEntry.lastModifiedByClass.name.equals(String.class.name)
        auditableEntityEntry.lastModifiedDateClass.name.equals(Date.class.name)


        cleanup:
        authenticator.end()
    }

    def "Irregular timestamp type should work"() {
        expect:
        auditConversion.canConvert(Date, Date)
        auditConversion.canConvert(Date, LocalTime)
        auditConversion.canConvert(Date, LocalDate)
        auditConversion.canConvert(Date, LocalDateTime)
        auditConversion.canConvert(Date, OffsetDateTime)


        when:
        IrregularAuditTypesEntity irregularEntity = dataManager.create(IrregularAuditTypesEntity)
        irregularEntity = dataManager.save(irregularEntity)
        irregularEntity.setName("test")
        irregularEntity = dataManager.save(irregularEntity)
        dataManager.remove(irregularEntity)
        irregularEntity = dataManager.load(IrregularAuditTypesEntity).id(irregularEntity.id)
                .hint(PersistenceHints.SOFT_DELETION, false).one()

        then:
        irregularEntity.createdDate != null
        irregularEntity.touchDate != null
        irregularEntity.whenDeleted != null
    }

    static boolean beforeOrEquals(Date first, Date second) {
        return first.before(second) || first.equals(second)
    }

    static boolean afterOrEquals(Date first, Date second) {
        return first.after(second) || first.equals(second)
    }


}
