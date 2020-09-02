/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui.template.edit;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.haulmont.cuba.core.global.Messages;
import io.jmix.ui.component.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(JsonConfigValidator.NAME)
public class JsonConfigValidator implements Consumer<String> {

    public static final String NAME = "cuba_JsonConfigValidator";

    protected static final Gson gson;
    
    static {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    protected String messagePack;
    protected Messages messages;

    public JsonConfigValidator(String messagePack) {
        this.messagePack = messagePack;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void accept(@Nullable String s) throws ValidationException {
        if (!Strings.isNullOrEmpty(s)) {
            try {
                gson.fromJson(s, JsonObject.class);
            } catch (JsonParseException e) {
                throw new ValidationException(messages.getMessage(messagePack, "chartEdit.invalidJson"));
            }
        }
    }
}
