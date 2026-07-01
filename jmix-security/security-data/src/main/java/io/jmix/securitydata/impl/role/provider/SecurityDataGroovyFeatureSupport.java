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

package io.jmix.securitydata.impl.role.provider;

import io.jmix.core.CoreProperties;
import io.jmix.securitydata.SecurityDataProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("sec_SecurityDataGroovyFeatureSupport")
public class SecurityDataGroovyFeatureSupport {

    private static final Logger log = LoggerFactory.getLogger(SecurityDataGroovyFeatureSupport.class);

    protected final ScriptEvaluator scriptEvaluator;
    protected final SecurityDataProperties securityDataProperties;
    protected final CoreProperties coreProperties;

    public SecurityDataGroovyFeatureSupport(ScriptEvaluator scriptEvaluator,
                                            SecurityDataProperties securityDataProperties,
                                            CoreProperties coreProperties) {
        this.scriptEvaluator = scriptEvaluator;
        this.securityDataProperties = securityDataProperties;
        this.coreProperties = coreProperties;
    }

    public boolean isGroovyEnabled() {
        return coreProperties.isUnsafeRuntimeFeaturesEnabled() && securityDataProperties.isGroovyEnabled();
    }

    public boolean evaluatePredicate(String script, Object entity, ApplicationContext applicationContext) {
        if (!isGroovyEnabled()) {
            log.warn("Groovy execution is disabled for security row-level predicates. Returning deny result.");
            return false;
        }

        String modifiedScript = script.replace("{E}", "__entity__");
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("__entity__", entity);
        arguments.put("applicationContext", applicationContext);
        return Boolean.TRUE.equals(scriptEvaluator.evaluate(new StaticScriptSource(modifiedScript), arguments));
    }
}
