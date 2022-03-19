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

package io.jmix.securityui.screen.role;

import io.jmix.core.Messages;
import io.jmix.security.role.RoleRepository;
import io.jmix.securityui.model.BaseRoleModel;
import io.jmix.ui.Notifications;
import io.jmix.ui.RemoveOperation;

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
                    notifications.create()
                            .withCaption(messages.getMessage("io.jmix.securityui.screen.role/unableToRemove"))
                            .withDescription(messages.getMessage("io.jmix.securityui.screen.role/noPermission"))
                            .withType(Notifications.NotificationType.HUMANIZED)
                            .show();
                }
            } catch (UnsupportedOperationException | IllegalArgumentException ex) {
                notifications.create()
                        .withCaption(messages.getMessage("io.jmix.securityui.screen.role/unableToRemove"))
                        .withDescription(ex.getMessage())
                        .withType(Notifications.NotificationType.ERROR)
                        .show();
            }
        }
    }
}
