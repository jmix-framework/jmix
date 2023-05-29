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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.Field;
import io.jmix.core.MessageTools;
import io.jmix.core.Resources;
import io.jmix.ui.component.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.StaticScriptSource;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 */
@Deprecated
public class ScriptValidator implements Field.Validator {

    private String script;
    protected String message;
    protected String messagesPack;
    private String scriptPath;
    private boolean innerScript;
    private Map<String, Object> params;
    protected MessageTools messageTools = AppBeans.get(MessageTools.class);
    protected Resources resources = AppBeans.get(Resources.class);
    protected ScriptEvaluator scriptEvaluator = AppBeans.get(ScriptEvaluator.class);

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
        Map<String, Object> arguments = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            arguments.put(entry.getKey(), entry.getValue());
        }
        if (innerScript) {
            isValid = (Boolean) scriptEvaluator.evaluate(new StaticScriptSource(script), arguments);
        } else if (scriptPath != null) {
            isValid = (Boolean) scriptEvaluator.evaluate(new ResourceScriptSource(resources.getResource(scriptPath)), arguments);
        }
        if (!isValid) {
            String msg = message != null ? messageTools.loadString(messagesPack, message) : "Invalid value '%s'";
            throw new ValidationException(String.format(msg, value));
        }
    }
}
