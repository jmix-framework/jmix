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

package io.jmix.charts.widget.amcharts;

import com.vaadin.server.AbstractExtension;
import com.vaadin.server.Extension;
import com.vaadin.ui.UI;
import io.jmix.ui.widget.WebJarResource;

@WebJarResource(value = "amcharts:responsive.min.js", overridePath = "amcharts/")
public class JmixAmchartsResponsiveIntegration extends AbstractExtension {

    public static JmixAmchartsResponsiveIntegration get(UI ui) {
        JmixAmchartsResponsiveIntegration optioner = null;

        // Search singleton optioner
        for (Extension extension : ui.getExtensions()) {
            if (extension instanceof JmixAmchartsResponsiveIntegration) {
                optioner = (JmixAmchartsResponsiveIntegration) extension;
                break;
            }
        }

        // Create new optioner if not found
        if (optioner == null) {
            optioner = new JmixAmchartsResponsiveIntegration();
            optioner.extend(ui);
        }

        return optioner;

    }

    public static JmixAmchartsResponsiveIntegration get() {
        UI ui = UI.getCurrent();

        if (ui == null) {
            throw new IllegalStateException(
                    "This method must be used from UI thread");
        }
        return get(ui);
    }
}