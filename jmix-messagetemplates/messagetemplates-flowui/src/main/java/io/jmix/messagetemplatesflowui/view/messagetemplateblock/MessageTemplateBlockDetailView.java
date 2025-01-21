/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplateblock;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplateBlock;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;

import java.util.Locale;
import java.util.stream.Stream;

@Route(value = "msgtmp/messagetemplateblock/:id", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplateBlock.detail")
@ViewDescriptor("message-template-block-detail-view.xml")
@EditedEntityContainer("messageTemplateBlockDc")
public class MessageTemplateBlockDetailView extends StandardDetailView<MessageTemplateBlock> {

    @ViewComponent
    protected GrapesJs grapesJsEditor;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        grapesJsEditor.setValue(getEditedEntity().getContent());
        grapesJsEditor.addValueChangeEventListener(this::onGrapesJsValueChange);
    }

    @Install(to = "iconsComboBox", subject = "itemsFetchCallback")
    protected Stream<String> iconsComboBoxItemsFetchCallback(Query<VaadinIcon, String> query) {
        String enteredValue = query.getFilter()
                .map(value -> value.replace('-', '_'))
                .map(String::toUpperCase)
                .orElse("");

        return Stream.of(VaadinIcon.values())
                .filter(icon -> icon.name().contains(enteredValue))
                .map(this::iconNameMapper)
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    @Supply(to = "iconsComboBox", subject = "renderer")
    protected Renderer<String> iconsComboBoxRenderer() {
        return new ComponentRenderer<>(this::iconRenderer);
    }

    protected void onGrapesJsValueChange(GrapesJs.GrapesJsValueChangedEvent event) {
        getEditedEntity().setContent(event.getValue());
    }

    protected Div iconRenderer(String iconName) {
        Div div = new Div();
        div.addClassNames(
                LumoUtility.Gap.SMALL,
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER
        );

        Icon icon = new Icon(iconName);
        icon.addClassNames(LumoUtility.Margin.Start.SMALL);

        Text text = new Text(iconName);

        div.add(icon, text);

        return div;
    }

    protected String iconNameMapper(VaadinIcon icon) {
        String name = icon.name().toLowerCase(Locale.ENGLISH)
                .replace('_', '-');
        return "vaadin:%s".formatted(name);
    }

    @Subscribe
    public void onValidation(ValidationEvent event) {
        if (getEditedEntity().getContent() == null) {
            event.addErrors(ValidationErrors.of(
                    messageBundle.getMessage("emptyContentValidationMessage")
            ));
        }
    }
}
