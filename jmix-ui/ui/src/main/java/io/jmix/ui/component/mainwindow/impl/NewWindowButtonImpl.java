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

package io.jmix.ui.component.mainwindow.impl;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.ExternalResource;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.component.mainwindow.NewWindowButton;
import io.jmix.ui.sys.ControllerUtils;
import io.jmix.ui.widget.JmixButton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

public class NewWindowButtonImpl extends AbstractComponent<JmixButton> implements NewWindowButton {

    public static final String NEW_WINDOW_BUTTON_STYLENAME = "jmix-newwindow-button";

    public NewWindowButtonImpl() {
        component = new JmixButton();
        component.addStyleName(NEW_WINDOW_BUTTON_STYLENAME);
        component.setDescription(null);

        URL pageUrl;
        try {
            pageUrl = new URL(ControllerUtils.getLocationWithoutParams());
        } catch (MalformedURLException ignored) {
            LoggerFactory.getLogger(NewWindowButtonImpl.class).warn("Couldn't get URL of current Page");
            return;
        }

        ExternalResource currentPage = new ExternalResource(pageUrl);
        final BrowserWindowOpener opener = new BrowserWindowOpener(currentPage);
        opener.setWindowName("_blank");

        opener.extend(component);
    }

    @Override
    public void setStyleName(@Nullable String name) {
        super.setStyleName(name);

        component.addStyleName(NEW_WINDOW_BUTTON_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(NEW_WINDOW_BUTTON_STYLENAME, ""));
    }
}
