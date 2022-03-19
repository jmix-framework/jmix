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

package io.jmix.emailtemplatesui.screen.templateblock;


import io.jmix.emailtemplates.entity.TemplateBlock;
import io.jmix.emailtemplates.utils.HtmlTemplateUtils;
import io.jmix.grapesjs.component.GrapesJsNewsletterHtmlEditor;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.TreeMap;

@UiController("emltmp_TemplateBlock.edit")
@UiDescriptor("template-block-edit.xml")
@EditedEntityContainer("templateBlockDc")
public class TemplateBlockEdit extends StandardEditor<TemplateBlock> {

    @Autowired
    private SourceCodeEditor htmlTemplate;

    @Autowired
    protected ComboBox<String> iconLookup;

    @Autowired
    private GrapesJsNewsletterHtmlEditor visualTemplate;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initIconLookup();
    }

    protected void initIconLookup() {
        Map<String, String> optionsMap = new TreeMap<>();
        for (JmixIcon jmixIcon : JmixIcon.values()) {
            if (jmixIcon.source().startsWith("font-icon:")) {
                String caption = jmixIcon.source().substring("font-icon:".length());
                optionsMap.put(caption, jmixIcon.source());
            }
        }
        iconLookup.setOptionsMap(optionsMap);
        iconLookup.setOptionIconProvider(iconName -> iconName);
    }

    @Subscribe("htmlTemplate")
    public void onHtmlTemplateValueChange(HasValue.ValueChangeEvent<String> event) {
        visualTemplate.setValue(htmlTemplate.getValue());
    }

    @Subscribe("visualTemplate")
    public void onTemplateEditorValueChange(HasValue.ValueChangeEvent<String> event) {
        htmlTemplate.setValue(HtmlTemplateUtils.prettyPrintHTML(event.getValue()));
    }


}