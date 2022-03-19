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

package io.jmix.grapesjs.xml.layout.loader;

import io.jmix.grapesjs.component.GjsPlugin;
import io.jmix.grapesjs.component.GrapesJsNewsletterHtmlEditor;
import io.jmix.grapesjs.plugins.CustomCodeGjsPlugin;
import io.jmix.grapesjs.plugins.NewsletterGjsPlugin;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class GrapesJsNewsletterHtmlEditorLoader extends GrapesJsHtmlEditorLoader {

    @Override
    protected void createResultComponent() {
        resultComponent = factory.create(GrapesJsNewsletterHtmlEditor.class);
    }

    @Override
    protected void setDefaultPlugins() {
        List<GjsPlugin> plugins = new ArrayList<>();
        plugins.add(getPlugin(NewsletterGjsPlugin.XSD_CODE));
        plugins.add(getPlugin(CustomCodeGjsPlugin.XSD_CODE));
        resultComponent.setPlugins(plugins);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();
        loadInlineCss((GrapesJsNewsletterHtmlEditor) resultComponent, element);
    }

    private void loadInlineCss(GrapesJsNewsletterHtmlEditor component, Element element) {
        if (element.attribute("inlineCss") != null) {
            component.setInlineCss(Boolean.valueOf(element.attribute("inlineCss").getValue()));
        } else {
            component.setInlineCss(true);
        }
    }

}
