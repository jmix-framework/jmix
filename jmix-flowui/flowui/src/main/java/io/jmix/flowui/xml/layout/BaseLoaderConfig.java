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

package io.jmix.flowui.xml.layout;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.xml.layout.loader.component.*;
import io.jmix.flowui.xml.layout.loader.container.*;
import io.jmix.flowui.xml.layout.loader.html.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseLoaderConfig {

    protected Map<String, Class<? extends ComponentLoader<? extends Component>>> loaders = new ConcurrentHashMap<>();

    public BaseLoaderConfig() {
        initStandardLoaders();
    }

    protected void initStandardLoaders() {
        /* Abstract layouts */
        loaders.put("hbox", HorizontalLayoutLoader.class);
        loaders.put("vbox", VerticalLayoutLoader.class);

        /* Content panel */
        loaders.put("accordion", AccordionLoader.class);
        loaders.put("accordionPanel", AccordionLoader.AccordionPanelLoader.class);
        loaders.put("scroller", ScrollerLoader.class);
        loaders.put("tabs", TabsLoader.class);
        loaders.put("tab", TabsLoader.TabLoader.class);
        loaders.put("details", DetailsLoader.class);
        loaders.put("split", SplitLayoutLoader.class);
        loaders.put("formLayout", FormLayoutLoader.class);

        /* Components */
        loaders.put("button", ButtonLoader.class);
        loaders.put("textField", TextFieldLoader.class);
        loaders.put("emailField", EmailFieldLoader.class);
        loaders.put("numberField", NumberFieldLoader.class);
        loaders.put("passwordField", PasswordFieldLoader.class);
        loaders.put("bigDecimalField", BigDecimalFieldLoader.class);
        loaders.put("integerField", IntegerFieldLoader.class);
        loaders.put("progressBar", ProgressBarLoader.class);
        loaders.put("radioButtonGroup", RadioButtonGroupLoader.class);
        loaders.put("checkboxGroup", CheckboxGroupLoader.class);
        loaders.put("listBox", ListBoxLoader.class);
        loaders.put("multiSelectListBox", MultiSelectListBoxLoader.class);
        loaders.put("textArea", TextAreaLoader.class);
        loaders.put("checkbox", CheckboxLoader.class);
        loaders.put("comboBox", ComboBoxLoader.class);
        loaders.put("timePicker", TimePickerLoader.class);
        loaders.put("dateTimePicker", DateTimePickerLoader.class);
        loaders.put("datePicker", DatePickerLoader.class);
        loaders.put("avatar", AvatarLoader.class);
        loaders.put("select", SelectLoader.class);
        loaders.put("valuePicker", ValuePickerLoader.class);
        loaders.put("valuesPicker", ValuesPickerLoader.class);
        loaders.put("entityPicker", EntityPickerLoader.class);
        loaders.put("comboBoxPicker", ComboBoxPickerLoader.class);
        loaders.put("entityComboBox", EntityComboBoxLoader.class);
        loaders.put("listMenu", ListMenuLoader.class);
        loaders.put("drawerToggle", DrawerToggleLoader.class);
        loaders.put("userIndicator", UserIndicatorLoader.class);
        loaders.put("dataGrid", DataGridLoader.class);
        loaders.put("treeDataGrid", TreeDataGridLoader.class);
        loaders.put("loginForm", LoginFormLoader.class);
        loaders.put("loginOverlay", LoginOverlayLoader.class);

        /* HTML components */
        loaders.put("param", ParamLoader.class);
        loaders.put("hr", HrLoader.class);
        loaders.put("input", InputLoader.class);
        loaders.put("image", ImageLoader.class);
        loaders.put("iframe", IFrameLoader.class);

        /* Containers */
        loaders.put("h1", H1Loader.class);
        loaders.put("h2", H2Loader.class);
        loaders.put("h3", H3Loader.class);
        loaders.put("h4", H4Loader.class);
        loaders.put("h5", H5Loader.class);
        loaders.put("h6", H6Loader.class);

        loaders.put("listItem", ListItemLoader.class);
        loaders.put("unorderedList", UnorderedListLoader.class);
        loaders.put("orderedList", OrderedListLoader.class);

        loaders.put("descriptionList", DescriptionListLoader.class);
        loaders.put("term", DescriptionListLoader.TermLoader.class);
        loaders.put("description", DescriptionListLoader.DescriptionLoader.class);

        loaders.put("div", DivLoader.class);
        loaders.put("span", SpanLoader.class);
        loaders.put("section", SectionLoader.class);
        loaders.put("nav", NavLoader.class);
        loaders.put("main", MainLoader.class);
        loaders.put("footer", FooterLoader.class);
        loaders.put("aside", AsideLoader.class);
        loaders.put("emphasis", EmphasisLoader.class);
        loaders.put("article", ArticleLoader.class);
        loaders.put("header", HeaderLoader.class);
        loaders.put("pre", PreLoader.class);
        loaders.put("p", ParagraphLoader.class);
        loaders.put("htmlObject", HtmlObjectLoader.class);
        loaders.put("anchor", AnchorLoader.class);
    }
}
