/*
 * Copyright 2026 Haulmont.
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

package app_settings_view;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import io.jmix.appsettingsflowui.view.appsettings.AppSettingsEntityView;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AppSettingsFlowuiTestConfiguration;
import test_support.entity.TestSettings;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UiTest(viewBasePackages = "io.jmix.appsettingsflowui.view")
@SpringBootTest(classes = {AppSettingsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class AppSettingsEntityViewTest {

    @Autowired
    ViewNavigationSupport viewNavigationSupport;
    @Autowired
    Metadata metadata;

    @Test
    void propertyCaption_longCaption_shownInFull() {
        viewNavigationSupport.navigate(AppSettingsEntityView.class);
        AppSettingsEntityView view = UiTestUtils.getCurrentView();

        ComboBox<MetaClass> entitySelector = UiTestUtils.getComponent(view, "entitySelector");
        entitySelector.setValue(metadata.getClass(TestSettings.class));

        Scroller fieldsScroller = UiTestUtils.getComponent(view, "fieldsScroller");
        FormLayout formLayout = (FormLayout) Objects.requireNonNull(fieldsScroller.getContent());
        List<String> captions = formLayout.getChildren()
                .filter(Span.class::isInstance)
                .map(component -> ((Span) component).getText())
                .toList();

        assertEquals(List.of("Check for spelling errors when you type text on web pages"), captions);
    }
}
