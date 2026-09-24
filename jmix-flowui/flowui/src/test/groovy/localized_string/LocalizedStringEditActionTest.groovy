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

import io.jmix.core.Metadata
import io.jmix.flowui.Actions
import io.jmix.flowui.OpenedDialogWindows
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.valuepicker.LocalizedStringEditAction
import io.jmix.flowui.app.localizedstring.LocalizedStringEditDialog
import com.vaadin.flow.component.HasValueAndElement
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.textarea.JmixTextArea
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.component.valuepicker.JmixValuePicker
import io.jmix.flowui.kit.component.button.JmixButton
import localized_string.view.LocalizedStringPickerView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.localized_string.LsItem
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(properties = ["jmix.core.available-locales=en,de"])
class LocalizedStringEditActionTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    UiComponents uiComponents
    @Autowired
    Actions actions

    LocalizedStringPickerView view
    JmixValuePicker<String> picker

    void setup() {
        registerViewBasePackages("localized_string.view", "io.jmix.flowui.app")
        view = navigateToView(LocalizedStringPickerView)
        picker = view.plainPicker
    }

    LocalizedStringEditDialog open(JmixValuePicker<String> target) {
        target.getAction('edit').actionPerform(target)
        return currentDialog()
    }

    void typeFromClient(HasValueAndElement<?, String> field, String text) {
        field.element.node.getFeature(ElementPropertyMap).deferredUpdateFromClient('value', text).run()
    }

    LocalizedStringEditDialog currentDialog() {
        applicationContext.getBean(OpenedDialogWindows).currentDialog.orElse(null) as LocalizedStringEditDialog
    }

    void save(LocalizedStringEditDialog dialog) {
        (UiComponentUtils.getComponent(dialog, 'saveAndCloseBtn') as JmixButton).click()
    }

    HasValueAndElement<?, String> defaultField(LocalizedStringEditDialog dialog) {
        UiComponentUtils.getComponent(dialog, LocalizedStringEditDialog.DEFAULT_FIELD_ID) as HasValueAndElement
    }

    HasValueAndElement<?, String> localeField(LocalizedStringEditDialog dialog, String key) {
        UiComponentUtils.getComponent(dialog, LocalizedStringEditDialog.LOCALE_FIELD_ID_PREFIX + key)
                as HasValueAndElement
    }

    /**
     * The locale keys in the order their fields are shown, read from the form itself.
     */
    List<String> localeKeys(LocalizedStringEditDialog dialog) {
        def prefix = LocalizedStringEditDialog.LOCALE_FIELD_ID_PREFIX
        UiComponentUtils.getComponent(dialog, 'fieldsForm').children
                .map { it.id.orElse('') }
                .filter { it.startsWith(prefix) }
                .map { it.substring(prefix.length()) }
                .toList()
    }

    def "a picker not bound to a property shows the text of the current locale"() {
        when:
        picker.value = 'Default role\nen=My role\nde=Meine Rolle'

        then: "without a data binding the picker would format the value as a plain string and show the stored one"
        picker.element.getProperty('value') == 'My role'
    }

    def "the edit action keeps a formatter the picker already has"() {
        given:
        def custom = uiComponents.create(JmixValuePicker)
        custom.formatter = { value -> 'custom' }

        when:
        custom.addAction(actions.create(LocalizedStringEditAction.ID))
        custom.value = 'Default role\nen=My role'

        then:
        custom.element.getProperty('value') == 'custom'
    }

    def "the dialog shows the default value and one field per available locale, and saves the canonical form"() {
        given:
        picker.value = 'Default role\nen=My role'

        when:
        def dialog = open(picker)

        then:
        defaultField(dialog).value == 'Default role'
        localeKeys(dialog) == ['en', 'de']
        localeField(dialog, 'en').value == 'My role'
        localeField(dialog, 'de').value == ''

        when:
        localeField(dialog, 'de').value = 'Meine Rolle'
        save(dialog)

        then:
        picker.value == 'Default role\nen=My role\nde=Meine Rolle'
        currentDialog() == null
    }

    def "a stored locale that is not available gets its own field and is kept"() {
        given: "fr is not among the available locales en,de"
        picker.value = 'Default role\nen=My role\nfr=Mon rôle'

        when:
        def dialog = open(picker)

        then: "the available locales first, then whatever the value carries"
        localeKeys(dialog) == ['en', 'de', 'fr']
        localeField(dialog, 'fr').value == 'Mon rôle'

        when:
        localeField(dialog, 'de').value = 'Meine Rolle'
        save(dialog)

        then: "the stored entries keep their order and the new one is appended"
        picker.value == 'Default role\nen=My role\nfr=Mon rôle\nde=Meine Rolle'
    }

    def "opening and saving without an edit leaves the stored string exactly as it was"() {
        given: "the stored order of the entries differs from the order of the fields"
        picker.value = 'Default role\nde=Meine Rolle\nen=My role'

        when:
        save(open(picker))

        then:
        currentDialog() == null
        picker.value == 'Default role\nde=Meine Rolle\nen=My role'
    }

    def "the editor holds no result until it is saved"() {
        given:
        picker.value = 'Default role'

        when:
        def dialog = open(picker)

        then: "the result is meaningful after SAVE only, so before it there must be nothing that looks like one"
        dialog.value == null
    }

    def "a value that has neither a default value nor the application locale can be saved unchanged"() {
        given: "the application default locale is 'en' and the value carries a de entry only"
        picker.value = '\nde=Bericht'

        when:
        save(open(picker))

        then: "the rule applies to an edit, not to a value that was stored before"
        currentDialog() == null
        picker.value == '\nde=Bericht'
    }

    def "a stored value in a tolerated shape that was not edited is neither validated nor rewritten"() {
        given: "no leading line break and no entry of the application locale: the shape reports and dynattr store"
        picker.value = 'de=Bericht\nfr=Rapport'

        when:
        save(open(picker))

        then: "compared in its canonical form it is unchanged, so the rule it would break does not run"
        currentDialog() == null
        picker.value == 'de=Bericht\nfr=Rapport'
    }

    def "a trailing line break typed into a text area is not stored"() {
        given: "a stored line break switches the fields to text areas"
        picker.value = 'First line\nsecond line'

        when: "typed the way the client sends it, which the field trims"
        def dialog = open(picker)
        typeFromClient(localeField(dialog, 'en'), 'Role\n')
        save(dialog)

        then: "the database ends an entry at a line break, so a stored one would be read back differently"
        picker.value == 'First line\nsecond line\nen=Role'
    }

    def "an untouched stored value with whitespace at an edge is closed unchanged"() {
        given:
        picker.value = stored

        when:
        save(open(picker))

        then: "a value loaded into the fields is not trimmed, so it compares equal to what was stored"
        currentDialog() == null
        picker.value == stored

        where: "an entry ending with a space, which a rule would refuse, and a default ending with a space"
        stored << ['\nde=Bericht ', 'Report \nen=Book']
    }

    def "a target holding a value of another type is reported by the action"() {
        given:
        picker.value = 42

        when:
        picker.getAction('edit').actionPerform(picker)

        then: "instead of a ClassCastException from a cast, and before any dialog is opened"
        def e = thrown(IllegalStateException)
        e.message.contains('String value')
        e.message.contains(Integer.name)
        currentDialog() == null
    }

    def "a stored value that carries line breaks is edited in multi-line fields"() {
        given:
        picker.value = 'First line\nsecond line\nen=One line'

        when:
        def dialog = open(picker)

        then: "a single-line field would drop the line breaks and save the shortened text"
        defaultField(dialog) instanceof JmixTextArea
        localeField(dialog, 'en') instanceof JmixTextArea
    }

    def "a single-line value of a property that is not a LOB is edited in single-line fields"() {
        given:
        picker.value = 'One line\nen=One line'

        when:
        def dialog = open(picker)

        then:
        defaultField(dialog) instanceof TypedTextField
        localeField(dialog, 'en') instanceof TypedTextField
    }

    def "multiline set among the action properties in the view XML gives multi-line fields"() {
        given: "the property the way Studio writes it"
        view.multilinePicker.value = 'One line\nen=One line'

        when:
        def dialog = open(view.multilinePicker)

        then:
        defaultField(dialog) instanceof JmixTextArea
        localeField(dialog, 'en') instanceof JmixTextArea
    }

    def "a @Lob property is edited in multi-line fields"() {
        given:
        view.itemDc.item = metadata.create(LsItem)
        view.lobPicker.value = 'One line\nen=One line'

        when:
        def dialog = open(view.lobPicker)

        then:
        defaultField(dialog) instanceof JmixTextArea
        localeField(dialog, 'en') instanceof JmixTextArea
    }

    def "a required picker refuses to save a value that was empty before"() {
        when:
        def dialog = open(view.requiredPicker)
        save(dialog)

        then: "the rule is about the value the attribute keeps, not about what was edited"
        currentDialog() != null
        view.requiredPicker.value == null
    }

    def "clearing every field writes null, as clearing the picker does"() {
        given:
        picker.value = 'Default role\nde=Meine Rolle'

        when:
        def dialog = open(picker)
        defaultField(dialog).value = ''
        localeField(dialog, 'de').value = ''
        save(dialog)

        then:
        currentDialog() == null
        picker.value == null
    }

    def "a field of an unavailable locale can be cleared"() {
        given:
        picker.value = 'Default role\nfr=Mon rôle'

        when:
        def dialog = open(picker)
        localeField(dialog, 'fr').value = ''
        save(dialog)

        then:
        picker.value == 'Default role'
    }

    def "a message reference combined with locale values is refused"() {
        given:
        picker.value = 'Default role\nde=Meine Rolle'

        when:
        def dialog = open(picker)
        defaultField(dialog).value = 'msg://roles.manager.name'
        save(dialog)

        then:
        currentDialog() != null
        picker.value == 'Default role\nde=Meine Rolle'
    }

    def "clearing a locale field removes its entry"() {
        given:
        picker.value = 'Default role\nen=My role\nde=Meine Rolle'

        when:
        def dialog = open(picker)
        localeField(dialog, 'en').value = ''
        save(dialog)

        then:
        picker.value == 'Default role\nde=Meine Rolle'
    }

    def "a locale value that starts a line with a locale code is reported on its own field"() {
        given:
        picker.value = 'Default role'

        when:
        def dialog = open(picker)
        localeField(dialog, 'en').value = 'Role\nde=x'
        save(dialog)

        then: "the field validator marks the field that holds the offending value, and only it"
        currentDialog() != null
        localeField(dialog, 'en').invalid
        !defaultField(dialog).invalid
        picker.value == 'Default role'
    }

    def "the length limit of the bound property is enforced, including the one only bean validation declares"() {
        given: "shortName carries @Size(max = 12) and no @Column"
        view.itemDc.item = metadata.create(LsItem)
        def bound = view.boundPicker
        bound.value = 'Short'

        when:
        def dialog = open(bound)
        localeField(dialog, 'en').value = 'Way too long to fit'
        save(dialog)

        then:
        currentDialog() != null
        bound.value == 'Short'
    }

    def "the strictest of the declared length limits applies, and a value of exactly the limit fits: #declared"() {
        given:
        view.itemDc.item = metadata.create(LsItem)
        JmixValuePicker<String> bound = view."$pickerId"

        when: "a value of exactly the limit"
        def dialog = open(bound)
        defaultField(dialog).value = 'x' * limit
        save(dialog)

        then:
        currentDialog() == null
        bound.value == 'x' * limit

        when: "a value one character longer"
        dialog = open(bound)
        defaultField(dialog).value = 'x' * (limit + 1)
        save(dialog)

        then:
        currentDialog() != null
        bound.value == 'x' * limit

        where:
        pickerId      | declared                                | limit
        'titlePicker' | 'a column length under a @Size limit'   | 20
        'labelPicker' | 'a @Length limit under a column length' | 15
    }

    def "saving without locale values leaves a plain string"() {
        given:
        picker.value = 'Default role'

        when: "the default value is edited, so that the dialog writes the value, and every locale field stays blank"
        def dialog = open(picker)
        defaultField(dialog).value = 'New role'
        save(dialog)

        then:
        picker.value == 'New role'
    }

    def "a value that starts a line with a locale code is rejected"() {
        given:
        picker.value = 'Default role'

        when:
        def dialog = open(picker)
        defaultField(dialog).value = 'Default role\nen=x'
        save(dialog)

        then:
        currentDialog() != null
        picker.value == 'Default role'
    }

    def "locale values without a default or an application-default-locale value are rejected"() {
        given: "the application default locale is 'en'"
        picker.value = ''

        when:
        def dialog = open(picker)
        localeField(dialog, 'de').value = 'Rolle'
        save(dialog)

        then:
        currentDialog() != null

        when:
        localeField(dialog, 'en').value = 'Role'
        save(dialog)

        then:
        currentDialog() == null
        picker.value == '\nen=Role\nde=Rolle'
    }

    def "a msg:// reference is shown in the default field and the locale fields are read-only"() {
        given:
        picker.value = 'msg://roles.manager.name'

        when:
        def dialog = open(picker)

        then:
        defaultField(dialog).value == 'msg://roles.manager.name'
        localeKeys(dialog).every { localeField(dialog, it).readOnly }

        when:
        save(dialog)

        then:
        picker.value == 'msg://roles.manager.name'
    }
}
