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

package io.jmix.dynattrflowui.impl;

import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.validation.AbstractValidator;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.sys.substitutor.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.support.StaticScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * GroovyScript validator runs a custom Groovy script. If the script returns any object,
 * then {@link ValidationException} is thrown. {@code scriptResult.toString()} is used as error message.
 * <p>
 * For error message it uses template string and it is possible to use '${value}' key for formatted output.
 * <p>
 * In order to provide your own implementation globally, create a subclass and register it in configuration class,
 * for example:
 * <pre>
 *     &#64;Bean("dynattr_GroovyScriptValidator")
 *     &#64;Scope(BeanDefinition.SCOPE_PROTOTYPE)
 *     &#64;Primary
 *     protected GroovyScriptValidator groovyScriptValidator() {
 *          return new CustomGroovyScriptValidator();
 *     }
 * </pre>
 *
 * @param <T> any Object
 */
@Component("dynat_GroovyScriptValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GroovyScriptValidator<T> extends AbstractValidator<T> {

    protected String validatorGroovyScript;

    protected final ScriptEvaluator scriptEvaluator;

    public GroovyScriptValidator(Messages messages,
                                 DatatypeRegistry datatypeRegistry,
                                 CurrentAuthentication currentAuthentication,
                                 @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                 StringSubstitutor substitutor,
                                 @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                                 ScriptEvaluator scriptEvaluator) {
        this.messages = messages;
        this.datatypeRegistry = datatypeRegistry;
        this.currentAuthentication = currentAuthentication;
        this.substitutor = substitutor;
        this.scriptEvaluator = scriptEvaluator;
    }


    @Override
    public void accept(T value) throws ValidationException {
        // consider null value is valid
        if (value == null) {
            return;
        }
        Object scriptResult = scriptEvaluator.evaluate(new StaticScriptSource(validatorGroovyScript),
                Collections.singletonMap("value", value));
        if (scriptResult != null) {
            setMessage(scriptResult.toString());
            fireValidationException(value);
        }
    }

    protected void fireValidationException(T value) {
        String message = getMessage();

        String formattedValue = formatValue(value);

        String formattedMessage = getTemplateErrorMessage(
                message, ParamsMap.of("value", formattedValue));

        throw new ValidationException(formattedMessage);
    }

    /**
     * Sets a Groovy script
     *
     * @param validatorGroovyScript Groovy script
     */
    public void setValidatorGroovyScript(String validatorGroovyScript) {
        this.validatorGroovyScript = validatorGroovyScript;
    }
}
