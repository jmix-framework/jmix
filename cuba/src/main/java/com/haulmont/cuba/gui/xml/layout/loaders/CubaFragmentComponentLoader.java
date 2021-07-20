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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.compatibility.LegacyFragmentAdapter;
import com.haulmont.cuba.gui.sys.FrameHelper;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.xml.layout.loader.FragmentComponentLoader;
import org.dom4j.Element;

public class CubaFragmentComponentLoader extends FragmentComponentLoader {

    @Override
    protected Fragment createComponentInternal() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(com.haulmont.cuba.gui.components.Fragment.NAME);
    }

    @Override
    protected ComponentLoaderContext createInnerContext() {
        return new ComponentLoaderContext(getComponentContext().getOptions());
    }

    @Override
    protected void setupScreenContext(ScreenFragment controller, ScreenContext screenContext) {
        super.setupScreenContext(controller, screenContext);

        if (controller instanceof LegacyFragmentAdapter) {
            AbstractWindow realScreen = ((LegacyFragmentAdapter) controller).getRealScreen();
            UiControllerUtils.setScreenContext(realScreen, screenContext);
        }
    }

    @Override
    protected WindowInfo createWindowInfo(Element element) {
        String src = element.attributeValue("src");
        if (src == null) {
            return super.createWindowInfo(element);
        }

        String frameId;
        if (element.attributeValue("id") != null) {
            frameId = element.attributeValue("id");
        } else if (element.attributeValue("screen") != null) {
            frameId = element.attributeValue("screen");
        } else {
            frameId = src;
        }

        FrameHelper frameHelper = getFrameHelper();
        return frameHelper.createFakeWindowInfo(src, frameId);
    }

    @Override
    protected void loadAdditionalData(Element rootElement) {
        super.loadAdditionalData(rootElement);

        String messagesPack = rootElement.attributeValue("messagesPack");
        if (messagesPack != null) {
            innerContext.setMessageGroup(messagesPack);
        }
    }

    protected FrameHelper getFrameHelper() {
        return applicationContext.getBean(FrameHelper.class);
    }
}
