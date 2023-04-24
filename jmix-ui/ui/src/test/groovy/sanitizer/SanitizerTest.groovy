/*
 * Copyright 2020 Haulmont.
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

package sanitizer

import com.google.common.base.Joiner
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.DialogAction
import io.jmix.ui.component.ContentMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import io.jmix.ui.widget.JmixLabel
import org.springframework.test.context.ContextConfiguration
import sanitizer.screen.SanitizerTestScreen
import test_support.UiTestConfiguration

@SuppressWarnings('GroovyAccessibility')
@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class SanitizerTest extends ScreenSpecification {

    protected static final String UNSAFE_HTML = Joiner.on('\n').join(
            "<p onclick='alert(\"XSS via event handler\")' style='alert(\"XSS via style\")'>",
            "Test Label<script>alert(\"XSS via script\")</script>",
            "</p>",
            "<p>",
            "<a href='javascript:alert(\"XSS via link\")'>Test Link</a>",
            "</p>"
    )

    protected static final String SAFE_HTML = Joiner.on('\n').join(
            "<p>",
            "Test Label",
            "</p>",
            "<p>",
            "Test Link",
            "</p>"
    )

    protected static final String TEST_FONT_ELEMENT = "<font size=\"7\" color=\"#0000ff\" face=\"Verdana\">Test font</font>"

    @Override
    def setup() {
        exportScreensPackages(['sanitizer'])
    }

    def "Sanitize component html caption"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)
        screen.show()

        when: 'Html sanitizer is enabled for component and caption as html is enabled'

        screen.textField.captionAsHtml = true
        screen.textField.htmlSanitizerEnabled = true
        screen.textField.caption = UNSAFE_HTML

        then: 'TextField has a safe html as its caption'

        screen.textField.caption == SAFE_HTML

        when: 'Html sanitizer is disabled for component'

        screen.textField.htmlSanitizerEnabled = false
        screen.textField.caption = UNSAFE_HTML

        then: 'TextField has an unsafe html as its caption'

        screen.textField.caption == UNSAFE_HTML
    }

    def "Sanitize component html description"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)
        screen.show()

        when: 'Html sanitizer is enabled for component and description as html is enabled'

        screen.textField.descriptionAsHtml = true
        screen.textField.htmlSanitizerEnabled = true
        screen.textField.description = UNSAFE_HTML

        then: 'TextField has a safe html as its description'

        screen.textField.description == SAFE_HTML

        when: 'Html sanitizer is disabled for component'

        screen.textField.htmlSanitizerEnabled = false
        screen.textField.description = UNSAFE_HTML

        then: 'TextField has an unsafe html as its description'

        screen.textField.description == UNSAFE_HTML
    }

    def "Sanitize component html context help"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)
        screen.show()

        when: 'Html sanitizer is enabled for component and context help as html is enabled'

        screen.textField.contextHelpTextHtmlEnabled = true
        screen.textField.htmlSanitizerEnabled = true
        screen.textField.contextHelpText = UNSAFE_HTML

        then: 'TextField has a safe html as its context help'

        screen.textField.contextHelpText == SAFE_HTML

        when: 'Html sanitizer is disabled for component'

        screen.textField.htmlSanitizerEnabled = false
        screen.textField.contextHelpText = UNSAFE_HTML

        then: 'TextField has an unsafe html as its context help'

        screen.textField.contextHelpText == UNSAFE_HTML
    }

    def "Sanitize html message of MessageDialog"() {
        when: 'Html sanitizer is enabled for MessageDialog'

        vaadinUi.dialogs.createMessageDialog()
                .withMessage(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(true)
                .show()

        then: 'MessageDialog has a safe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == SAFE_HTML
        }

        when: 'Html sanitizer is disabled for MessageDialog'

        vaadinUi.dialogs.createMessageDialog()
                .withMessage(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(false)
                .show()

        then: 'MessageDialog has an unsafe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == UNSAFE_HTML
        }
    }

    def "Sanitize html message of OptionDialog"() {
        when: 'Html sanitizer is enabled for OptionDialog'

        vaadinUi.dialogs.createOptionDialog()
                .withMessage(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(true)
                .withActions(new DialogAction(DialogAction.Type.OK))
                .show()

        then: 'OptionDialog has a safe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == SAFE_HTML
        }

        when: 'Html sanitizer is disabled for OptionDialog'

        vaadinUi.dialogs.createOptionDialog()
                .withMessage(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(false)
                .withActions(new DialogAction(DialogAction.Type.OK))
                .show()

        then: 'OptionDialog has an unsafe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == UNSAFE_HTML
        }
    }

    def "Sanitize html description of Notification"() {
        when: 'Html sanitizer is enabled for Notification'

        vaadinUi.notifications.create()
                .withDescription(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(true)
                .show()

        then: 'Notification has a safe html as its description'

        vaadinUi.getExtensions().find { extension ->
            extension instanceof Notification &&
                    ((Notification) extension).description == SAFE_HTML
        }

        when: 'Html sanitizer is disabled for Notification'

        vaadinUi.notifications.create()
                .withDescription(UNSAFE_HTML)
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(false)
                .show()

        then: 'Notification has an unsafe html as its description'

        vaadinUi.getExtensions().find { extension ->
            extension instanceof Notification &&
                    ((Notification) extension).description == UNSAFE_HTML
        }
    }

    def "Sanitize html message of MessageDialogFacet with enabled html sanitizer"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'MessageDialog with unsafe html as message is shown'

        screen.messageDialogFacet.message = UNSAFE_HTML
        screen.messageDialogFacet.contentMode = ContentMode.HTML
        screen.messageDialogFacet.htmlSanitizerEnabled = true
        screen.messageDialogFacet.show()

        then: 'MessageDialog has a safe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == SAFE_HTML
        }
    }

    def "Sanitize html message of MessageDialogFacet with disabled html sanitizer"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'MessageDialog with unsafe html as message is shown'

        screen.messageDialogFacet.message = UNSAFE_HTML
        screen.messageDialogFacet.contentMode = ContentMode.HTML
        screen.messageDialogFacet.htmlSanitizerEnabled = false
        screen.messageDialogFacet.show()

        then: 'MessageDialog has an unsafe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == UNSAFE_HTML
        }
    }

    def "Sanitize html message of OptionDialogFacet with enabled html sanitizer"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'OptionDialog with unsafe html as message is shown'

        screen.optionDialogFacet.message = UNSAFE_HTML
        screen.optionDialogFacet.contentMode = ContentMode.HTML
        screen.optionDialogFacet.htmlSanitizerEnabled = true
        screen.optionDialogFacet.show()

        then: 'OptionDialog has a safe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == SAFE_HTML
        }
    }

    def "Sanitize html message of OptionDialogFacet with disabled html sanitizer"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'OptionDialog with unsafe html as message is shown'

        screen.optionDialogFacet.message = UNSAFE_HTML
        screen.optionDialogFacet.contentMode = ContentMode.HTML
        screen.optionDialogFacet.htmlSanitizerEnabled = false
        screen.optionDialogFacet.show()

        then: 'OptionDialog has an unsafe html as its message'

        vaadinUi.windows.find { window ->
            def messageLabel = ((VerticalLayout) window.content).components.first
            ((JmixLabel) messageLabel).value == UNSAFE_HTML
        }
    }

    def "Sanitize html description of NotificationFacet with enabled html sanitizer"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'Notification with unsafe html as description is shown'

        screen.notificationFacet.description = UNSAFE_HTML
        screen.notificationFacet.contentMode = ContentMode.HTML
        screen.notificationFacet.htmlSanitizerEnabled = true
        screen.notificationFacet.show()

        then: 'Notification has a safe html as its description'

        vaadinUi.getExtensions().find { extension ->
            extension instanceof Notification &&
                    ((Notification) extension).description == SAFE_HTML
        }
    }

    def "Sanitize html description of NotificationFacet"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)

        when: 'Notification with unsafe html as description is shown'

        screen.notificationFacet.description = UNSAFE_HTML
        screen.notificationFacet.contentMode = ContentMode.HTML
        screen.notificationFacet.htmlSanitizerEnabled = false
        screen.notificationFacet.show()

        then: 'Notification has an unsafe html as its description'

        vaadinUi.getExtensions().find { extension ->
            extension instanceof Notification &&
                    ((Notification) extension).description == UNSAFE_HTML
        }
    }

    def "Html Sanitizer supports font element attributes inside RichTextArea component"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)
        screen.show()

        when: 'Font element is set to RichTextArea for which the sanitizer is enabled'

        screen.richTextArea.htmlSanitizerEnabled = true
        screen.richTextArea.value = TEST_FONT_ELEMENT

        then: 'RichTextArea value contains font element with all attributes'

        screen.richTextArea.value == TEST_FONT_ELEMENT
    }

    def "Sanitize component html caption depending on the value of the jmix.ui.component.htmlSanitizerEnabled property"() {
        showTestMainScreen()

        def screen = screens.create(SanitizerTestScreen)
        screen.show()

        when: 'Html sanitizer is enabled globally and caption as html is enabled'

        screen.textField.caption = UNSAFE_HTML
        screen.textField.captionAsHtml = true

        then: 'TextField has a safe html as its caption'

        screen.textField.caption == SAFE_HTML

        when: 'Html sanitizer is disabled for component'

        screen.textField.htmlSanitizerEnabled = false
        screen.textField.caption = UNSAFE_HTML

        then: 'TextField has an unsafe html as its caption'

        screen.textField.caption == UNSAFE_HTML
    }
}
