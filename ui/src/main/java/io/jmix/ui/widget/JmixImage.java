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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.image.JmixImageState;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Image;

public class JmixImage extends Image {

    public void setScaleMode(String scaleMode) {
        if (!getState(false).scaleMode.equals(scaleMode)) {
            getState().scaleMode = scaleMode;
        }
    }

    public String getScaleMode() {
        return getState(false).scaleMode;
    }

    @Override
    public void attach() {
        super.attach();

        WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
        if (webBrowser.isIE() || webBrowser.isEdge()) {
            JmixImageObjectFitPolyfillExtension.get(getUI());
        }
    }

    @Override
    protected JmixImageState getState() {
        return (JmixImageState) super.getState();
    }

    @Override
    protected JmixImageState getState(boolean markAsDirty) {
        return (JmixImageState) super.getState(markAsDirty);
    }
}