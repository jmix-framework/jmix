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

package io.jmix.flowuidata.action.genericfilter;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

//TODO: kremnevda, implement after viewSettingsFacet 10.04.2023
// https://github.com/jmix-framework/jmix/issues/1578
@ActionType(GenericFilterMakeDefaultAction.ID)
public class GenericFilterMakeDefaultAction extends GenericFilterAction<GenericFilterMakeDefaultAction> {

    public static final String ID = "filter_makeDefault";

    public GenericFilterMakeDefaultAction() {
        this(ID);
    }

    public GenericFilterMakeDefaultAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PIN);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.MakeDefault");
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && target.getId().isPresent()
                && findParent() != null
                //&& ViewControllerUtils.getViewFacets(findParent(), ViewSettingsFacet.class) != null
                && !Objects.equals(target.getCurrentConfiguration(), target.getEmptyConfiguration());
    }

    @Override
    public void execute() {
        checkTarget();

        /*ViewControllerUtils.getViewFacets(findParent(), ViewSettingsFacet.class);

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
        settings.put(filterSettings);*/
    }
}
