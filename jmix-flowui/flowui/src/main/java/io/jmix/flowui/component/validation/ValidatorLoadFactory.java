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

package io.jmix.flowui.component.validation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.MessageTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Specific bean for loading validators.
 */
@Component("flowui_ValidatorFactory")
public class ValidatorLoadFactory {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;
    protected LoaderSupport loaderSupport;

    protected final Map<String, BiFunction<Element, String, Validator<?>>> validatorsMap
            = ImmutableMap.<String, BiFunction<Element, String, Validator<?>>>builder()
            .put("custom", this::loadCustomValidator)
            .put("decimalMin", this::loadDecimalMinValidator)
            .put("decimalMax", this::loadDecimalMaxValidator)
            .put("doubleMin", this::loadDoubleMinValidator)
            .put("doubleMax", this::loadDoubleMaxValidator)
            .put("digits", this::loadDigitsValidator)
            .put("email", this::loadValidatorWithoutAttributes)
            .put("future", this::loadFutureValidator)
            .put("futureOrPresent", this::loadFutureOrPresentValidator)
            .put("max", this::loadMaxValidator)
            .put("min", this::loadMinValidator)
            .put("negativeOrZero", this::loadValidatorWithoutAttributes)
            .put("negative", this::loadValidatorWithoutAttributes)
            .put("notBlank", this::loadValidatorWithoutAttributes)
            .put("notEmpty", this::loadValidatorWithoutAttributes)
            .put("notNull", this::loadValidatorWithoutAttributes)
            .put("pastOrPresent", this::loadPastOrPresentValidator)
            .put("past", this::loadPastValidator)
            .put("positiveOrZero", this::loadValidatorWithoutAttributes)
            .put("positive", this::loadValidatorWithoutAttributes)
            .put("regexp", this::loadRegexpValidator)
            .put("size", this::loadSizeValidator)
            .build();

    public ValidatorLoadFactory(ApplicationContext applicationContext, MessageTools messageTools,
                                LoaderSupport loaderSupport) {
        this.applicationContext = applicationContext;
        this.messageTools = messageTools;
        this.loaderSupport = loaderSupport;
    }

    /**
     * Creates validator from XML element.
     *
     * @param element      validator element
     * @param messageGroup message group
     * @return validator or null if there is no such element
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public Validator createValidator(Element element, String messageGroup) {
        BiFunction<Element, String, Validator<?>> function = validatorsMap.get(element.getName());
        if (function != null) {
            return function.apply(element, messageGroup);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadCustomValidator(Element element, String messageGroup) {
        String beanName = loaderSupport.loadString(element, "bean").orElse(null);
        if (Strings.isNullOrEmpty(beanName)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        Validator validator = (Validator) applicationContext.getBean(beanName);

        if (validator instanceof AbstractValidator) {
            loaderSupport.loadResourceString(
                    element, "message", messageGroup, ((AbstractValidator) validator)::setMessage);
        }

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMinValidator(Element element, String messageGroup) {
        Double doubleValue = loaderSupport.loadDouble(element, "value").orElse(null);
        if (doubleValue == null) {
            throw new IllegalArgumentException("Min value is not defined");
        }

        DecimalMinValidator validator = applicationContext.getBean(
                DecimalMinValidator.class, BigDecimal.valueOf(doubleValue));

        loaderSupport.loadBoolean(element, "inclusive", validator::setInclusive);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMaxValidator(Element element, String messageGroup) {
        Double doubleValue = loaderSupport.loadDouble(element, "value").orElse(null);
        if (doubleValue == null) {
            throw new IllegalArgumentException("Max value is not defined");
        }

        DecimalMaxValidator validator = applicationContext.getBean(
                DecimalMaxValidator.class, BigDecimal.valueOf(doubleValue));

        loaderSupport.loadBoolean(element, "inclusive", validator::setInclusive);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMinValidator(Element element, String messageGroup) {
        Double doubleValue = loaderSupport.loadDouble(element, "value").orElse(null);
        if (doubleValue == null) {
            throw new IllegalArgumentException("Min value is not defined");
        }

        DoubleMinValidator validator = applicationContext.getBean(DoubleMinValidator.class, doubleValue);

        loaderSupport.loadBoolean(element, "inclusive", validator::setInclusive);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMaxValidator(Element element, String messageGroup) {
        Double doubleValue = loaderSupport.loadDouble(element, "value").orElse(null);
        if (doubleValue == null) {
            throw new IllegalArgumentException("Max value is not defined");
        }

        DoubleMaxValidator validator = applicationContext.getBean(DoubleMaxValidator.class, doubleValue);

        loaderSupport.loadBoolean(element, "inclusive", validator::setInclusive);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDigitsValidator(Element element, String messageGroup) {
        Integer integerValue = loaderSupport.loadInteger(element, "integer").orElse(null);
        Integer fractionValue = loaderSupport.loadInteger(element, "fraction").orElse(null);

        if (integerValue == null) {
            throw new IllegalArgumentException("Integer value is not defined");
        }
        if (fractionValue == null) {
            throw new IllegalArgumentException("Fraction value is not defined");
        }

        DigitsValidator validator = applicationContext.getBean(DigitsValidator.class, integerValue, fractionValue);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureValidator(Element element, String messageGroup) {
        FutureValidator validator = applicationContext.getBean(FutureValidator.class);

        loaderSupport.loadBoolean(element, "checkSeconds", validator::setCheckSeconds);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureOrPresentValidator(Element element, String messageGroup) {
        FutureOrPresentValidator validator = applicationContext.getBean(FutureOrPresentValidator.class);

        loaderSupport.loadBoolean(element, "checkSeconds", validator::setCheckSeconds);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastValidator(Element element, String messageGroup) {
        PastValidator validator = applicationContext.getBean(PastValidator.class);

        loaderSupport.loadBoolean(element, "checkSeconds", validator::setCheckSeconds);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastOrPresentValidator(Element element, String messageGroup) {
        PastOrPresentValidator validator = applicationContext.getBean(PastOrPresentValidator.class);

        loaderSupport.loadBoolean(element, "checkSeconds", validator::setCheckSeconds);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMaxValidator(Element element, String messageGroup) {
        Integer maxValue = loaderSupport.loadInteger(element, "value").orElse(null);
        if (maxValue == null) {
            throw new IllegalArgumentException("Max value is not defined");
        }

        MaxValidator validator = applicationContext.getBean(MaxValidator.class, maxValue);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMinValidator(Element element, String messageGroup) {
        Integer minValue = loaderSupport.loadInteger(element, "value").orElse(null);
        if (minValue == null) {
            throw new IllegalArgumentException("Min value is not defined");
        }

        MinValidator validator = applicationContext.getBean(MinValidator.class, minValue);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadValidatorWithoutAttributes(Element element, String messageGroup) {
        AbstractValidator validator;
        switch (element.getName()) {
            case "dateTimeRangeValidator":
                validator = applicationContext.getBean(DateTimeRangeValidator.class);
                break;
            case "email":
                validator = applicationContext.getBean(EmailValidator.class);
                break;
            case "negativeOrZero":
                validator = applicationContext.getBean(NegativeOrZeroValidator.class);
                break;
            case "negative":
                validator = applicationContext.getBean(NegativeValidator.class);
                break;
            case "notBlank":
                validator = applicationContext.getBean(NotBlankValidator.class);
                break;
            case "notEmpty":
                validator = applicationContext.getBean(NotEmptyValidator.class);
                break;
            case "notNull":
                validator = applicationContext.getBean(NotNullValidator.class);
                break;
            case "positiveOrZero":
                validator = applicationContext.getBean(PositiveOrZeroValidator.class);
                break;
            case "positive":
                validator = applicationContext.getBean(PositiveValidator.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown validator element: " + element.getName());
        }

        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadRegexpValidator(Element element, String messageGroup) {
        String regexp = loaderSupport.loadString(element, "regexp").orElse(null);
        Preconditions.checkNotNullArgument(regexp);

        RegexpValidator validator = applicationContext.getBean(RegexpValidator.class, regexp);

        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadSizeValidator(Element element, String messageGroup) {
        SizeValidator validator = applicationContext.getBean(SizeValidator.class);

        loaderSupport.loadInteger(element, "min", validator::setMin);
        loaderSupport.loadInteger(element, "max", validator::setMax);
        loaderSupport.loadResourceString(element, "message", messageGroup, validator::setMessage);

        return validator;
    }
}
