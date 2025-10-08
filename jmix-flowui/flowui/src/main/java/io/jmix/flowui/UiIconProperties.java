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

package io.jmix.flowui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for UI components in a Jmix application.
 */
@ConfigurationProperties(prefix = "jmix.ui.icon")
public class UiIconProperties {

    String detailCloseActionIcon;
    String detailDiscardActionIcon;
    String detailEnableEditingActionIcon;
    String detailSaveActionIcon;
    String detailSaveCloseActionIcon;
    String lookupDiscardActionIcon;
    String lookupSelectActionIcon;
    String viewCloseActionIcon;

    String listAddActionIcon;
    String listCreateActionIcon;
    String listEditActionIcon;
    String listExcludeActionIcon;
    String listReadActionIcon;
    String listRefreshActionIcon;
    String listRemoveActionIcon;

    String entityClearActionIcon;
    String entityLookupActionIcon;
    String entityOpenActionIcon;

    String valueDateIntervalActionIcon;
    String valueClearActionIcon;

    String multiValueSelectActionIcon;

    String genericFilterAddConditionActionIcon;
    String genericFilterClearValuesActionIcon;
    String genericFilterCopyActionIcon;
    String genericFilterEditActionIcon;
    String genericFilterMakeDefaultActionIcon;
    String genericFilterRemoveActionIcon;
    String genericFilterSaveActionIcon;
    String genericFilterSaveAsActionIcon;
    String genericFilterSaveWithValuesActionIcon;

    String userMenuThemeSwitchActionSystemIcon;
    String userMenuThemeSwitchActionLightIcon;
    String userMenuThemeSwitchActionDarkIcon;

    String logoutActionIcon;

    String genericFilterSettingsButtonIcon;
    String genericFilterConditionRemoveButtonIcon;

    public UiIconProperties(
            @DefaultValue("vaadin:ban") String detailCloseActionIcon,
            @DefaultValue("vaadin:ban") String detailDiscardActionIcon,
            @DefaultValue("vaadin:pencil") String detailEnableEditingActionIcon,
            @DefaultValue("vaadin:archive") String detailSaveActionIcon,
            @DefaultValue("vaadin:check") String detailSaveCloseActionIcon,
            @DefaultValue("vaadin:ban") String lookupDiscardActionIcon,
            @DefaultValue("vaadin:check") String lookupSelectActionIcon,
            @DefaultValue("vaadin:ban") String viewCloseActionIcon,
            @DefaultValue("vaadin:plus") String listAddActionIcon,
            @DefaultValue("vaadin:plus") String listCreateActionIcon,
            @DefaultValue("vaadin:pencil") String listEditActionIcon,
            @DefaultValue("vaadin:close") String listExcludeActionIcon,
            @DefaultValue("vaadin:eye") String listReadActionIcon,
            @DefaultValue("vaadin:refresh") String listRefreshActionIcon,
            @DefaultValue("vaadin:trash") String listRemoveActionIcon,
            @DefaultValue("vaadin:close") String entityClearActionIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String entityLookupActionIcon,
            @DefaultValue("vaadin:search") String entityOpenActionIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String valueDateIntervalActionIcon,
            @DefaultValue("vaadin:close") String valueClearActionIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String multiValueSelectActionIcon,
            @DefaultValue("vaadin:plus") String genericFilterAddConditionActionIcon,
            @DefaultValue("vaadin:eraser") String genericFilterClearValuesActionIcon,
            @DefaultValue("vaadin:copy") String genericFilterCopyActionIcon,
            @DefaultValue("vaadin:pencil") String genericFilterEditActionIcon,
            @DefaultValue("vaadin:star") String genericFilterMakeDefaultActionIcon,
            @DefaultValue("vaadin:trash") String genericFilterRemoveActionIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveActionIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveAsActionIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveWithValuesActionIcon,
            @DefaultValue("vaadin:adjust") String userMenuThemeSwitchActionSystemIcon,
            @DefaultValue("vaadin:sun-o") String userMenuThemeSwitchActionLightIcon,
            @DefaultValue("vaadin:moon-o") String userMenuThemeSwitchActionDarkIcon,
            @DefaultValue("vaadin:sign-out") String logoutActionIcon,
            @DefaultValue("vaadin:cog") String genericFilterSettingsButtonIcon,
            @DefaultValue("vaadin:trash") String genericFilterConditionRemoveButtonIcon) {
        /* Actions */
        this.detailCloseActionIcon = detailCloseActionIcon;
        this.detailDiscardActionIcon = detailDiscardActionIcon;
        this.detailEnableEditingActionIcon = detailEnableEditingActionIcon;
        this.detailSaveActionIcon = detailSaveActionIcon;
        this.detailSaveCloseActionIcon = detailSaveCloseActionIcon;
        this.lookupDiscardActionIcon = lookupDiscardActionIcon;
        this.lookupSelectActionIcon = lookupSelectActionIcon;
        this.viewCloseActionIcon = viewCloseActionIcon;

        this.listAddActionIcon = listAddActionIcon;
        this.listCreateActionIcon = listCreateActionIcon;
        this.listEditActionIcon = listEditActionIcon;
        this.listExcludeActionIcon = listExcludeActionIcon;
        this.listReadActionIcon = listReadActionIcon;
        this.listRefreshActionIcon = listRefreshActionIcon;
        this.listRemoveActionIcon = listRemoveActionIcon;

        this.entityClearActionIcon = entityClearActionIcon;
        this.entityLookupActionIcon = entityLookupActionIcon;
        this.entityOpenActionIcon = entityOpenActionIcon;

        this.valueDateIntervalActionIcon = valueDateIntervalActionIcon;
        this.valueClearActionIcon = valueClearActionIcon;

        this.multiValueSelectActionIcon = multiValueSelectActionIcon;

        this.genericFilterAddConditionActionIcon = genericFilterAddConditionActionIcon;
        this.genericFilterClearValuesActionIcon = genericFilterClearValuesActionIcon;
        this.genericFilterCopyActionIcon = genericFilterCopyActionIcon;
        this.genericFilterEditActionIcon = genericFilterEditActionIcon;
        this.genericFilterMakeDefaultActionIcon = genericFilterMakeDefaultActionIcon;
        this.genericFilterRemoveActionIcon = genericFilterRemoveActionIcon;
        this.genericFilterSaveActionIcon = genericFilterSaveActionIcon;
        this.genericFilterSaveAsActionIcon = genericFilterSaveAsActionIcon;
        this.genericFilterSaveWithValuesActionIcon = genericFilterSaveWithValuesActionIcon;

        this.userMenuThemeSwitchActionSystemIcon = userMenuThemeSwitchActionSystemIcon;
        this.userMenuThemeSwitchActionLightIcon = userMenuThemeSwitchActionLightIcon;
        this.userMenuThemeSwitchActionDarkIcon = userMenuThemeSwitchActionDarkIcon;

        this.logoutActionIcon = logoutActionIcon;

        /* Components */
        this.genericFilterSettingsButtonIcon = genericFilterSettingsButtonIcon;
        this.genericFilterConditionRemoveButtonIcon = genericFilterConditionRemoveButtonIcon;


        /* Views */

    }

    public String getDetailCloseActionIcon() {
        return detailCloseActionIcon;
    }

    public String getDetailDiscardActionIcon() {
        return detailDiscardActionIcon;
    }

    public String getDetailEnableEditingActionIcon() {
        return detailEnableEditingActionIcon;
    }

    public String getDetailSaveActionIcon() {
        return detailSaveActionIcon;
    }

    public String getDetailSaveCloseActionIcon() {
        return detailSaveCloseActionIcon;
    }

    public String getLookupDiscardActionIcon() {
        return lookupDiscardActionIcon;
    }

    public String getLookupSelectActionIcon() {
        return lookupSelectActionIcon;
    }

    public String getViewCloseActionIcon() {
        return viewCloseActionIcon;
    }

    public String getListAddActionIcon() {
        return listAddActionIcon;
    }

    public String getListCreateActionIcon() {
        return listCreateActionIcon;
    }

    public String getListEditActionIcon() {
        return listEditActionIcon;
    }

    public String getListExcludeActionIcon() {
        return listExcludeActionIcon;
    }

    public String getListReadActionIcon() {
        return listReadActionIcon;
    }

    public String getListRefreshActionIcon() {
        return listRefreshActionIcon;
    }

    public String getListRemoveActionIcon() {
        return listRemoveActionIcon;
    }

    public String getEntityClearActionIcon() {
        return entityClearActionIcon;
    }

    public String getEntityLookupActionIcon() {
        return entityLookupActionIcon;
    }

    public String getEntityOpenActionIcon() {
        return entityOpenActionIcon;
    }

    public String getValueDateIntervalActionIcon() {
        return valueDateIntervalActionIcon;
    }

    public String getValueClearActionIcon() {
        return valueClearActionIcon;
    }

    public String getMultiValueSelectActionIcon() {
        return multiValueSelectActionIcon;
    }

    public String getGenericFilterAddConditionActionIcon() {
        return genericFilterAddConditionActionIcon;
    }

    public String getGenericFilterClearValuesActionIcon() {
        return genericFilterClearValuesActionIcon;
    }

    public String getGenericFilterCopyActionIcon() {
        return genericFilterCopyActionIcon;
    }

    public String getGenericFilterEditActionIcon() {
        return genericFilterEditActionIcon;
    }

    public String getGenericFilterMakeDefaultActionIcon() {
        return genericFilterMakeDefaultActionIcon;
    }

    public String getGenericFilterRemoveActionIcon() {
        return genericFilterRemoveActionIcon;
    }

    public String getGenericFilterSaveActionIcon() {
        return genericFilterSaveActionIcon;
    }

    public String getGenericFilterSaveAsActionIcon() {
        return genericFilterSaveAsActionIcon;
    }

    public String getGenericFilterSaveWithValuesActionIcon() {
        return genericFilterSaveWithValuesActionIcon;
    }

    public String getUserMenuThemeSwitchActionSystemIcon() {
        return userMenuThemeSwitchActionSystemIcon;
    }

    public String getUserMenuThemeSwitchActionLightIcon() {
        return userMenuThemeSwitchActionLightIcon;
    }

    public String getUserMenuThemeSwitchActionDarkIcon() {
        return userMenuThemeSwitchActionDarkIcon;
    }

    public String getLogoutActionIcon() {
        return logoutActionIcon;
    }

    public String getGenericFilterSettingsButtonIcon() {
        return genericFilterSettingsButtonIcon;
    }

    public String getGenericFilterConditionRemoveButtonIcon() {
        return genericFilterConditionRemoveButtonIcon;
    }
}
