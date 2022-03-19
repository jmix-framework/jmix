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

package io.jmix.ui.component;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * A component that is marked with this interface allows to manage a description for option displayed
 * by this component.
 *
 * @param <T> option item type
 */
public interface HasOptionDescriptionProvider<T> extends Component {

    /**
     * Sets the option description provider.
     *
     * @param optionDescriptionProvider provider which provides descriptions for options
     */
    void setOptionDescriptionProvider(@Nullable Function<? super T, String> optionDescriptionProvider);

    /**
     * @return option description provider
     */
    @Nullable
    Function<? super T, String> getOptionDescriptionProvider();
}
