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

package auditing_and_softdelete

import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.TimeSource
import io.jmix.core.entity.EntityEntrySoftDelete
import io.jmix.core.security.Authenticator
import io.jmix.core.security.CurrentAuthentication
import io.jmix.core.security.impl.CoreUser
import io.jmix.core.security.impl.InMemoryUserRepository
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.soft_delete.SoftDeleteEntity
import test_support.entity.soft_delete.SoftDeleteWithUserEntity
import test_support.entity.soft_delete.TestLegacySoftDeleteEntity

//todo taimanov refactor to common class with Auditing test or with other tests when need
class SoftDeleteTest extends DataSpec {

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

    @Autowired
    Metadata metadata;

    CoreUser admin

    def setup() {
        admin = new CoreUser('admin', '{noop}admin123', 'Admin')
        userRepository.createUser(admin)
    }

    def cleanup() {
        userRepository.removeUser(admin)
    }

    def "Enhancing should work for SoftDelete entities"() {
        setup:
        authenticator.begin()

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

        beforeOrEquals(beforeDelete, entity.getDeleteTs())
        afterOrEquals(afterDelete, entity.getDeleteTs())
        "UFO".equals(entity.getWhoDeleted())
        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()


        cleanup:
        authenticator.end()

    }


    def "Soft deletion should work"() {
        setup:
        authenticator.begin("admin")

        SoftDeleteWithUserEntity entity = dataManager.create(SoftDeleteWithUserEntity)
        entity = dataManager.save(entity)

        TestLegacySoftDeleteEntity legacyEntity = dataManager.create(TestLegacySoftDeleteEntity)
        legacyEntity = dataManager.save(legacyEntity)

        SoftDeleteEntity tsOnly = dataManager.create(SoftDeleteEntity)
        tsOnly = dataManager.save(tsOnly)

        when:
        dataManager.remove(legacyEntity)
        legacyEntity = dataManager.load(TestLegacySoftDeleteEntity).id(legacyEntity.getId()).softDeletion(false).one()

        dataManager.remove(entity)
        entity = dataManager.load(SoftDeleteWithUserEntity).id(entity.getId()).softDeletion(false).one()

        dataManager.remove(tsOnly)
        tsOnly = dataManager.load(SoftDeleteEntity).id(tsOnly.getId()).softDeletion(false).one()

        then:

        legacyEntity.isDeleted()
        legacyEntity.getDeleteTs() != null
        legacyEntity.getDeletedBy().equals("admin")


        ((EntityEntrySoftDelete) entity.__getEntityEntry()).isDeleted()
        entity.whoDeleted.equals("admin")
        entity.deleteTs != null

        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).isDeleted()
        ((EntityEntrySoftDelete) tsOnly.__getEntityEntry()).getDeletedBy() == null
        entity.deleteTs != null

        cleanup:
        authenticator.end()
    }

    def "TODO test MetadataHelper"() {
        //todo taimanov improve tests
    }

    //todo taimanov: another cases: 1) date without by 2) ...


    static boolean beforeOrEquals(Date first, Date second) {
        return first.before(second) || first.equals(second)
    }

    static boolean afterOrEquals(Date first, Date second) {
        return first.after(second) || first.equals(second)
    }

}
