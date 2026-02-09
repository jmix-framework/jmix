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

package io.jmix.flowui.action.genericfilter;

import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.genericfilter.FilterUtils;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.settings.UiComponentSettings;
import io.jmix.flowui.facet.settings.component.GenericFilterSettings;
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action makes the current {@link FilterConfigurationModel} for the {@link GenericFilter} the default for the current view.
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

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.GenericFilter.MakeDefault");
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.GENERIC_FILTER_MAKE_DEFAULT_ACTION);
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable()
                && UiComponentUtils.getComponentId(target).isPresent()
                && target.isAttached()
                && FilterUtils.getFacet(target, SettingsFacet.class) != null
                && target.getCurrentConfiguration() != target.getEmptyConfiguration();
    }

    @Override
    public void execute() {
        checkTarget();

        SettingsFacet<?> settingsFacet = FilterUtils.getFacet(target, SettingsFacet.class);
        Preconditions.checkNotNullArgument(settingsFacet,
                "The view doesn't contain %s", SettingsFacet.class.getSimpleName());

        UiComponentSettings<?> settings = settingsFacet.getSettings();
        Preconditions.checkNotNullArgument(settings,
                "%s isn't attached to the view", SettingsFacet.class.getSimpleName());

        GenericFilterSettings genericFilterSettings = new GenericFilterSettings();
        genericFilterSettings.setId(UiComponentUtils.getComponentId(target).orElse(null));
        genericFilterSettings.setDefaultConfigurationId(target.getCurrentConfiguration().getId());
        settings.put(genericFilterSettings);
    }
}
