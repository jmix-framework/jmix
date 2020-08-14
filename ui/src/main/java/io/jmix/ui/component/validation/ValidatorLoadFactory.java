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

package io.jmix.ui.component.validation;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationContext;
import io.jmix.core.MessageTools;
import io.jmix.core.common.util.Preconditions;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Specific bean for loading validators.
 */
@Component(ValidatorLoadFactory.NAME)
public class ValidatorLoadFactory {
    public static final String NAME = "ui_ValidatorFactory";

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;

    protected final Map<String, BiFunction<Element, String, Validator>> validatorsMap
            = ImmutableMap.<String, BiFunction<Element, String, Validator>>builder()
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

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    protected void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    /**
     * Creates validator from XML element.
     *
     * @param element     validator element
     * @param messagePack message pack
     * @return validator or null if there is no such element
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public Validator createValidator(Element element, String messagePack) {
        BiFunction<Element, String, Validator> function = validatorsMap.get(element.getName());
        if (function != null) {
            return function.apply(element, messagePack);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadCustomValidator(Element element, String messagePack) {
        String beanName = element.attributeValue("bean");
        if (Strings.isNullOrEmpty(beanName)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        Validator validator = (Validator) applicationContext.getBean(beanName);

        if (validator instanceof AbstractValidator) {
            ((AbstractValidator) validator).setMessage(loadMessage(element, messagePack));
        }

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMinValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        BigDecimal decimalValue = new BigDecimal(value);
        DecimalMinValidator validator = (DecimalMinValidator) applicationContext.getBean(DecimalMinValidator.NAME, decimalValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMaxValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        BigDecimal decimalValue = new BigDecimal(value);
        DecimalMaxValidator validator = (DecimalMaxValidator) applicationContext.getBean(DecimalMaxValidator.NAME, decimalValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMinValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        Double doubleValue = Double.valueOf(value);
        DoubleMinValidator validator = (DoubleMinValidator) applicationContext.getBean(DoubleMinValidator.NAME, doubleValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMaxValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        Double doubleValue = Double.valueOf(value);
        DoubleMaxValidator validator = (DoubleMaxValidator) applicationContext.getBean(DoubleMaxValidator.NAME, doubleValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDigitsValidator(Element element, String messagePack) {
        String integer = element.attributeValue("integer");
        if (Strings.isNullOrEmpty(integer)) {
            throw new IllegalArgumentException("Integer value is not defined");
        }
        int integerValue = Integer.parseInt(integer);

        String fraction = element.attributeValue("fraction");
        if (Strings.isNullOrEmpty(fraction)) {
            throw new IllegalArgumentException("Fraction value is not defined");
        }
        int fractionValue = Integer.parseInt(fraction);

        DigitsValidator validator = (DigitsValidator) applicationContext.getBean(DigitsValidator.NAME, integerValue, fractionValue);
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureValidator(Element element, String messagePack) {
        FutureValidator validator = (FutureValidator) applicationContext.getBean(FutureValidator.NAME);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureOrPresentValidator(Element element, String messagePack) {
        FutureOrPresentValidator validator = (FutureOrPresentValidator) applicationContext.getBean(FutureOrPresentValidator.NAME);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastValidator(Element element, String messagePack) {
        PastValidator validator = (PastValidator) applicationContext.getBean(PastValidator.NAME);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastOrPresentValidator(Element element, String messagePack) {
        PastOrPresentValidator validator = (PastOrPresentValidator) applicationContext.getBean(PastOrPresentValidator.NAME);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMaxValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        long maxValue = Long.parseLong(value);

        MaxValidator validator = (MaxValidator) applicationContext.getBean(MaxValidator.NAME, maxValue);
        validator.setMessage(loadMessage(element, messagePack));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMinValidator(Element element, String messagePack) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        long minValue = Long.parseLong(value);

        MinValidator validator = (MinValidator) applicationContext.getBean(MinValidator.NAME, minValue);
        validator.setMessage(loadMessage(element, messagePack));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadValidatorWithoutAttributes(Element element, String messagePack) {
        AbstractValidator validator;
        switch (element.getName()) {
            case "email":
                validator = (AbstractValidator) applicationContext.getBean(EmailValidator.NAME);
                break;
            case "negativeOrZero":
                validator = (AbstractValidator) applicationContext.getBean(NegativeOrZeroValidator.NAME);
                break;
            case "negative":
                validator = (AbstractValidator) applicationContext.getBean(NegativeValidator.NAME);
                break;
            case "notBlank":
                validator = (AbstractValidator) applicationContext.getBean(NotBlankValidator.NAME);
                break;
            case "notEmpty":
                validator = (AbstractValidator) applicationContext.getBean(NotEmptyValidator.NAME);
                break;
            case "notNull":
                validator = (AbstractValidator) applicationContext.getBean(NotNullValidator.NAME);
                break;
            case "positiveOrZero":
                validator = (AbstractValidator) applicationContext.getBean(PositiveOrZeroValidator.NAME);
                break;
            case "positive":
                validator = (AbstractValidator) applicationContext.getBean(PositiveValidator.NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown validator element: " + element.getName());
        }
        validator.setMessage(loadMessage(element, messagePack));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadRegexpValidator(Element element, String messagePack) {
        String regexp = element.attributeValue("regexp");

        Preconditions.checkNotNullArgument(regexp);

        RegexpValidator validator = (RegexpValidator) applicationContext.getBean(RegexpValidator.NAME, regexp);
        validator.setMessage(loadMessage(element, messagePack));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadSizeValidator(Element element, String messagePack) {
        SizeValidator validator = (SizeValidator) applicationContext.getBean(SizeValidator.NAME);

        String min = element.attributeValue("min");
        if (min != null) {
            validator.setMin(Integer.parseInt(min));
        }
        String max = element.attributeValue("max");
        if (max != null) {
            validator.setMax(Integer.parseInt(max));
        }

        validator.setMessage(loadMessage(element, messagePack));
        return validator;
    }

    @Nullable
    protected Boolean loadInclusive(Element element) {
        String inclusive = element.attributeValue("inclusive");
        if (inclusive != null) {
            return Boolean.parseBoolean(inclusive);
        }
        return null;
    }

    @Nullable
    protected Boolean loadCheckSeconds(Element element) {
        String checkSeconds = element.attributeValue("checkSeconds");
        if (checkSeconds != null) {
            return Boolean.parseBoolean(checkSeconds);
        }
        return null;
    }

    @Nullable
    protected String loadMessage(Element element, String messagePack) {
        String message = element.attributeValue("message");
        if (!Strings.isNullOrEmpty(message)) {
            return messageTools.loadString(messagePack, message);
        }
        return null;
    }
}
