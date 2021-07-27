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

package io.jmix.multitenancyui.screen.tenant;

import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Predicate;

@UiController("mten_Tenant.edit")
@UiDescriptor("tenant-edit.xml")
@EditedEntityContainer("tenantDc")
public class TenantEdit extends StandardEditor<Tenant> {

    @Autowired
    private TextField<String> tenantIdField;

    @Autowired
    private TextField<String> nameField;

    @Autowired
    private Messages messages;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInitEntity(InitEntityEvent<Tenant> event) {
        tenantIdField.setEditable(true);
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Tenant editedEntity = getEditedEntity();
        initField(tenantIdField,
                tenant -> tenant.getTenantId().equals(editedEntity.getTenantId()),
                "io.jmix.multitenancyui.screen.tenant/tenantEdit.uniqueTenantId");
        initField(nameField,
                tenant -> tenant.getName().equals(editedEntity.getName()),
                "io.jmix.multitenancyui.screen.tenant/tenantEdit.uniqueName");
    }

    private void initField(TextField<?> textField, Predicate<Tenant> predicate, String messageKey) {
        textField.addValidator(s -> {
            boolean exist = dataManager.load(Tenant.class)
                    .all()
                    .list()
                    .stream()
                    .anyMatch(predicate);
            if (exist) {
                throw new ValidationException(messages.getMessage(messageKey));
            }
        });
    }


}