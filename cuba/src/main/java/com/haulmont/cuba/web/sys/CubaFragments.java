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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.FragmentsImpl;
import org.dom4j.Element;

@Deprecated
public class CubaFragments extends FragmentsImpl {

    @Override
    protected ComponentLoaderContext createComponentLoaderContext(ScreenOptions options) {
        return new ComponentLoaderContext(options);
    }

    @Override
    protected Fragment createFragmentInternal() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(com.haulmont.cuba.gui.components.Fragment.NAME);
    }

    @Override
    protected void loadAdditionalData(Element rootElement, io.jmix.ui.xml.layout.loader.ComponentLoaderContext innerContext) {
        super.loadAdditionalData(rootElement, innerContext);

        String messagesPack = rootElement.attributeValue("messagesPack");
        if (messagesPack != null) {
            innerContext.setMessageGroup(messagesPack);
        }
    }
}
