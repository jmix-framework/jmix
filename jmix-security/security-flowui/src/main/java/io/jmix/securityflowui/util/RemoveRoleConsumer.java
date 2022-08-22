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

package io.jmix.securityflowui.util;

import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityflowui.model.BaseRoleModel;

import java.util.function.Consumer;

public class RemoveRoleConsumer<T extends BaseRoleModel>
        implements Consumer<RemoveOperation.BeforeActionPerformedEvent<T>> {

    protected RoleRepository<?> roleRepository;
    protected Notifications notifications;
    protected Messages messages;

    public RemoveRoleConsumer(RoleRepository<?> roleRepository,
                              Notifications notifications,
                              Messages messages) {
        this.roleRepository = roleRepository;
        this.notifications = notifications;
        this.messages = messages;
    }

    @Override
    public void accept(RemoveOperation.BeforeActionPerformedEvent<T> event) {
        if (!event.getItems().isEmpty()) {
            BaseRoleModel roleModel = event.getItems().get(0);
            try {
                boolean deleted = roleRepository.deleteRole(roleModel.getCode());
                if (!deleted) {
                    event.preventAction();
                    notifications.create(
                                    messages.getMessage("io.jmix.securityflowui.util/unableToRemove"),
                                    messages.getMessage("io.jmix.securityflowui.util/noPermission"))
                            .show();
                }
            } catch (UnsupportedOperationException | IllegalArgumentException ex) {
                notifications.create(
                                messages.getMessage("io.jmix.securityflowui.util/unableToRemove"),
                                ex.getMessage()
                        )
                        .withType(Notifications.Type.ERROR)
                        .show();
            }
        }
    }
}
