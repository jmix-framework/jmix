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

package com.haulmont.cuba.core.config.type;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class DecimalStringify extends TypeStringify {
    @Override
    public String stringify(Object value) {
        UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);
        NumberFormat decimalFormat = DecimalFormat.getInstance(userSessionSource.getLocale());
        return decimalFormat.format(value);
    }
}
