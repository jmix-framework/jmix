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

package com.haulmont.cuba.gui.components.compatibility;

import io.jmix.ui.component.HasEnterPressHandler;

import java.util.function.Consumer;

@Deprecated
public class LookupFieldNewOptionHandlerAdapter implements Consumer<HasEnterPressHandler.EnterPressEvent> {

    protected Consumer<String> newOptionHandler;

    public LookupFieldNewOptionHandlerAdapter(Consumer<String> newOptionHandler) {
        this.newOptionHandler = newOptionHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LookupFieldNewOptionHandlerAdapter)) return false;

        LookupFieldNewOptionHandlerAdapter that = (LookupFieldNewOptionHandlerAdapter) o;

        return newOptionHandler.equals(that.newOptionHandler);
    }

    @Override
    public int hashCode() {
        return newOptionHandler.hashCode();
    }

    @Override
    public void accept(HasEnterPressHandler.EnterPressEvent enterPressEvent) {
        newOptionHandler.accept(enterPressEvent.getText());
    }

    public Consumer<String> getNewOptionHandler() {
        return newOptionHandler;
    }
}
