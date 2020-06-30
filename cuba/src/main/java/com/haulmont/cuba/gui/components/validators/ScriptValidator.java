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
package com.haulmont.cuba.gui.components.validators;

import com.haulmont.cuba.gui.components.Field;
import io.jmix.core.AppBeans;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Resources;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.GroovyScriptValidator;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated Use a {@link GroovyScriptValidator} instead
 */
@Deprecated
public class ScriptValidator implements Field.Validator {

    private String script;
    protected String message;
    protected String messagesPack;
    private String scriptPath;
    private boolean innerScript;
    private Map<String, Object> params;
    protected Messages messages = AppBeans.get(Messages.NAME);
    protected MessageTools messageTools = AppBeans.get(MessageTools.NAME);
    protected Resources resources = AppBeans.get(Resources.NAME);
    protected ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    public ScriptValidator(Element element, String messagesPack) {
        this.script = element.getText();
        innerScript = StringUtils.isNotBlank(script);
        if (!innerScript) {
            scriptPath = element.attributeValue("script");
        }
        message = element.attributeValue("message");
        this.messagesPack = messagesPack;
    }

    public ScriptValidator(String path, String messagesPack) {
        this.scriptPath = path;
        innerScript = false;
        message = "Not sure of it";
        this.messagesPack = messagesPack;
    }

    public ScriptValidator(String scriptPath, String message, String messagesPack) {
        this.message = message;
        this.messagesPack = messagesPack;
        this.scriptPath = scriptPath;
    }

    public ScriptValidator(String scriptPath, String message, String messagesPack, Map<String, Object> params) {
        this.scriptPath = scriptPath;
        this.message = message;
        this.messagesPack = messagesPack;
        this.params = params;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        Boolean isValid = false;
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("value", value);
        ScriptEngine engine = scriptEngineManager.getEngineByName("groovy");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        if (innerScript) {
            try {
                isValid = (Boolean) engine.eval(script);
            } catch (ScriptException e) {
                throw new RuntimeException("Error evaluating Groovy expression", e);
            }
        } else if (scriptPath != null) {
            try (FileReader reader = new FileReader(resources.getResource(scriptPath).getFile())) {
                isValid = (Boolean) engine.eval(reader);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Groovy script file not found", e);
            } catch (ScriptException e) {
                throw new RuntimeException("Error evaluating Groovy expression", e);
            } catch (IOException e) {
                throw new RuntimeException("Groovy script file I/O exception", e);
            }
        }
        if (!isValid) {
            String msg = message != null ? messageTools.loadString(messagesPack, message) : "Invalid value '%s'";
            throw new ValidationException(String.format(msg, value));
        }
    }
}
