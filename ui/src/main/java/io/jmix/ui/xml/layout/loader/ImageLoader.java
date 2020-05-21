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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.Image;
import org.dom4j.Element;

public class ImageLoader extends AbstractResourceViewLoader<Image> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(Image.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadData(resultComponent, element);
        loadScaleMode(resultComponent, element);
    }

    protected void loadScaleMode(Image image, Element element) {
        String scaleModeString = element.attributeValue("scaleMode");
        Image.ScaleMode scaleMode = Image.ScaleMode.NONE;
        if (scaleModeString != null) {
            scaleMode = Image.ScaleMode.valueOf(scaleModeString);
        }
        image.setScaleMode(scaleMode);
    }
}
