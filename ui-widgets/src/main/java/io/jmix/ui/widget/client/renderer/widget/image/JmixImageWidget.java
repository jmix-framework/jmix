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

package io.jmix.ui.widget.client.renderer.widget.image;

import com.google.gwt.user.client.ui.Image;
import io.jmix.ui.widget.client.grid.HasClickSettings;

public class JmixImageWidget extends Image implements HasClickSettings {
    protected boolean clickThroughEnabled = false;

    @Override
    public boolean isClickThroughEnabled() {
        return clickThroughEnabled;
    }

    @Override
    public void setClickThroughEnabled(boolean enabled) {
        clickThroughEnabled = enabled;
    }
}
