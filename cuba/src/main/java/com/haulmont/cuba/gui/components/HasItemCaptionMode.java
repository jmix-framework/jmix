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

package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.compatibility.LegacyCaptionAdapter;
import io.jmix.ui.component.HasItemCaptionProvider;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @deprecated use {@link HasItemCaptionProvider} instead
 */
@Deprecated
public interface HasItemCaptionMode {

    /**
     * @return caption mode
     * @deprecated use {@link HasItemCaptionProvider#getItemCaptionProvider} instead
     */
    @Deprecated
    @Nullable
    default CaptionMode getCaptionMode() {
        Function provider = ((HasItemCaptionProvider) this).getItemCaptionProvider();

        if (provider instanceof LegacyCaptionAdapter) {
            return ((LegacyCaptionAdapter) provider).getCaptionMode();
        }

        return null;
    }

    /**
     * Sets caption mode.
     *
     * @param captionMode caption mode
     * @deprecated use {@link HasItemCaptionProvider#setItemCaptionProvider(Function)} instead
     */
    @Deprecated
    default void setCaptionMode(@Nullable CaptionMode captionMode) {
        Function provider = ((HasItemCaptionProvider) this).getItemCaptionProvider();

        String captionProperty = null;
        if (provider instanceof LegacyCaptionAdapter) {
            captionProperty = ((LegacyCaptionAdapter) provider).getCaptionProperty();
        }

        LegacyCaptionAdapter adapter = captionMode != null
                || StringUtils.isNotEmpty(captionProperty)
                ? new LegacyCaptionAdapter(captionMode, captionProperty)
                : null;

        ((HasItemCaptionProvider) this).setItemCaptionProvider(adapter);
    }

    /**
     * @return caption property
     * @deprecated use {@link HasItemCaptionProvider#getItemCaptionProvider} instead
     */
    @Deprecated
    @Nullable
    default String getCaptionProperty() {
        Function provider = ((HasItemCaptionProvider) this).getItemCaptionProvider();

        if (provider instanceof LegacyCaptionAdapter) {
            return ((LegacyCaptionAdapter) provider).getCaptionProperty();
        }

        return null;
    }

    /**
     * Sets caption property.
     *
     * @param captionProperty caption property
     * @deprecated use {@link HasItemCaptionProvider#setItemCaptionProvider(Function)} instead
     */
    @Deprecated
    default void setCaptionProperty(@Nullable String captionProperty) {
        CaptionMode captionMode = null;

        Function provider = ((HasItemCaptionProvider) this).getItemCaptionProvider();

        if (provider instanceof LegacyCaptionAdapter) {
            captionMode = ((LegacyCaptionAdapter) provider).getCaptionMode();
        }

        LegacyCaptionAdapter adapter = captionMode != null
                || StringUtils.isNotEmpty(captionProperty)
                ? new LegacyCaptionAdapter(captionMode, captionProperty)
                : null;

        ((HasItemCaptionProvider) this).setItemCaptionProvider(adapter);
    }
}
