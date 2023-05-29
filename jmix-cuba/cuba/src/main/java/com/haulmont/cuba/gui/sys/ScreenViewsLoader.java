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

package com.haulmont.cuba.gui.sys;

import io.jmix.core.Resources;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.InputStream;

/**
 * Loads views defined in screen descriptors.
 */
@Component("ui_ScreenViewsLoader")
@Deprecated
public class ScreenViewsLoader {


    @Autowired
    protected Resources resources;

    /**
     * Deploy views defined in <code>metadataContext</code> of a frame.
     *
     * @param rootElement root element of a frame XML
     */
    public void deployViews(Element rootElement) {
        Element metadataContextEl = rootElement.element("metadataContext");
        if (metadataContextEl != null) {
            //todo views from metadata context
            throw new UnsupportedOperationException();
        }
    }

    protected InputStream getInputStream(String resource) {
        InputStream resourceInputStream = resources.getResourceAsStream(resource);
        if (resourceInputStream == null) {
            throw new RuntimeException("View resource not found: " + resource);
        }
        return resourceInputStream;
    }
}
