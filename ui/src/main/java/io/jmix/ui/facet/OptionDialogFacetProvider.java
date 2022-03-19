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
import io.jmix.ui.component.ActionsAwareDialogFacet;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.OptionDialogFacet;
import io.jmix.ui.component.WindowMode;
import io.jmix.ui.component.impl.OptionDialogFacetImpl;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.theme.ThemeConstants;
import io.jmix.ui.theme.ThemeConstantsManager;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.jmix.ui.icon.Icons.ICON_NAME_REGEX;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("ui_OptionDialogFacetProvider")
public class OptionDialogFacetProvider
        implements FacetProvider<OptionDialogFacet> {

    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected Icons icons;
    @Autowired
    protected ThemeConstantsManager themeConstantsManager;
    @Autowired
    protected UiComponentProperties componentProperties;
    @Autowired
    protected LoaderSupport loaderSupport;

    @Override
    public Class<OptionDialogFacet> getFacetClass() {
        return OptionDialogFacet.class;
    }

    @Override
    public OptionDialogFacet create() {
        return new OptionDialogFacetImpl();
    }

    @Override
    public String getFacetTag() {
        return "optionDialog";
    }

    @Override
    public void loadFromXml(OptionDialogFacet facet, Element element,
                            ComponentLoader.ComponentContext context) {
        loadId(facet, element);
        loadCaption(facet, element, context);
        loadMessage(facet, element, context);

        loadWidth(facet, element);
        loadHeight(facet, element);

        loadContentMode(facet, element);
        loadStyleName(facet, element);

        loaderSupport.loadEnum(element, WindowMode.class, "windowMode")
                .ifPresent(facet::setWindowMode);

        loadHtmlSanitizerEnabled(facet, element);

        loadTarget(facet, element, context);

        loadActions(facet, element, context);
    }

    protected void loadId(OptionDialogFacet facet, Element element) {
        String id = element.attributeValue("id");
        if (isNotEmpty(id)) {
            facet.setId(id);
        }
    }

    protected void loadCaption(OptionDialogFacet facet, Element element,
                               ComponentLoader.ComponentContext context) {
        String caption = element.attributeValue("caption");
        if (isNotEmpty(caption)) {
            facet.setCaption(loadResourceString(context, caption));
        }
    }

    protected void loadMessage(OptionDialogFacet facet, Element element,
                               ComponentLoader.ComponentContext context) {
        String message = element.attributeValue("message");
        if (isNotEmpty(message)) {
            facet.setMessage(loadResourceString(context, message));
        }
    }

    protected void loadWidth(OptionDialogFacet facet, Element element) {
        String width = element.attributeValue("width");
        if (isNotEmpty(width)) {
            facet.setWidth(width);
        }
    }

    protected void loadHeight(OptionDialogFacet facet, Element element) {
        String height = element.attributeValue("height");
        if (isNotEmpty(height)) {
            facet.setHeight(height);
        }
    }

    protected void loadContentMode(OptionDialogFacet facet, Element element) {
        String contentMode = element.attributeValue("contentMode");
        if (isNotEmpty(contentMode)) {
            facet.setContentMode(ContentMode.valueOf(contentMode));
        }
    }

    protected void loadStyleName(OptionDialogFacet facet, Element element) {
        String styleName = element.attributeValue("stylename");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }

    protected void loadTarget(OptionDialogFacet facet, Element element,
                              ComponentLoader.ComponentContext context) {
        String actionTarget = element.attributeValue("onAction");
        String buttonTarget = element.attributeValue("onButton");

        if (isNotEmpty(actionTarget) && isNotEmpty(buttonTarget)) {
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

    protected void loadActions(ActionsAwareDialogFacet facet, Element element,
                               ComponentLoader.ComponentContext context) {
        Element actionsEl = element.element("actions");
        if (actionsEl == null) {
            return;
        }

        List<Element> actionElements = actionsEl.elements("action");

        List<ActionsAwareDialogFacet.DialogAction> actions = new ArrayList<>(actionElements.size());
        for (Element actionElement : actionElements) {
            actions.add(loadAction(actionElement, context));
        }

        facet.setActions(actions);
    }

    protected ActionsAwareDialogFacet.DialogAction loadAction(Element element,
                                                              ComponentLoader.ComponentContext context) {
        String id = element.attributeValue("id");
        String caption = loadResourceString(context, element.attributeValue("caption"));
        String description = loadResourceString(context, element.attributeValue("description"));
        String icon = getIconPath(context, element.attributeValue("icon"));
        boolean primary = Boolean.parseBoolean(element.attributeValue("primary"));

        return new ActionsAwareDialogFacet.DialogAction(id, caption, description, icon, primary);
    }

    protected void loadHtmlSanitizerEnabled(OptionDialogFacet facet, Element element) {
        String htmlSanitizerEnabledString = element.attributeValue("htmlSanitizerEnabled");
        boolean htmlSanitizerEnabled = isNotEmpty(htmlSanitizerEnabledString)
                ? Boolean.parseBoolean(htmlSanitizerEnabledString)
                : componentProperties.isHtmlSanitizerEnabled();

        facet.setHtmlSanitizerEnabled(htmlSanitizerEnabled);
    }

    @Nullable
    protected String loadResourceString(ComponentLoader.ComponentContext context, @Nullable String caption) {
        if (isEmpty(caption)) {
            return caption;
        }

        Class screenClass = context.getFrame()
                .getFrameOwner()
                .getClass();

        return messageTools.loadString(screenClass.getPackage().getName(), caption);
    }

    @Nullable
    protected String getIconPath(ComponentLoader.ComponentContext context, @Nullable String icon) {
        if (icon == null || icon.isEmpty()) {
            return null;
        }

        String iconPath = null;

        if (ICON_NAME_REGEX.matcher(icon).matches()) {
            iconPath = icons.get(icon);
        }

        if (isEmpty(iconPath)) {
            String themeValue = loadThemeString(icon);
            iconPath = loadResourceString(context, themeValue);
        }

        return iconPath;
    }

    @Nullable
    protected String loadThemeString(@Nullable String value) {
        if (value != null
                && value.startsWith(ThemeConstants.PREFIX)) {
            value = themeConstantsManager.getConstants()
                    .get(value.substring(ThemeConstants.PREFIX.length()));
        }
        return value;
    }
}
