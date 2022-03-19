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

package io.jmix.ui.widget.client.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Image;
import io.jmix.ui.widget.client.renderer.widget.image.JmixImageWidget;
import com.vaadin.client.renderers.ImageRenderer;
import com.vaadin.client.widget.grid.RendererCellReference;

public class JmixImageRenderer extends ImageRenderer {
    @Override
    public JmixImageWidget createWidget() {
        JmixImageWidget image = GWT.create(JmixImageWidget.class);
        image.addClickHandler(this);
        image.setClickThroughEnabled(true);
        return image;
    }

    @Override
    public void render(RendererCellReference cell, String url, Image image) {
        super.render(cell, url, image);
        if (url == null) {
            image.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
    }
}
