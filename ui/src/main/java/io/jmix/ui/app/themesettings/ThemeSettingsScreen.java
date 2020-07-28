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
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.HeliumThemeVariantsManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

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
    private ComboBox<String> requiredComboBox;
    @Autowired
    private ComboBox<String> comboBox;
    @Autowired
    private RadioButtonGroup<String> radioButtonGroup;
    @Autowired
    private CheckBoxGroup<String> checkBoxGroup;

    @Autowired
    protected HeliumThemeVariantsManager variantsManager;

    @Autowired
    protected Environment environment;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private Notifications notifications;

    protected String appWindowTheme;
    protected boolean settingsAvailable;

    @Subscribe
    public void onInit(InitEvent event) {
        checkSettingsAvailable();

        if (settingsAvailable) {
            appWindowTheme = environment.getProperty("jmix.ui.theme", "helium");

            initModeField();
            initSizeField();
            initOptions();
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (!settingsAvailable) {
            notifications.create()
                    .withCaption(messageBundle.getMessage("noSettingsAvailable"))
                    .withType(Notifications.NotificationType.WARNING)
                    .show();

            close(WINDOW_CLOSE_ACTION);
        }
    }

    protected void checkSettingsAvailable() {
        List<String> appThemeModeList = variantsManager.getAppThemeModeList();
        List<String> appThemeSizeList = variantsManager.getAppThemeSizeList();

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
        return Arrays.asList("Option 1", "Options 2", "Option 3");
    }

    protected void initModeField() {
        modeField.setOptionsList(variantsManager.getAppThemeModeList());
        modeField.setValue(variantsManager.loadUserAppThemeModeSettingOrDefault());
    }

    protected void initSizeField() {
        sizeField.setOptionsList(variantsManager.getAppThemeSizeList());
        sizeField.setValue(variantsManager.loadUserAppThemeSizeSettingOrDefault());
    }

    @Subscribe("applyBtn")
    public void onApplyBtnClick(Button.ClickEvent event) {
        applyThemeMode();
        applyThemeSize();

        Page.getCurrent().reload();
    }

    protected void applyThemeMode() {
        String mode = modeField.getValue();
        variantsManager.setUserAppThemeMode(mode);
    }

    protected void applyThemeSize() {
        String size = sizeField.getValue();
        variantsManager.setUserAppThemeSize(size);
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