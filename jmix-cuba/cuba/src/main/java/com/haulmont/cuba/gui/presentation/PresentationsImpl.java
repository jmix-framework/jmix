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

package com.haulmont.cuba.gui.presentation;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.ui.component.Component;
import io.jmix.uidata.TablePresentationsImpl;
import io.jmix.ui.presentation.model.TablePresentation;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.UUID;

public class PresentationsImpl extends TablePresentationsImpl implements Presentations {

    @Autowired
    protected Dom4jTools dom4jTools;

    public PresentationsImpl(Component c) {
        super(c);
    }

    @Override
    public Element getSettings(TablePresentation p) {
        Preconditions.checkNotNullArgument(p);

        p = getPresentation(EntityValues.<UUID>getId(p));
        if (p != null) {
            Document doc;
            if (!StringUtils.isEmpty(p.getSettings())) {
                doc = dom4jTools.readDocument(p.getSettings());
            } else {
                doc = DocumentHelper.createDocument();
                doc.setRootElement(doc.addElement("presentation"));
            }
            return doc.getRootElement();
        } else {
            return null;
        }
    }

    @Override
    public void setSettings(TablePresentation p, Element e) {
        Preconditions.checkNotNullArgument(p);
        Preconditions.checkNotNullArgument(e);

        p = getPresentation(EntityValues.<UUID>getId(p));
        if (p != null) {
            p.setSettings(dom4jTools.writeDocument(e.getDocument(), false));
            modify(p);
        }
    }
}
