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

package io.jmix.grapesjs.component.impl;

import io.jmix.core.Resources;
import io.jmix.grapesjs.component.GjsPlugin;
import io.jmix.grapesjs.component.GjsPluginsRepository;
import io.jmix.grapesjs.plugins.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component("grpjs_GjsPluginsRepository")
public class GjsPluginsRepositoryImpl implements GjsPluginsRepository {
    @Autowired
    protected Resources resources;

    protected Map<String, GjsPlugin> registeredPlugins = new HashMap<>();

    @PostConstruct
    protected void registerDefaultPlugins() {
        registeredPlugins.put(CKEditorGjsPlugin.XSD_CODE, new CKEditorGjsPlugin(getOptions("gjs-plugin-ckeditor.js")));
        registeredPlugins.put(CustomCodeGjsPlugin.XSD_CODE, new CustomCodeGjsPlugin());
        registeredPlugins.put(NewsletterGjsPlugin.XSD_CODE, new NewsletterGjsPlugin(getOptions("gjs-preset-newsletter.js")));
        registeredPlugins.put(PostCssGjsPlugin.XSD_CODE, new PostCssGjsPlugin());
        registeredPlugins.put(TabsGjsPlugin.XSD_CODE, new TabsGjsPlugin(getOptions("grapesjs-tabs.js")));
        registeredPlugins.put(WebpageGjsPlugin.XSD_CODE, new WebpageGjsPlugin(getOptions("gjs-preset-webpage.js")));
        registeredPlugins.put(BasicBlocksGjsPlugin.XSD_CODE, new BasicBlocksGjsPlugin());
        registeredPlugins.put(FlexBlocksGjsPlugin.XSD_CODE, new FlexBlocksGjsPlugin());
        registeredPlugins.put(TuiImageEditorGjsPlugin.XSD_CODE, new TuiImageEditorGjsPlugin(getOptions("gjs-tui-image-editor.js")));
        registeredPlugins.put(FormsGjsPlugin.XSD_CODE,  new FormsGjsPlugin());
        registeredPlugins.put(StyleFilterGjsPlugin.XSD_CODE, new StyleFilterGjsPlugin());
        registeredPlugins.put(TooltipGjsPlugin.XSD_CODE, new TooltipGjsPlugin());
    }

    @Override
    public GjsPlugin getPlugin(String pluginXsdCode) {
        return registeredPlugins.get(pluginXsdCode).clone();
    }

    @Override
    public void registerPlugin(String pluginXsdCode, GjsPlugin plugin) {
        registeredPlugins.put(pluginXsdCode, plugin.clone());
    }

    protected String getOptions(String optionsFile) {
        return resources.getResourceAsString(String.format("/io/jmix/grapesjs/plugins/%s", optionsFile));
    }
}
