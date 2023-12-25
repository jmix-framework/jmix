/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securitydata.impl.role.assignment;

import io.jmix.core.DataManager;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component("sec_RoleAssignmentUserRemovalHandler")
public class RoleAssignmentUserRemovalHandler {

    private DataManager dataManager;

    public RoleAssignmentUserRemovalHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onUserRemove(UserRemovedEvent event) {
        List<RoleAssignmentEntity> assignments = dataManager.load(RoleAssignmentEntity.class)
                .query("e.username = :username")
                .parameter("username", event.getUsername())
                .list();
        dataManager.remove(assignments.toArray());
    }
}
