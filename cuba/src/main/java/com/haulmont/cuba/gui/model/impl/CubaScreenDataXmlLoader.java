/*
 * Copyright 2021 Haulmont.
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

package com.haulmont.cuba.gui.model.impl;

import com.google.common.base.Strings;
import com.haulmont.cuba.gui.model.CubaDataComponents;
import com.haulmont.cuba.gui.xml.layout.CubaLoaderConfig;
import io.jmix.data.PersistenceHints;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.ScreenData;
import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import org.dom4j.Element;

import javax.annotation.Nullable;

public class CubaScreenDataXmlLoader extends ScreenDataXmlLoader {

    protected void loadAdditionalLoaderProperties(Element element, DataLoader loader) {
        String softDeletionVal = element.attributeValue("softDeletion");
        if (!Strings.isNullOrEmpty(softDeletionVal))
            loader.setHint(PersistenceHints.SOFT_DELETION, Boolean.parseBoolean(softDeletionVal));
    }

    @Override
    protected CollectionLoader<Object> createCollectionLoader(Element element) {
        String schema = element.getNamespace().getStringValue();
        return schema.startsWith(CubaLoaderConfig.CUBA_XSD_PREFIX) ?
                ((CubaDataComponents) factory).createCubaCollectionLoader()
                : super.createCollectionLoader(element);
    }
}
