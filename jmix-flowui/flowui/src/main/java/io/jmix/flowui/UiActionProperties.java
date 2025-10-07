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
@ConfigurationProperties(prefix = "jmix.ui.action")
public class UiActionProperties {

    String detailCloseIcon;
    String detailDiscardIcon;
    String detailEnableEditingIcon;
    String detailSaveIcon;
    String detailSaveCloseIcon;
    String lookupDiscardIcon;
    String lookupSelectIcon;
    String viewCloseIcon;

    String listAddIcon;
    String listCreateIcon;
    String listEditIcon;
    String listExcludeIcon;
    String listReadIcon;
    String listRefreshIcon;
    String listRemoveIcon;

    String entityClearIcon;
    String entityLookupIcon;
    String entityOpenIcon;

    String valueDateIntervalIcon;
    String valueClearIcon;

    String multiValueSelectIcon;

    String genericFilterAddConditionIcon;
    String genericFilterClearValuesIcon;
    String genericFilterCopyIcon;
    String genericFilterEditIcon;
    String genericFilterMakeDefaultIcon;
    String genericFilterRemoveIcon;
    String genericFilterSaveIcon;
    String genericFilterSaveAsIcon;
    String genericFilterSaveWithValuesIcon;

    String userMenuThemeSwitchSystemIcon;
    String userMenuThemeSwitchLightIcon;
    String userMenuThemeSwitchDarkIcon;

    String logoutIcon;

    public UiActionProperties(
            @DefaultValue("vaadin:ban") String detailCloseIcon,
            @DefaultValue("vaadin:ban") String detailDiscardIcon,
            @DefaultValue("vaadin:pencil") String detailEnableEditingIcon,
            @DefaultValue("vaadin:archive") String detailSaveIcon,
            @DefaultValue("vaadin:check") String detailSaveCloseIcon,
            @DefaultValue("vaadin:ban") String lookupDiscardIcon,
            @DefaultValue("vaadin:check") String lookupSelectIcon,
            @DefaultValue("vaadin:ban") String viewCloseIcon,
            @DefaultValue("vaadin:plus") String listAddIcon,
            @DefaultValue("vaadin:plus") String listCreateIcon,
            @DefaultValue("vaadin:pencil") String listEditIcon,
            @DefaultValue("vaadin:close") String listExcludeIcon,
            @DefaultValue("vaadin:eye") String listReadIcon,
            @DefaultValue("vaadin:refresh") String listRefreshIcon,
            @DefaultValue("vaadin:trash") String listRemoveIcon,
            @DefaultValue("vaadin:close") String entityClearIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String entityLookupIcon,
            @DefaultValue("vaadin:search") String entityOpenIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String valueDateIntervalIcon,
            @DefaultValue("vaadin:close") String valueClearIcon,
            @DefaultValue("vaadin:ellipsis-dots-h") String multiValueSelectIcon,

            @DefaultValue("vaadin:plus") String genericFilterAddConditionIcon,
            @DefaultValue("vaadin:eraser") String genericFilterClearValuesIcon,
            @DefaultValue("vaadin:copy") String genericFilterCopyIcon,
            @DefaultValue("vaadin:pencil") String genericFilterEditIcon,
            @DefaultValue("vaadin:star") String genericFilterMakeDefaultIcon,
            @DefaultValue("vaadin:trash") String genericFilterRemoveIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveAsIcon,
            @DefaultValue("vaadin:archive") String genericFilterSaveWithValuesIcon,

            @DefaultValue("vaadin:adjust") String userMenuThemeSwitchSystemIcon,
            @DefaultValue("vaadin:sun-o") String userMenuThemeSwitchLightIcon,
            @DefaultValue("vaadin:moon-o") String userMenuThemeSwitchDarkIcon,
            @DefaultValue("vaadin:sign-out") String logoutIcon) {
        this.detailCloseIcon = detailCloseIcon;
        this.detailDiscardIcon = detailDiscardIcon;
        this.detailEnableEditingIcon = detailEnableEditingIcon;
        this.detailSaveIcon = detailSaveIcon;
        this.detailSaveCloseIcon = detailSaveCloseIcon;
        this.lookupDiscardIcon = lookupDiscardIcon;
        this.lookupSelectIcon = lookupSelectIcon;
        this.viewCloseIcon = viewCloseIcon;

        this.listAddIcon = listAddIcon;
        this.listCreateIcon = listCreateIcon;
        this.listEditIcon = listEditIcon;
        this.listExcludeIcon = listExcludeIcon;
        this.listReadIcon = listReadIcon;
        this.listRefreshIcon = listRefreshIcon;
        this.listRemoveIcon = listRemoveIcon;

        this.entityClearIcon = entityClearIcon;
        this.entityLookupIcon = entityLookupIcon;
        this.entityOpenIcon = entityOpenIcon;

        this.valueDateIntervalIcon = valueDateIntervalIcon;
        this.valueClearIcon = valueClearIcon;

        this.multiValueSelectIcon = multiValueSelectIcon;

        this.genericFilterAddConditionIcon = genericFilterAddConditionIcon;
        this.genericFilterClearValuesIcon = genericFilterClearValuesIcon;
        this.genericFilterCopyIcon = genericFilterCopyIcon;
        this.genericFilterEditIcon = genericFilterEditIcon;
        this.genericFilterMakeDefaultIcon = genericFilterMakeDefaultIcon;
        this.genericFilterRemoveIcon = genericFilterRemoveIcon;
        this.genericFilterSaveIcon = genericFilterSaveIcon;
        this.genericFilterSaveAsIcon = genericFilterSaveAsIcon;
        this.genericFilterSaveWithValuesIcon = genericFilterSaveWithValuesIcon;

        this.userMenuThemeSwitchSystemIcon = userMenuThemeSwitchSystemIcon;
        this.userMenuThemeSwitchLightIcon = userMenuThemeSwitchLightIcon;
        this.userMenuThemeSwitchDarkIcon = userMenuThemeSwitchDarkIcon;

        this.logoutIcon = logoutIcon;
    }

    public String getDetailCloseIcon() {
        return detailCloseIcon;
    }

    public String getDetailDiscardIcon() {
        return detailDiscardIcon;
    }

    public String getDetailEnableEditingIcon() {
        return detailEnableEditingIcon;
    }

    public String getDetailSaveIcon() {
        return detailSaveIcon;
    }

    public String getDetailSaveCloseIcon() {
        return detailSaveCloseIcon;
    }

    public String getLookupDiscardIcon() {
        return lookupDiscardIcon;
    }

    public String getLookupSelectIcon() {
        return lookupSelectIcon;
    }

    public String getViewCloseIcon() {
        return viewCloseIcon;
    }

    public String getListAddIcon() {
        return listAddIcon;
    }

    public String getListCreateIcon() {
        return listCreateIcon;
    }

    public String getListEditIcon() {
        return listEditIcon;
    }

    public String getListExcludeIcon() {
        return listExcludeIcon;
    }

    public String getListReadIcon() {
        return listReadIcon;
    }

    public String getListRefreshIcon() {
        return listRefreshIcon;
    }

    public String getListRemoveIcon() {
        return listRemoveIcon;
    }

    public String getEntityClearIcon() {
        return entityClearIcon;
    }

    public String getEntityLookupIcon() {
        return entityLookupIcon;
    }

    public String getEntityOpenIcon() {
        return entityOpenIcon;
    }

    public String getValueDateIntervalIcon() {
        return valueDateIntervalIcon;
    }

    public String getValueClearIcon() {
        return valueClearIcon;
    }

    public String getMultiValueSelectIcon() {
        return multiValueSelectIcon;
    }

    public String getGenericFilterAddConditionIcon() {
        return genericFilterAddConditionIcon;
    }

    public String getGenericFilterClearValuesIcon() {
        return genericFilterClearValuesIcon;
    }

    public String getGenericFilterCopyIcon() {
        return genericFilterCopyIcon;
    }

    public String getGenericFilterEditIcon() {
        return genericFilterEditIcon;
    }

    public String getGenericFilterMakeDefaultIcon() {
        return genericFilterMakeDefaultIcon;
    }

    public String getGenericFilterRemoveIcon() {
        return genericFilterRemoveIcon;
    }

    public String getGenericFilterSaveIcon() {
        return genericFilterSaveIcon;
    }

    public String getGenericFilterSaveAsIcon() {
        return genericFilterSaveAsIcon;
    }

    public String getGenericFilterSaveWithValuesIcon() {
        return genericFilterSaveWithValuesIcon;
    }

    public String getUserMenuThemeSwitchSystemIcon() {
        return userMenuThemeSwitchSystemIcon;
    }

    public String getUserMenuThemeSwitchLightIcon() {
        return userMenuThemeSwitchLightIcon;
    }

    public String getUserMenuThemeSwitchDarkIcon() {
        return userMenuThemeSwitchDarkIcon;
    }

    public String getLogoutIcon() {
        return logoutIcon;
    }
}
