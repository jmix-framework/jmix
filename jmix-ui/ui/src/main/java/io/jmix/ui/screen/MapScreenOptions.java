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

package io.jmix.ui.screen;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class MapScreenOptions implements ScreenOptions {

    private final Map<String, Object> params;

    public MapScreenOptions(@Nullable Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getParams() {
        if (params == null) {
            return Collections.emptyMap();
        }
        return params;
    }
}