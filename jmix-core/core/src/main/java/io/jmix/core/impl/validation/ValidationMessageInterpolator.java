/*
 * Copyright (c) 2008-2016 Haulmont.
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

package io.jmix.core.impl.validation;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.security.CurrentAuthentication;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.el.ExpressionFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("core_ValidationMessageInterpolator")
public class ValidationMessageInterpolator implements MessageInterpolator {

    @Autowired
    protected Messages messages;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected ExpressionFactory expressionFactory = ExpressionFactory.newInstance();

    protected static final Pattern LEFT_BRACE = Pattern.compile("\\{", Pattern.LITERAL);
    protected static final Pattern RIGHT_BRACE = Pattern.compile("\\}", Pattern.LITERAL);
    protected static final Pattern SLASH = Pattern.compile("\\\\", Pattern.LITERAL);
    protected static final Pattern DOLLAR = Pattern.compile("\\$", Pattern.LITERAL);

    private static final Logger log = LoggerFactory.getLogger(ValidationMessageInterpolator.class);

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, getUserLocale());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        String interpolatedMessage = messageTemplate;
        try {
            interpolatedMessage = interpolateMessage(messageTemplate, context, locale);
        } catch (ValidationException e) {
            log.error("Unable to interpolate validation message: {}", e.getMessage());
        }
        return interpolatedMessage;
    }

    protected String interpolateMessage(String messageTemplate, Context context, Locale locale) {
        String resolvedMessage = interpolateMessage(messageTemplate, locale);

        TokenCollector tokenCollector = new TokenCollector(resolvedMessage, InterpolationTermType.PARAMETER);
        List<Token> tokens = tokenCollector.getTokenList();

        resolvedMessage = interpolateExpression(new TokenIterator(tokens), context, locale);

        tokenCollector = new TokenCollector(resolvedMessage, InterpolationTermType.EL);
        tokens = tokenCollector.getTokenList();

        resolvedMessage = interpolateExpression(new TokenIterator(tokens), context, locale);

        resolvedMessage = replaceEscapedLiterals(resolvedMessage);

        return resolvedMessage;
    }

    protected String interpolateExpression(TokenIterator tokenIterator, Context context, Locale locale) {
        while (tokenIterator.hasMoreInterpolationTerms()) {
            String term = tokenIterator.nextInterpolationTerm();

            InterpolationTerm expression = new InterpolationTerm(term, locale, expressionFactory);
            String resolvedExpression = expression.interpolate(context);
            tokenIterator.replaceCurrentInterpolationTerm(resolvedExpression);
        }
        return tokenIterator.getInterpolatedMessage();
    }

    protected String replaceEscapedLiterals(String resolvedMessage) {
        resolvedMessage = LEFT_BRACE.matcher(resolvedMessage).replaceAll("{");
        resolvedMessage = RIGHT_BRACE.matcher(resolvedMessage).replaceAll("}");
        resolvedMessage = SLASH.matcher(resolvedMessage).replaceAll(Matcher.quoteReplacement("\\"));
        resolvedMessage = DOLLAR.matcher(resolvedMessage).replaceAll(Matcher.quoteReplacement("$"));
        return resolvedMessage;
    }

    protected String interpolateMessage(String message, Locale locale) {
        TokenCollector tokenCollector = new TokenCollector(message, InterpolationTermType.PARAMETER);
        TokenIterator tokenIterator = new TokenIterator(tokenCollector.getTokenList());
        while (tokenIterator.hasMoreInterpolationTerms()) {
            String term = tokenIterator.nextInterpolationTerm();
            String resolvedParameterValue = resolveParameter(term, locale);
            tokenIterator.replaceCurrentInterpolationTerm(resolvedParameterValue);
        }
        return tokenIterator.getInterpolatedMessage();
    }

    protected String resolveParameter(String parameterName, Locale locale) {
        String parameterValue;
        String messageCode = removeCurlyBraces(parameterName);
        try {
            if (messageCode.startsWith("jakarta.validation.constraints")
                    || messageCode.startsWith("org.hibernate.validator.constraints")) {
                parameterValue = messages.getMessage(messageCode, locale);
                // try to find tokens recursive
                parameterValue = interpolateMessage(parameterValue, locale);
            } else if (messageTools.isMessageKey(messageCode)) {
                parameterValue = messageTools.loadString(messageCode, locale);
                // try to find tokens recursive
                parameterValue = interpolateMessage(parameterValue, locale);
            } else {
                parameterValue = parameterName;
            }
        } catch (UnsupportedOperationException e) {
            // return parameter itself
            parameterValue = parameterName;
        }
        return parameterValue;
    }

    protected String removeCurlyBraces(String parameter) {
        return parameter.substring(1, parameter.length() - 1);
    }

    protected Locale getUserLocale() {
        return currentAuthentication.isSet() ? currentAuthentication.getLocale() : Locale.getDefault();
    }
}