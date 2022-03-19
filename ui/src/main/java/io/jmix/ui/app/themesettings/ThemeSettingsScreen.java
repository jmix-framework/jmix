/*
 * Copyright (c) 2008-2020 Haulmont.
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

package io.jmix.ui.app.themesettings;

import com.vaadin.server.Page;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiThemeProperties;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeVariantsManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

@Internal
@Route("theme-settings")
@UiController("themeSettingsScreen")
@UiDescriptor("theme-settings-screen.xml")
public class ThemeSettingsScreen extends Screen {

    @Autowired
    protected RadioButtonGroup<String> modeField;
    @Autowired
    protected RadioButtonGroup<String> sizeField;

    @Autowired
    protected GroupBoxLayout previewBox;
    @Autowired
    protected ScrollBoxLayout innerPreviewBox;

    @Autowired
    protected ComboBox<String> requiredComboBox;
    @Autowired
    protected ComboBox<String> comboBox;
    @Autowired
    protected RadioButtonGroup<String> radioButtonGroup;
    @Autowired
    protected CheckBoxGroup<String> checkBoxGroup;

    @Autowired
    protected TabSheet tabSheet;
    @Autowired
    protected TabSheet tabSheetFramed;

    @Autowired
    protected ThemeVariantsManager variantsManager;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected UiThemeProperties uiThemeProperties;

    protected String appWindowTheme;
    protected boolean settingsAvailable;

    @Subscribe
    public void onInit(InitEvent event) {
        checkSettingsAvailable();

        if (settingsAvailable) {
            appWindowTheme = uiThemeProperties.getName();

            initModeField();
            initSizeField();
            initOptions();
            initTabSheets();
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!settingsAvailable) {
            notifications.create()
                    .withCaption(messageBundle.getMessage("themeSettings.noSettingsAvailable"))
                    .withType(Notifications.NotificationType.WARNING)
                    .show();

            close(WINDOW_CLOSE_ACTION);
        }
    }

    protected void checkSettingsAvailable() {
        List<String> appThemeModeList = variantsManager.getThemeModeList();
        List<String> appThemeSizeList = variantsManager.getThemeSizeList();

        settingsAvailable = CollectionUtils.isNotEmpty(appThemeModeList)
                || CollectionUtils.isNotEmpty(appThemeSizeList);
    }

    protected void initOptions() {
        List<String> options = generateSampleOptions();
        checkBoxGroup.setOptionsList(options);
        radioButtonGroup.setOptionsList(options);
        comboBox.setOptionsList(options);
        requiredComboBox.setOptionsList(options);
    }

    protected List<String> generateSampleOptions() {
        return Arrays.asList(
                messageBundle.formatMessage("themeSettings.option", 1),
                messageBundle.formatMessage("themeSettings.option", 2),
                messageBundle.formatMessage("themeSettings.option", 3)
        );
    }

    protected void initModeField() {
        modeField.setOptionsList(variantsManager.getThemeModeList());
        modeField.setValue(variantsManager.getThemeModeUserSettingOrDefault());
    }

    @Install(to = "modeField", subject = "optionCaptionProvider")
    protected String modeFieldOptionCaptionProvider(String mode) {
        return messageBundle.getMessage("themeSettings.mode." + mode);
    }

    protected void initSizeField() {
        sizeField.setOptionsList(variantsManager.getThemeSizeList());
        sizeField.setValue(variantsManager.getThemeSizeUserSettingOrDefault());
    }

    @Install(to = "sizeField", subject = "optionCaptionProvider")
    protected String sizeFieldOptionCaptionProvider(String size) {
        return messageBundle.getMessage("themeSettings.size." + size);
    }

    protected void initTabSheets() {
        for (int i = 1; i <= 3; i++) {
            tabSheet.addTab(
                    messageBundle.formatMessage("themeSettings.tab", i),
                    uiComponents.create(VBoxLayout.class));
            tabSheetFramed.addTab(
                    messageBundle.formatMessage("themeSettings.tab", i),
                    uiComponents.create(VBoxLayout.class));
        }
    }

    @Subscribe("applyBtn")
    public void onApplyBtnClick(Button.ClickEvent event) {
        applyThemeMode();
        applyThemeSize();

        Page.getCurrent().reload();
    }

    protected void applyThemeMode() {
        String mode = modeField.getValue();
        variantsManager.setThemeMode(mode);
    }

    protected void applyThemeSize() {
        String size = sizeField.getValue();
        variantsManager.setThemeSize(size);
    }

    @Subscribe("modeField")
    public void onModeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        previewBox.setStyleName(appWindowTheme + " " + modeField.getValue());
    }

    @Subscribe("sizeField")
    public void onSizeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        innerPreviewBox.setStyleName(sizeField.getValue());
    }
}