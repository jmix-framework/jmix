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
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.genericfilter.GenericFilterAction;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowuidata.entity.FilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action makes the current {@link FilterConfiguration} for the {@link GenericFilter} the default for the current view.
 */
@ActionType(GenericFilterMakeDefaultAction.ID)
public class GenericFilterMakeDefaultAction extends GenericFilterAction<GenericFilterMakeDefaultAction> {

    public static final String ID = "genericFilter_makeDefault";

    public GenericFilterMakeDefaultAction() {
        this(ID);
    }

    public GenericFilterMakeDefaultAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.STAR);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.MakeDefault");
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && target.getId().isPresent()
                && ViewControllerUtils.getViewFacet(getParentView(), SettingsFacet.class) != null
                && target.getCurrentConfiguration() != target.getEmptyConfiguration();
    }

    @Override
    public void execute() {
        checkTarget();

        SettingsFacet settingsFacet = ViewControllerUtils.getViewFacet(getParentView(), SettingsFacet.class);
        Preconditions.checkNotNullArgument(settingsFacet,
                "The view doesn't contain %s", SettingsFacet.class.getSimpleName());

        ViewSettings settings = settingsFacet.getSettings();
        Preconditions.checkNotNullArgument(settings,
                "%s isn't attached to the view", SettingsFacet.class.getSimpleName());

        GenericFilterSettings genericFilterSettings = new GenericFilterSettings();
        genericFilterSettings.setId(target.getId().orElse(null));
        genericFilterSettings.setDefaultConfigurationId(target.getCurrentConfiguration().getId());
        settings.put(genericFilterSettings);
    }
}
