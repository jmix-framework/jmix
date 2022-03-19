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

import com.haulmont.cuba.core.model.entity_annotations.LegacyAuditableEntity
import io.jmix.core.DataManager
import io.jmix.core.TimeSource
import io.jmix.core.entity.EntityEntryAuditable
import io.jmix.core.security.CurrentAuthentication
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class LegacyAuditingTest extends CoreTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    TimeSource timeSource

    @Autowired
    private CurrentAuthentication currentAuthentication;

    def "auditing should work for legacy entities"() {

        expect:
        dataManager.create(LegacyAuditableEntity).__getEntityEntry() instanceof EntityEntryAuditable

        when:

        LegacyAuditableEntity legacyAuditable = dataManager.create(LegacyAuditableEntity)

        Date beforeCreate = timeSource.currentTimestamp()
        legacyAuditable = dataManager.save(legacyAuditable)
        Date afterCreate = timeSource.currentTimestamp()

        legacyAuditable.name = "Legacy"

        Date beforeUpdate = timeSource.currentTimestamp()
        legacyAuditable = dataManager.save(legacyAuditable)
        Date afterUpdate = timeSource.currentTimestamp()

        EntityEntryAuditable auditableEntityEntry = ((EntityEntryAuditable) legacyAuditable.__getEntityEntry())

        then:

        beforeOrEquals(beforeCreate, legacyAuditable.createTs)
        afterOrEquals(afterCreate, legacyAuditable.createTs)

        beforeOrEquals(beforeUpdate, legacyAuditable.updateTs)
        afterOrEquals(afterUpdate, legacyAuditable.updateTs)

        currentAuthentication.user.username.equals(legacyAuditable.createdBy)
        currentAuthentication.user.username.equals(legacyAuditable.updatedBy)

        auditableEntityEntry.createdByClass.name.equals(String.class.name)
        auditableEntityEntry.createdDateClass.name.equals(Date.class.name)
        auditableEntityEntry.lastModifiedByClass.name.equals(String.class.name)
        auditableEntityEntry.lastModifiedDateClass.name.equals(Date.class.name)
    }

    static boolean beforeOrEquals(Date first, Date second) {
        return first.before(second) || first.equals(second)
    }

    static boolean afterOrEquals(Date first, Date second) {
        return first.after(second) || first.equals(second)
    }
}
