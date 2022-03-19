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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.ResourceView.HasStreamSettings;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.StreamResource;

import javax.annotation.Nullable;

public abstract class AbstractStreamSettingsResource extends AbstractResource implements HasStreamSettings {

    protected long cacheTime = DownloadStream.DEFAULT_CACHETIME;
    protected int bufferSize;
    protected String fileName;

    @Override
    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;

        if (resource != null) {
            ((StreamResource) resource).setCacheTime(cacheTime);
        }
    }

    @Override
    public long getCacheTime() {
        return cacheTime;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;

        if (resource != null) {
            ((StreamResource) resource).setBufferSize(bufferSize);
        }
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;

        if (resource != null) {
            ((StreamResource) resource).setFilename(fileName);
        }
    }

    @Nullable
    @Override
    public String getFileName() {
        return fileName;
    }
}
