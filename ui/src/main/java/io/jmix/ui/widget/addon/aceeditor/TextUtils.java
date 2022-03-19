/*
 * Copyright 2017 Antti Nieminen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.ui.widget.addon.aceeditor;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author artamonov
 * @version $Id$
 */
public class TextUtils {
    public static boolean equals(@Nullable String a, @Nullable String b) {
        return Objects.equals(a, b);
    }
}
