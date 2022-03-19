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

package io.jmix.dashboardsui.screen.dashboard.editor.style;

import io.jmix.core.Messages;
import io.jmix.dashboards.model.visualmodel.DashboardLayout;
import io.jmix.dashboards.model.visualmodel.SizeUnit;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.ui.component.Window.CLOSE_ACTION_ID;
import static io.jmix.ui.component.Window.COMMIT_ACTION_ID;

@UiController("dshbrd_Style.dialog")
@UiDescriptor("style-dialog.xml")
public class StyleDialog extends Screen {

    private static final Pattern DIMENSION_PATTERN = Pattern.compile("^(\\d+)(px|%)$");

    public static final String WIDGET = "WIDGET";

    @Autowired
    private TextField<String> styleName;

    @Autowired
    private TextField<Integer> height;
    @Autowired
    private TextField<Integer> width;
    @Autowired
    private ComboBox<SizeUnit> widthUnits;
    @Autowired
    private ComboBox<SizeUnit> heightUnits;
    @Autowired
    private CheckBox autoHeight;
    @Autowired
    private CheckBox autoWidth;
    @Autowired
    private Messages messages;

    @Autowired
    private ScreenValidation screenValidation;

    @Subscribe
    public void onInit(InitEvent event) {
        MapScreenOptions options = (MapScreenOptions) event.getOptions();
        Map<String, Object> params = options.getParams();

        DashboardLayout dashboardLayout = (DashboardLayout) params.get(WIDGET);
        styleName.setValue(dashboardLayout.getStyleName());

        initWidthComponents(dashboardLayout);
        initHeightComponents(dashboardLayout);
    }

    protected void initHeightComponents(DashboardLayout dashboardLayout) {
        height.setValue(dashboardLayout.getHeight());
        heightUnits.setOptionsMap(getSizeUnitsValues());
        heightUnits.setValue(dashboardLayout.getHeightUnit());

        height.addTextChangeListener(e -> {
            String text = e.getText();
            checkFieldInput(text, height, heightUnits);
            setSizeAutoVisible(autoHeight, height, heightUnits);
        });

        setSizeAutoVisible(autoHeight, height, heightUnits);
        addSizeAutoFieldListener(autoHeight, height, heightUnits);
    }

    protected void initWidthComponents(DashboardLayout dashboardLayout) {
        width.setValue(dashboardLayout.getWidth());
        widthUnits.setOptionsMap(getSizeUnitsValues());
        widthUnits.setValue(dashboardLayout.getWidthUnit());

        width.addTextChangeListener(e -> {
            String text = e.getText();
            checkFieldInput(text, width, widthUnits);
            setSizeAutoVisible(autoWidth, width, widthUnits);
        });

        setSizeAutoVisible(autoWidth, width, widthUnits);
        addSizeAutoFieldListener(autoWidth, width, widthUnits);
    }

    private void addSizeAutoFieldListener(CheckBox sizeAutoCheckbox, TextField<Integer> field, ComboBox<SizeUnit> unitsField) {
        sizeAutoCheckbox.addValueChangeListener(e -> {
            Boolean value = e.getValue();
            if (BooleanUtils.isTrue(value)) {
                field.setValue(-1);
                unitsField.setValue(SizeUnit.PIXELS);
            } else {
                field.setValue(100);
                unitsField.setValue(SizeUnit.PERCENTAGE);
            }
            field.setVisible(!value);
            unitsField.setVisible(!value);
        });
    }

    private void setSizeAutoVisible(CheckBox sizeAutoCheckbox, TextField field, ComboBox unitsField) {
        boolean autoSize = isAutoSize(field, unitsField);
        field.setVisible(!autoSize);
        unitsField.setVisible(!autoSize);
        sizeAutoCheckbox.setValue(autoSize);
    }

    private boolean isAutoSize(TextField field, ComboBox unitsField) {
        return field.getValue() != null && field.getValue().equals(-1) && SizeUnit.PIXELS == unitsField.getValue();
    }

    private void checkFieldInput(String text, TextField<Integer> field, ComboBox<SizeUnit> unitsField) {
        Matcher matcher = DIMENSION_PATTERN.matcher(text);
        if (matcher.matches()) {
            String value = matcher.group(1);
            SizeUnit sizeUnit = SizeUnit.fromId(matcher.group(2));
            field.setValue(Integer.parseInt(value));
            unitsField.setValue(sizeUnit);
        }
    }

    protected Map<String, SizeUnit> getSizeUnitsValues() {
        Map<String, SizeUnit> sizeUnitsMap = new HashMap<>();
        for (SizeUnit sizeUnit : SizeUnit.values()) {
            sizeUnitsMap.put(messages.getMessage(sizeUnit), sizeUnit);
        }
        return sizeUnitsMap;
    }

    public String getLayoutStyleName() {
        return styleName.getValue();
    }

    public Integer getLayoutHeight() {
        return height.getValue();
    }

    public SizeUnit getLayoutHeightUnit() {
        return heightUnits.getValue();
    }

    public Integer getLayoutWidth() {
        return width.getValue();
    }

    public SizeUnit getLayoutWidthUnit() {
        return widthUnits.getValue();
    }

    @Subscribe("okBtn")
    public void apply(Button.ClickEvent event) {
        ValidationErrors errors = screenValidation.validateUiComponents(getWindow());
        if (errors.isEmpty()) {
            this.close(new StandardCloseAction(COMMIT_ACTION_ID));
        } else {
            screenValidation.showValidationErrors(getWindow().getFrameOwner(), errors);
        }
    }

    @Subscribe("cancelBtn")
    public void cancel(Button.ClickEvent event) {
        this.close(new StandardCloseAction(CLOSE_ACTION_ID));
    }
}