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

package io.jmix.uidata.action.filter;

import io.jmix.core.Messages;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.filter.FilterAction;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.component.FilterSettings;
import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@StudioAction(
        target = "io.jmix.ui.component.Filter",
        description = "Makes the filter configuration default for this screen")
@ActionType(FilterMakeDefaultAction.ID)
public class FilterMakeDefaultAction extends FilterAction {

    public static final String ID = "filter_makeDefault";

    public FilterMakeDefaultAction() {
        this(ID);
    }

    public FilterMakeDefaultAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Filter.MakeDefault");
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && filter.getId() != null
                && filter.getFrame() != null
                && UiControllerUtils.getFacet(filter.getFrame(), ScreenSettingsFacet.class) != null
                && !Objects.equals(filter.getCurrentConfiguration(), filter.getEmptyConfiguration());
    }

    @Override
    public void execute() {
        if (filter.getFrame() == null) {
            throw new IllegalStateException("Filter component is not attached to the Frame");
        }

        ScreenSettingsFacet screenSettingsFacet = UiControllerUtils
                .getFacet(filter.getFrame(), ScreenSettingsFacet.class);

        if (screenSettingsFacet == null) {
            throw new IllegalStateException("The Frame does no contain ScreenSettingsFacet");
        }

        ScreenSettings settings = screenSettingsFacet.getSettings();
        if (settings == null) {
            throw new IllegalStateException("ScreenSettingsFacet is not attached to the frame");
        }

        FilterSettings filterSettings = new FilterSettings();
        filterSettings.setId(filter.getId());
        filterSettings.setDefaultConfigurationId(filter.getCurrentConfiguration().getId());
        settings.put(filterSettings);
    }
}
