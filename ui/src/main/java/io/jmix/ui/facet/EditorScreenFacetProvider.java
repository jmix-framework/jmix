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

import org.springframework.context.ApplicationContext;
import io.jmix.core.Metadata;
import io.jmix.ui.builder.EditMode;
import io.jmix.ui.component.EditorScreenFacet;
import io.jmix.ui.component.impl.EditorScreenFacetImpl;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("ui_EditorScreenFacetProvider")
public class EditorScreenFacetProvider
        extends AbstractEntityAwareScreenFacetProvider<EditorScreenFacet> {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Class<EditorScreenFacet> getFacetClass() {
        return EditorScreenFacet.class;
    }

    @Override
    public EditorScreenFacet create() {
        EditorScreenFacetImpl editorScreenFacet = new EditorScreenFacetImpl();
        editorScreenFacet.setApplicationContext(applicationContext);
        return editorScreenFacet;
    }

    @Override
    public String getFacetTag() {
        return "editorScreen";
    }

    @Override
    public void loadFromXml(EditorScreenFacet facet, Element element,
                            ComponentLoader.ComponentContext context) {
        super.loadFromXml(facet, element, context);

        loadEditMode(facet, element);
        loadAddFirst(facet, element);
    }

    @Override
    protected Metadata getMetadata() {
        return metadata;
    }

    protected void loadAddFirst(EditorScreenFacet facet, Element element) {
        String addFirst = element.attributeValue("addFirst");
        if (isNotEmpty(addFirst)) {
            facet.setAddFirst(Boolean.parseBoolean(addFirst));
        }
    }

    protected void loadEditMode(EditorScreenFacet facet, Element element) {
        String editMode = element.attributeValue("editMode");
        if (isNotEmpty(editMode)) {
            facet.setEditMode(EditMode.valueOf(editMode));
        }
    }
}
