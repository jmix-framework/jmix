/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.Button;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import static io.jmix.ui.theme.ThemeClassNames.PRIMARY_ACTION;

public class ButtonLoader extends AbstractComponentLoader<Button> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(Button.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        assignXmlDescriptor(resultComponent, element);
        assignFrame(resultComponent);

        loadEnable(resultComponent, element);
        loadVisible(resultComponent, element);

        loadStyleName(resultComponent, element);

        loadHtmlSanitizerEnabled(resultComponent, element);

        loadCaption(resultComponent, element);
        loadCaptionAsHtml(resultComponent, element);
        loadDescription(resultComponent, element);
        loadAction(resultComponent, element);
        loadIcon(resultComponent, element);

        loadWidth(resultComponent, element);
        loadHeight(resultComponent, element);
        loadAlign(resultComponent, element);

        loadTabIndex(resultComponent, element);

        loadShortcut(resultComponent, element);

        loadDisableOnClick(resultComponent, element);
        loadResponsive(resultComponent, element);
        loadCss(resultComponent, element);
        loadPrimary(resultComponent, element);
    }

    protected void loadCaptionAsHtml(Button resultComponent, Element element) {
        String captionAsHtml = element.attributeValue("captionAsHtml");
        if (StringUtils.isNotEmpty(captionAsHtml)) {
            resultComponent.setCaptionAsHtml(Boolean.parseBoolean(captionAsHtml));
        }
    }

    protected void loadShortcut(Button resultComponent, Element element) {
        String shortcut = element.attributeValue("shortcut");
        if (StringUtils.isNotEmpty(shortcut)) {
            resultComponent.setShortcut(loadShortcut(shortcut));
        }
    }

    protected void loadDisableOnClick(Button component, Element element) {
        String disableOnClick = element.attributeValue("disableOnClick");
        if (StringUtils.isNotEmpty(disableOnClick)) {
            component.setDisableOnClick(Boolean.parseBoolean(disableOnClick));
        }
    }

    private void loadPrimary(Button resultComponent, Element element) {
        loadBoolean(element, "primary", primary -> {
            if (primary) {
                resultComponent.addStyleName(PRIMARY_ACTION);
            } else {
                resultComponent.removeStyleName(PRIMARY_ACTION);
            }
        });
    }
}