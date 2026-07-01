/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.meta.action;

import io.jmix.flowui.kit.meta.StudioAction;
import io.jmix.flowui.kit.meta.StudioPropertiesItem;
import io.jmix.flowui.kit.meta.StudioPropertyType;
import io.jmix.flowui.kit.meta.StudioUiKit;
import io.jmix.flowui.kit.meta.StudioPropertyGroups;
import io.jmix.flowui.kit.meta.StudioXmlAttributes;

@StudioUiKit(requiredDependencies = "io.jmix.bulkeditor:jmix-bulkeditor-starter")
interface StudioBulkEditorActions {

    @StudioAction(
            type = "bulked_edit",
            description = "Changes attribute values for several entity instances at once",
            classFqn = "io.jmix.bulkeditor.action.BulkEditAction",
            documentationLink = "%VERSION%/bulk-edit/index.html#usage",
            availableInViewWizard = true,
            propertyGroups = StudioActionPropertyGroups.BulkEditActionComponent.class,
            items = {
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.EXCLUDE, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.INCLUDE_PROPERTIES, type = StudioPropertyType.STRING),
                    @StudioPropertiesItem(xmlAttribute = StudioXmlAttributes.USE_CONFIRM_DIALOG, type = StudioPropertyType.BOOLEAN, defaultValue = "true")
            }
    )
    void bulkEditAction();
}
