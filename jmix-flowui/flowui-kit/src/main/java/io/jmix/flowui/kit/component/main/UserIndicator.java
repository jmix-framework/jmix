/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component.main;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.SupportsFormatter;
import io.jmix.flowui.kit.component.formatter.Formatter;

import javax.annotation.Nullable;

public class UserIndicator<V> extends Composite<Div> implements SupportsFormatter<V>, HasTitle, HasStyle, HasSize,
        HasEnabled {

    protected static final String USER_INDICATOR_CLASS_NAME = "jmix-user-indicator";
    protected static final String USER_INDICATOR_LABEL_CLASS_NAME = "jmix-user-indicator-label";

    protected Component userComponent;
    protected Formatter<? super V> userFormatter;

    @Override
    protected Div initContent() {
        Div root = super.initContent();
        root.addClassName(USER_INDICATOR_CLASS_NAME);
        return root;
    }

    public void refreshUser() {
        getContent().removeAll();

        userComponent = createUserIndicator();

        getContent().add(userComponent);
    }

    protected void updateUserIndicatorLabel(V user) {
        if (userComponent instanceof HasText) {
            String userTitle = generateUserTitle(user);
            ((HasText) userComponent).setText(userTitle);
        }
    }


    protected Span createUserIndicator() {
        Span userNameLabel = new Span();
        userNameLabel.addClassName(USER_INDICATOR_LABEL_CLASS_NAME);
        return userNameLabel;
    }

    protected String generateUserTitle(V user) {
        if (userFormatter != null) {
            return userFormatter.apply(user);
        }
        return "";
    }

    @Nullable
    @Override
    public Formatter<V> getFormatter() {
        return (Formatter<V>) userFormatter;
    }

    @Override
    public void setFormatter(@Nullable Formatter<? super V> formatter) {
        this.userFormatter = formatter;
        refreshUser();
    }
}
