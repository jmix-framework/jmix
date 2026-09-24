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

package localized_string

import com.vaadin.flow.component.HasValueAndElement
import io.jmix.core.Metadata
import io.jmix.flowui.OpenedDialogWindows
import io.jmix.flowui.app.localizedstring.LocalizedStringEditDialog
import io.jmix.flowui.component.ComponentGenerationContext
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.UiComponentsGenerator
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.kit.component.button.JmixButton
import io.jmix.flowui.view.template.impl.ComponentXmlFactory
import localized_string.view.LocalizedStringPickerView
import org.springframework.beans.factory.annotation.Autowired
import org.dom4j.DocumentHelper
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.localized_string.LsItem
import test_support.spec.FlowuiTestSpecification

/**
 * An application with a single locale, which is not English, so that the application locale is not mistaken
 * for a default.
 */
@SpringBootTest(properties = ["jmix.core.available-locales=de"])
class LocalizedStringSingleLocaleTest extends FlowuiTestSpecification {

    @Autowired
    UiComponentsGenerator uiComponentsGenerator
    @Autowired
    Metadata metadata
    @Autowired
    ComponentXmlFactory componentXmlFactory

    void setup() {
        registerViewBasePackages("localized_string.view", "io.jmix.flowui.app")
    }

    LocalizedStringEditDialog currentDialog() {
        applicationContext.getBean(OpenedDialogWindows).currentDialog.orElse(null) as LocalizedStringEditDialog
    }

    HasValueAndElement<?, String> field(LocalizedStringEditDialog dialog, String id) {
        UiComponentUtils.getComponent(dialog, id) as HasValueAndElement
    }

    def "a localized property gets the plain field, because there is nothing to localize"() {
        when:
        def component = uiComponentsGenerator.generate(new ComponentGenerationContext(metadata.getClass(LsItem), 'name'))

        then:
        component instanceof TypedTextField
    }

    def "a view template gives a localized property the plain field as well"() {
        when:
        def xml = componentXmlFactory.createComponentXml(metadata.getClass(LsItem).getProperty(property), null)

        then:
        DocumentHelper.parseText(xml).rootElement.name == element

        where:
        property      || element
        'name'        || 'textField'
        'description' || 'textArea'
    }

    def "the editor still shows a field for the single locale, so the value it requires can be entered"() {
        given: "the default value cleared, the rule demands a value of the application locale"
        def view = navigateToView(LocalizedStringPickerView)
        view.plainPicker.value = 'Standard\nfr=Rapport'

        when:
        view.plainPicker.getAction('edit').actionPerform(view.plainPicker)
        def dialog = currentDialog()
        field(dialog, LocalizedStringEditDialog.DEFAULT_FIELD_ID).value = ''
        field(dialog, LocalizedStringEditDialog.LOCALE_FIELD_ID_PREFIX + 'de').value = 'Bericht'
        (UiComponentUtils.getComponent(dialog, 'saveAndCloseBtn') as JmixButton).click()

        then:
        currentDialog() == null
        view.plainPicker.value == '\nfr=Rapport\nde=Bericht'
    }
}
