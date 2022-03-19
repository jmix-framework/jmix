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

package io.jmix.ui.component.filter;

import com.google.common.base.Strings;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.Window;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nullable;

public class FilterUtils {

    public static String generateConfigurationId(@Nullable String configurationName) {
        return WordUtils.capitalize(Strings.nullToEmpty(configurationName))
                .replaceAll(" ", "")
                + RandomStringUtils.randomAlphabetic(8);
    }

    public static String generateFilterPath(Filter filter) {
        StringBuilder sb = new StringBuilder();
        Frame frame = filter.getFrame();
        while (frame != null) {
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            s = "[" + s + "]";
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }

        sb.append(".")
                .append(filter.getId() != null ? filter.getId() : "filterWithoutId");

        return sb.toString();
    }
}
