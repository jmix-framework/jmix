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

package io.jmix.emailtemplatesui.screen.templateblock;

import io.jmix.emailtemplates.entity.TemplateBlock;
import io.jmix.emailtemplatesui.screen.templateblockgroup.TemplateBlockGroupBrowse;
import io.jmix.ui.Screens;
import io.jmix.ui.component.Button;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;


@UiController("emltmp_TemplateBlock.browse")
@UiDescriptor("template-block-browse.xml")
@LookupComponent("templateBlocksTable")
public class TemplateBlockBrowse extends StandardLookup<TemplateBlock> {

    @Autowired
    private Screens screens;

    @Subscribe("groups")
    private void onGroupsClick(Button.ClickEvent event) {
        screens.create(TemplateBlockGroupBrowse.class, OpenMode.NEW_TAB).show();
    }
}