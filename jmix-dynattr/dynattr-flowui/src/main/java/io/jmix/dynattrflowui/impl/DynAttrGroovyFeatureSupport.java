/*
 * Copyright 2026 Haulmont.
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

package io.jmix.dynattrflowui.impl;

import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.dynattrflowui.DynAttrUiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("dynat_DynAttrGroovyFeatureSupport")
public class DynAttrGroovyFeatureSupport {

    private static final Logger log = LoggerFactory.getLogger(DynAttrGroovyFeatureSupport.class);

    protected final ScriptEvaluator scriptEvaluator;
    protected final DynAttrUiProperties dynAttrUiProperties;
    protected final CoreProperties coreProperties;
    protected final Messages messages;

    public DynAttrGroovyFeatureSupport(ScriptEvaluator scriptEvaluator,
                                       DynAttrUiProperties dynAttrUiProperties,
                                       CoreProperties coreProperties,
                                       Messages messages) {
        this.scriptEvaluator = scriptEvaluator;
        this.dynAttrUiProperties = dynAttrUiProperties;
        this.coreProperties = coreProperties;
        this.messages = messages;
    }

    public boolean isGroovyEnabled() {
        return coreProperties.isUnsafeRuntimeFeaturesEnabled() && dynAttrUiProperties.isGroovyEnabled();
    }

    public Object evaluateValidationScript(String script, Object value) {
        if (!isGroovyEnabled()) {
            log.warn("Groovy execution is disabled for dynamic attribute validation. Returning a fixed validation error.");
            return messages.getMessage(getClass(), "validationScriptDisabled");
        }
        return scriptEvaluator.evaluate(new StaticScriptSource(script), Collections.singletonMap("value", value));
    }

    public Object evaluateRecalculationScript(String script, Map<String, Object> parameters) {
        if (!isGroovyEnabled()) {
            throw new GroovyScriptExecutionDisabledException(
                    messages.getMessage(getClass(), "recalculationScriptDisabled"));
        }
        return scriptEvaluator.evaluate(new StaticScriptSource(script), parameters);
    }

    public List<?> evaluateOptionsLoaderScript(String script, Object entity) {
        if (!isGroovyEnabled()) {
            log.warn("Groovy execution is disabled for dynamic attribute options loading. Returning an empty list.");
            return Collections.emptyList();
        }
        return (List<?>) scriptEvaluator.evaluate(new StaticScriptSource(script), Collections.singletonMap("entity", entity));
    }
}
