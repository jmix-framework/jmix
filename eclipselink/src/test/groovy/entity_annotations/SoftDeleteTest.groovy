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
import io.jmix.core.entity.EntityEntrySoftDelete
import io.jmix.core.security.Authenticator
import io.jmix.core.security.InMemoryUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import test_support.DataSpec
import test_support.entity.soft_delete.SoftDeleteEntity
import test_support.entity.soft_delete.SoftDeleteWithUserEntity

class SoftDeleteTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    TimeSource timeSource

    @Autowired
    Authenticator authenticator

    @Autowired
    InMemoryUserRepository userRepository

    UserDetails admin

    def setup() {
        admin = User.builder()
                .username('admin')
                .password('{noop}admin123')
                .authorities(Collections.emptyList())
                .build()
        userRepository.addUser(admin)
        authenticator.begin()
    }

    def cleanup() {
        authenticator.end()
        userRepository.removeUser(admin)
    }

    def "Enhancing should work for SoftDelete entities"() {
        setup:
        SoftDeleteWithUserEntity entity = dataManager.create(SoftDeleteWithUserEntity)

        expect:
        (entity.__getEntityEntry()) instanceof EntityEntrySoftDelete


        when:
        EntityEntrySoftDelete softDeleteEntry = (EntityEntrySoftDelete) entity.__getEntityEntry()

        Date beforeDelete = timeSource.currentTimestamp()
        softDeleteEntry.setDeletedDate(timeSource.currentTimestamp())
        Date afterDelete = timeSource.currentTimestamp()

        softDeleteEntry.setDeletedBy("UFO")

        then:
        beforeOrEquals(beforeDelete, entity.getTimeOfDeletion())
        afterOrEquals(afterDelete, entity.getTimeOfDeletion())
        "UFO".equals(entity.getWhoDeleted())
        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()
    }


    def "Soft deletion should work"() {
        setup:
        authenticator.begin("admin")

        SoftDeleteWithUserEntity entity = dataManager.save(dataManager.create(SoftDeleteWithUserEntity))
        SoftDeleteEntity tsOnly = dataManager.save(dataManager.create(SoftDeleteEntity))


        when:
        Date beforeDelete = timeSource.currentTimestamp()
        dataManager.remove(entity)
        entity = dataManager.load(SoftDeleteWithUserEntity).id(entity.getId()).softDeletion(false).one()

        dataManager.remove(tsOnly)
        tsOnly = dataManager.load(SoftDeleteEntity).id(tsOnly.getId()).softDeletion(false).one()
        Date afterDelete = timeSource.currentTimestamp()


        then:
        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()
        entity.whoDeleted.equals("admin")
        beforeOrEquals(beforeDelete, entity.timeOfDeletion)
        afterOrEquals(afterDelete, entity.timeOfDeletion)

        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).isDeleted()
        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).getDeletedBy() == null
        beforeOrEquals(beforeDelete, tsOnly.timeOfDeletion)
        afterOrEquals(afterDelete, tsOnly.timeOfDeletion)

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
