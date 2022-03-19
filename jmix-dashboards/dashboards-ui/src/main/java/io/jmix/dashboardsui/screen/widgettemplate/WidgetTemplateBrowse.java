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

package io.jmix.dashboardsui.screen.widgettemplate;

import io.jmix.core.Metadata;
import io.jmix.dashboards.entity.WidgetTemplate;
import io.jmix.dashboards.converter.JsonConverter;
import io.jmix.dashboardsui.dashboard.tools.AccessConstraintsHelper;
import io.jmix.dashboardsui.screen.widgettemplategroup.WidgetTemplateGroupBrowse;
import io.jmix.ui.Screens;
import io.jmix.ui.component.Button;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("dshbrd_WidgetTemplate.browse")
@UiDescriptor("widget-template-browse.xml")
@LookupComponent("widgetTemplateTable")
public class WidgetTemplateBrowse extends StandardLookup<WidgetTemplate> {
    @Autowired
    protected CollectionContainer<WidgetTemplate> widgetTemplatesDc;

    @Autowired
    protected JsonConverter converter;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Screens screens;

    @Autowired
    protected CollectionLoader<WidgetTemplate> widgetTemplatesDl;

    @Autowired
    protected AccessConstraintsHelper accessConstraintsHelper;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        widgetTemplatesDl.setParameter("currentUser", accessConstraintsHelper.getCurrentUsername());
        widgetTemplatesDl.load();
    }


    @Subscribe("widgetTemplateGroupsBrowse")
    public void onWidgetTemplateGroupsBrowseClick(Button.ClickEvent event) {
       screens.create(WidgetTemplateGroupBrowse.class, OpenMode.NEW_TAB).show();
    }
}