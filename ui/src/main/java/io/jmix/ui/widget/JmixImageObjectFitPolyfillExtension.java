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

import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Extension;
import com.vaadin.ui.UI;

@WebJarResource("object-fit-images:ofi.min.js")
public class JmixImageObjectFitPolyfillExtension extends AbstractExtension {

    public void extend(JmixImage image) {
        super.extend(image);
    }

    public static JmixImageObjectFitPolyfillExtension get(UI ui) {
        JmixImageObjectFitPolyfillExtension extension = null;

        // Search singleton extension
        for (Extension uiExtension : ui.getExtensions()) {
            if (uiExtension instanceof JmixImageObjectFitPolyfillExtension) {
                extension = (JmixImageObjectFitPolyfillExtension) uiExtension;
                break;
            }
        }

        // Create new extension if not found
        if (extension == null) {
            extension = new JmixImageObjectFitPolyfillExtension();
            extension.extend(ui);
        }

        return extension;
    }
}