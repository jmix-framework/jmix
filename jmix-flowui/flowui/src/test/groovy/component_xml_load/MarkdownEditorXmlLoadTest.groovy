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

package component_xml_load

import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.MarkdownEditorView
import io.jmix.core.Messages
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.markdowneditor.MarkdownEditor
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.kit.component.markdowneditor.JmixMarkdownEditor.ModeChangedEvent
import io.jmix.flowui.kit.component.markdowneditor.MarkdownEditorMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MarkdownEditorXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    Messages messages

    @Autowired
    UiComponents uiComponents

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load markdownEditor attributes from XML"() {
        when: "Open the MarkdownEditorView"
        def view = navigateToView(MarkdownEditorView)

        then: "MarkdownEditor attributes will be loaded"
        verifyAll(view.markdownEditor) {
            id.get() == "markdownEditor"
            ariaLabel.orElse(null) == "ariaLabelString"
            ariaLabelledBy.orElse(null) == "ariaLabelledByString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            !enabled
            errorMessage == "errorMessageString"
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            mode == MarkdownEditorMode.PREVIEW
            placeholder == "placeholderString"
            readOnly
            required
            requiredMessage == "requiredMessageString"
            tabIndex == 3
            themeNames.containsAll(["toolbar-align-center", "helper-above-field"])
            valueChangeMode == ValueChangeMode.TIMEOUT
            valueChangeTimeout == 50
            !visible
            width == "100px"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load markdownEditor data binding from XML"() {
        given: "A MarkdownEditor bound to an entity property"
        def view = navigateToView(MarkdownEditorView)
        def order = view.orderDc.item

        expect: "MarkdownEditor will be bound to the container property"
        verifyAll(view.dataBoundMarkdownEditor) {
            id.get() == "dataBoundMarkdownEditor"
            value == order.number
            valueSource instanceof ContainerValueSource
            (valueSource as ContainerValueSource).item == order
            (valueSource as ContainerValueSource).metaPropertyPath.toPathString() == "number"
        }

        when: "Change an entity value"
        order.number = "new value"

        then: "MarkdownEditor stores the same value"
        view.dataBoundMarkdownEditor.value == order.number
    }

    def "Set null value directly"() {
        given: "A MarkdownEditor"
        def markdownEditor = uiComponents.create(MarkdownEditor)
        markdownEditor.value = "## Markdown value"

        when: "Set null value directly"
        markdownEditor.value = null

        then: "MarkdownEditor accepts null value"
        noExceptionThrown()
        markdownEditor.value == null
        markdownEditor.empty
    }

    def "Set null value through bound entity"() {
        given: "A MarkdownEditor bound to an entity property"
        def view = navigateToView(MarkdownEditorView)

        expect: "The initial value is loaded from the entity"
        view.orderDc.item.number == "## Markdown value"
        view.dataBoundMarkdownEditor.value == "## Markdown value"

        when: "Set null value to the bound entity"
        view.orderDc.item.number = null

        then: "MarkdownEditor accepts null from the bound entity"
        noExceptionThrown()
        view.orderDc.item.number == null
        view.dataBoundMarkdownEditor.value == null
        view.dataBoundMarkdownEditor.empty
    }

    def "Set null value through bound MarkdownEditor"() {
        given: "A MarkdownEditor bound to an entity property"
        def view = navigateToView(MarkdownEditorView)

        expect: "The initial value is loaded from the entity"
        view.orderDc.item.number == "## Markdown value"
        view.dataBoundMarkdownEditor.value == "## Markdown value"

        when: "Set null value to the bound component"
        view.dataBoundMarkdownEditor.value = null

        then: "MarkdownEditor accepts null from the bound entity"
        noExceptionThrown()
        view.orderDc.item.number == null
        view.dataBoundMarkdownEditor.value == null
        view.dataBoundMarkdownEditor.empty
    }

    def "MarkdownEditor fires mode change event"() {
        given: "A MarkdownEditor loaded from XML"
        def view = navigateToView(MarkdownEditorView)
        def events = []

        view.markdownEditor.addModeChangedListener { ModeChangedEvent event ->
            events.add(event)
        }

        when: "The editor mode changes"
        view.markdownEditor.mode = MarkdownEditorMode.EDIT

        then: "A mode change event is fired with the current mode"
        events.size() == 1
        events[0].source == view.markdownEditor
        events[0].mode == MarkdownEditorMode.EDIT
        !events[0].fromClient
    }

    def "MarkdownEditor initializes i18n from messages"() {
        when: "Open the MarkdownEditorView"
        def view = navigateToView(MarkdownEditorView)

        then: "MarkdownEditor i18n values will be initialized"
        verifyAll(view.markdownEditor.i18n.tabs) {
            edit == message("markdownEditor.i18n.tabs.edit")
            preview == message("markdownEditor.i18n.tabs.preview")
        }

        verifyAll(view.markdownEditor.i18n.toolbar) {
            accessibleLabel == message("markdownEditor.i18n.toolbar.accessibleLabel")
            heading == message("markdownEditor.i18n.toolbar.heading")
            bold == message("markdownEditor.i18n.toolbar.bold")
            italic == message("markdownEditor.i18n.toolbar.italic")
            quote == message("markdownEditor.i18n.toolbar.quote")
            code == message("markdownEditor.i18n.toolbar.code")
            link == message("markdownEditor.i18n.toolbar.link")
            unorderedList == message("markdownEditor.i18n.toolbar.unorderedList")
            orderedList == message("markdownEditor.i18n.toolbar.orderedList")
            taskList == message("markdownEditor.i18n.toolbar.taskList")
            overflow == message("markdownEditor.i18n.toolbar.overflow")
        }
    }

    protected String message(String key) {
        return messages.getMessage(key)
    }
}
