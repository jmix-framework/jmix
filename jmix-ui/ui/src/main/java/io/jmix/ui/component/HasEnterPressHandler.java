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
import java.util.EventObject;
import java.util.function.Consumer;

public interface HasEnterPressHandler {

    /**
     * @return an ENTER press handler
     */
    @Nullable
    Consumer<EnterPressEvent> getEnterPressHandler();

    /**
     * Sets an ENTER press handler.
     *
     * @param handler an ENTER press handler to set
     */
    void setEnterPressHandler(@Nullable Consumer<EnterPressEvent> handler);

    /**
     * Event is fired when the user presses the enter key when the text field is on focus.
     */
    class EnterPressEvent extends EventObject {

        protected String text;

        public EnterPressEvent(Component source, String text) {
            super(source);

            this.text = text;
        }

        @Override
        public Component getSource() {
            return (Component) super.getSource();
        }

        /**
         * @return entered text
         */
        public String getText() {
            return text;
        }
    }
}
