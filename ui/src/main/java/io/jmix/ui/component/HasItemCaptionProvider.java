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

package io.jmix.ui.component;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * A component that is marked with this interface allows to manage caption for component item.
 *
 * @param <I> component value type
 */
public interface HasItemCaptionProvider<I> extends Component {

    /**
     * Sets function that provides caption for component item.
     *
     * @param itemCaptionProvider caption provider for item
     */
    void setItemCaptionProvider(@Nullable Function<? super I, String> itemCaptionProvider);

    /**
     * @return caption provider for item
     */
    @Nullable
    Function<? super I, String> getItemCaptionProvider();
}
