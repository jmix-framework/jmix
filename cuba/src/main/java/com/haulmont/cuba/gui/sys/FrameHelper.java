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

package com.haulmont.cuba.gui.sys;

import com.haulmont.cuba.gui.components.AbstractFrame;
import io.jmix.core.ClassManager;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.ScreenXmlLoader;
import io.jmix.ui.sys.WindowAttributesProvider;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("cuba_FrameHelper")
public class FrameHelper {

    protected ScreenXmlLoader screenXmlLoader;
    protected ClassManager classManager;

    @Autowired
    public void setScreenXmlLoader(ScreenXmlLoader screenXmlLoader) {
        this.screenXmlLoader = screenXmlLoader;
    }

    @Autowired
    public void setClassManager(ClassManager classManager) {
        this.classManager = classManager;
    }

    @SuppressWarnings("unchecked")
    public WindowInfo createFakeWindowInfo(String src, String fragmentId) {
        Element screenElement = DocumentHelper.createElement("screen");
        screenElement.addAttribute("template", src);
        screenElement.addAttribute("id", fragmentId);

        Element windowElement = screenXmlLoader.load(src, fragmentId, Collections.emptyMap());
        Class<? extends ScreenFragment> fragmentClass;

        String className = windowElement.attributeValue("class");
        if (StringUtils.isNotEmpty(className)) {
            fragmentClass = (Class<? extends ScreenFragment>) classManager.loadClass(className);
        } else {
            fragmentClass = AbstractFrame.class;
        }

        return new WindowInfo(fragmentId, new WindowAttributesProvider() {
            @Override
            public WindowInfo.Type getType(WindowInfo wi) {
                return WindowInfo.Type.FRAGMENT;
            }

            @Override
            public String getTemplate(WindowInfo wi) {
                return src;
            }

            @Override
            public Class<? extends FrameOwner> getControllerClass(WindowInfo wi) {
                return fragmentClass;
            }

            @Override
            public WindowInfo resolve(WindowInfo windowInfo) {
                return windowInfo;
            }
        }, screenElement);
    }
}
