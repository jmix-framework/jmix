/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.export;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.CoreProperties;
import io.jmix.ui.UiProperties;

public class ByteArrayDataProvider extends io.jmix.ui.download.ByteArrayDataProvider implements ExportDataProvider {

    public ByteArrayDataProvider(byte[] data, int saveExportedByteArrayDataThresholdBytes, String tempDir) {
        super(data, saveExportedByteArrayDataThresholdBytes, tempDir);
    }

    public ByteArrayDataProvider(byte[] data) {
        this(data,
                AppBeans.get(UiProperties.class).getSaveExportedByteArrayDataThresholdBytes(),
                AppBeans.get(CoreProperties.class).getTempDir());
    }
}