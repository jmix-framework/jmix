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

import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.JmixEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> entity
 * @deprecated Use {@link io.jmix.ui.component.TokenList} instead
 */
@Deprecated
public interface TokenList<V extends JmixEntity> extends Field<Collection<V>>, io.jmix.ui.component.TokenList<V>,
        HasCaptionMode {

    /**
     * @return option captions mode generation
     * @deprecated use {@link io.jmix.ui.component.TokenList#getLookupFieldOptionsCaptionProvider()}
     */
    @Nullable
    @Deprecated
    CaptionMode getOptionsCaptionMode();

    /**
     * Sets how LookupField option captions should be generated.
     *
     * @param optionsCaptionMode mode
     * @deprecated use {@link io.jmix.ui.component.TokenList#setLookupFieldOptionsCaptionProvider(Function)} instead
     */
    @Deprecated
    void setOptionsCaptionMode(@Nullable CaptionMode optionsCaptionMode);

    /**
     * @return a property that is used for LookupField option captions generation
     * @deprecated use {@link io.jmix.ui.component.TokenList#getLookupFieldOptionsCaptionProvider()} instead
     */
    @Deprecated
    @Nullable
    String getOptionsCaptionProperty();

    /**
     * Sets a property that will be used for LookupField option captions generation when {@link CaptionMode#PROPERTY} is used.
     *
     * @param optionsCaptionProperty property
     * @deprecated use {@link io.jmix.ui.component.TokenList#setLookupFieldOptionsCaptionProvider(Function)} instead
     */
    @Deprecated
    void setOptionsCaptionProperty(@Nullable String optionsCaptionProperty);
}
