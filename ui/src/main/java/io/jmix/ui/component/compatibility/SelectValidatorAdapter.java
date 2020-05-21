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

package io.jmix.ui.component.compatibility;

import io.jmix.core.Entity;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.LookupScreen;

import java.util.function.Predicate;

@Deprecated
public class SelectValidatorAdapter<T extends Entity> implements Predicate<LookupScreen.ValidationContext<T>> {

    private final Window.Lookup.Validator validator;

    public SelectValidatorAdapter(Window.Lookup.Validator validator) {
        this.validator = validator;
    }

    @Override
    public boolean test(LookupScreen.ValidationContext<T> validationContext) {
        return validator.validate();
    }

    public Window.Lookup.Validator getValidator() {
        return validator;
    }
}
