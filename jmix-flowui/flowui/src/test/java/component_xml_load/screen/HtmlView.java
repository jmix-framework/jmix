/*
 * Copyright 2022 Haulmont.
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

package component_xml_load.screen;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "html-view")
@ViewController("HtmlView")
@ViewDescriptor("html-view.xml")
public class HtmlView extends StandardView {

    //Containers
    @ViewComponent
    public Article articleId;

    @ViewComponent
    public Aside asideId;

    @ViewComponent
    public DescriptionList descriptionListId;

    @ViewComponent
    public DescriptionList.Term termId;

    @ViewComponent
    public DescriptionList.Description descriptionId;

    @ViewComponent
    public Div divId;

    @ViewComponent
    public Emphasis emphasisId;

    @ViewComponent
    public Footer footerId;

    @ViewComponent
    public H1 h1Id;

    @ViewComponent
    public H2 h2Id;

    @ViewComponent
    public H3 h3Id;

    @ViewComponent
    public H4 h4Id;

    @ViewComponent
    public H5 h5Id;

    @ViewComponent
    public H6 h6Id;

    @ViewComponent
    public Header headerId;

    @ViewComponent
    public ListItem listItemId;

    @ViewComponent
    public Paragraph pId;

    @ViewComponent
    public Pre preId;

    @ViewComponent
    public Section sectionId;

    @ViewComponent
    public Span spanId;

    @ViewComponent
    public UnorderedList unorderedListId;

    @ViewComponent
    public Anchor anchorId;

    @ViewComponent
    public HtmlObject htmlObjectId;

    @ViewComponent
    public Image imageId;

    @ViewComponent
    public Main mainId;

    @ViewComponent
    public Nav navId;

    @ViewComponent
    public OrderedList orderedListId;

    //Components
    @ViewComponent
    public Hr hrId;

    @ViewComponent
    public IFrame iframeId;

    @ViewComponent
    public Input inputId;

    @ViewComponent
    public Param paramId;
}
