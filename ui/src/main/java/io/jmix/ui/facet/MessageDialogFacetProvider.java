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

package io.jmix.ui.facet;

import io.jmix.core.MessageTools;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.MessageDialogFacet;
import io.jmix.ui.component.WindowMode;
import io.jmix.ui.component.impl.MessageDialogFacetImpl;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("ui_MessageDialogFacetProvider")
public class MessageDialogFacetProvider implements FacetProvider<MessageDialogFacet> {

    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected UiComponentProperties componentProperties;
    @Autowired
    protected LoaderSupport loaderSupport;

    @Override
    public Class<MessageDialogFacet> getFacetClass() {
        return MessageDialogFacet.class;
    }

    @Override
    public MessageDialogFacet create() {
        return new MessageDialogFacetImpl();
    }

    @Override
    public String getFacetTag() {
        return "messageDialog";
    }

    @Override
    public void loadFromXml(MessageDialogFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadCaption(facet, element, context);
        loadMessage(facet, element, context);

        loadWidth(facet, element);
        loadHeight(facet, element);

        loadContentMode(facet, element);

        loaderSupport.loadEnum(element, WindowMode.class, "windowMode")
                .ifPresent(facet::setWindowMode);

        loadModal(facet, element);
        loadStyleName(facet, element);
        loadCloseOnClickOutside(facet, element);

        loadHtmlSanitizerEnabled(facet, element);

        loadTarget(facet, element, context);
    }

    protected void loadId(MessageDialogFacet facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(MessageDialogFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context, caption));
        }
    }

    protected void loadMessage(MessageDialogFacet facet, Element element, ComponentLoader.ComponentContext context) {
        String message = element.attributeValue("message");
        if (isNotEmpty(message)) {
            facet.setMessage(loadResourceString(context, message));
        }
    }

    protected void loadWidth(MessageDialogFacet facet, Element element) {
        String width = element.attributeValue("width");
        if (isNotEmpty(width)) {
            facet.setWidth(width);
        }
    }

    protected void loadHeight(MessageDialogFacet facet, Element element) {
        String height = element.attributeValue("height");
        if (isNotEmpty(height)) {
            facet.setHeight(height);
        }
    }

    protected void loadContentMode(MessageDialogFacet facet, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (isNotEmpty(contentMode)) {
            facet.setContentMode(ContentMode.valueOf(contentMode));
        }
    }

    protected void loadModal(MessageDialogFacet facet, Element element) {
        String modal = element.attributeValue("modal");
        if (isNotEmpty(modal)) {
            facet.setModal(Boolean.parseBoolean(modal));
        }

    }

    protected void loadStyleName(MessageDialogFacet facet, Element element) {
        String styleName = element.attributeValue("stylename");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }

    protected void loadTarget(MessageDialogFacet facet, Element element,
                              ComponentLoader.ComponentContext context) {
        String actionTarget = element.attributeValue("onAction");
        String buttonTarget = element.attributeValue("onButton");

        if (isNotEmpty(actionTarget)
                && isNotEmpty(buttonTarget)) {
            throw new GuiDevelopmentException(
                    "Dialog facet should have either action or button target",
                    context);
        }

        if (isNotEmpty(actionTarget)) {
            facet.setActionTarget(actionTarget);
        } else if (isNotEmpty(buttonTarget)) {
            facet.setButtonTarget(buttonTarget);
        }
    }

    protected void loadCloseOnClickOutside(MessageDialogFacet facet, Element element) {
        String closeOnClickOutside = element.attributeValue("closeOnClickOutside");
        if (isNotEmpty(closeOnClickOutside)) {
            facet.setCloseOnClickOutside(Boolean.parseBoolean(closeOnClickOutside));
        }
    }

    protected void loadHtmlSanitizerEnabled(MessageDialogFacet facet, Element element) {
        String htmlSanitizerEnabledString = element.attributeValue("htmlSanitizerEnabled");
        boolean htmlSanitizerEnabled = isNotEmpty(htmlSanitizerEnabledString)
                ? Boolean.parseBoolean(htmlSanitizerEnabledString)
                : componentProperties.isHtmlSanitizerEnabled();

        facet.setHtmlSanitizerEnabled(htmlSanitizerEnabled);
    }

    protected String loadResourceString(ComponentLoader.ComponentContext context, String caption) {
        if (isEmpty(caption)) {
            return caption;
        }

        Class screenClass = context.getFrame()
                .getFrameOwner()
                .getClass();

        return messageTools.loadString(screenClass.getPackage().getName(), caption);
    }
}
