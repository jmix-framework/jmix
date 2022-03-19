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
import io.jmix.core.MessageTools;
import io.jmix.core.common.util.Preconditions;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Specific bean for loading validators.
 */
@Component("ui_ValidatorFactory")
public class ValidatorLoadFactory {

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
     * @param element      validator element
     * @param messageGroup message group
     * @return validator or null if there is no such element
     */
    @SuppressWarnings("rawtypes")
    @Nullable
    public Validator createValidator(Element element, String messageGroup) {
        BiFunction<Element, String, Validator> function = validatorsMap.get(element.getName());
        if (function != null) {
            return function.apply(element, messageGroup);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadCustomValidator(Element element, String messageGroup) {
        String beanName = element.attributeValue("bean");
        if (Strings.isNullOrEmpty(beanName)) {
            throw new IllegalArgumentException("Bean name is not defined");
        }

        Validator validator = (Validator) applicationContext.getBean(beanName);

        if (validator instanceof AbstractValidator) {
            ((AbstractValidator) validator).setMessage(loadMessage(element, messageGroup));
        }

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMinValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        BigDecimal decimalValue = new BigDecimal(value);
        DecimalMinValidator validator = applicationContext.getBean(DecimalMinValidator.class, decimalValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDecimalMaxValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        BigDecimal decimalValue = new BigDecimal(value);
        DecimalMaxValidator validator = applicationContext.getBean(DecimalMaxValidator.class, decimalValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMinValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        Double doubleValue = Double.valueOf(value);
        DoubleMinValidator validator = applicationContext.getBean(DoubleMinValidator.class, doubleValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDoubleMaxValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        Double doubleValue = Double.valueOf(value);
        DoubleMaxValidator validator = applicationContext.getBean(DoubleMaxValidator.class, doubleValue);

        Boolean inclusive = loadInclusive(element);
        if (inclusive != null) {
            validator.setInclusive(inclusive);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadDigitsValidator(Element element, String messageGroup) {
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

        DigitsValidator validator = applicationContext.getBean(DigitsValidator.class, integerValue, fractionValue);
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureValidator(Element element, String messageGroup) {
        FutureValidator validator = applicationContext.getBean(FutureValidator.class);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadFutureOrPresentValidator(Element element, String messageGroup) {
        FutureOrPresentValidator validator = applicationContext.getBean(FutureOrPresentValidator.class);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastValidator(Element element, String messageGroup) {
        PastValidator validator = applicationContext.getBean(PastValidator.class);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadPastOrPresentValidator(Element element, String messageGroup) {
        PastOrPresentValidator validator = applicationContext.getBean(PastOrPresentValidator.class);

        Boolean checkSeconds = loadCheckSeconds(element);
        if (checkSeconds != null) {
            validator.setCheckSeconds(checkSeconds);
        }
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMaxValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Max value is not defined");
        }
        long maxValue = Long.parseLong(value);

        MaxValidator validator = applicationContext.getBean(MaxValidator.class, maxValue);
        validator.setMessage(loadMessage(element, messageGroup));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadMinValidator(Element element, String messageGroup) {
        String value = element.attributeValue("value");
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Min value is not defined");
        }
        long minValue = Long.parseLong(value);

        MinValidator validator = applicationContext.getBean(MinValidator.class, minValue);
        validator.setMessage(loadMessage(element, messageGroup));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadValidatorWithoutAttributes(Element element, String messageGroup) {
        AbstractValidator validator;
        switch (element.getName()) {
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
        validator.setMessage(loadMessage(element, messageGroup));
        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadRegexpValidator(Element element, String messageGroup) {
        String regexp = element.attributeValue("regexp");

        Preconditions.checkNotNullArgument(regexp);

        RegexpValidator validator = applicationContext.getBean(RegexpValidator.class, regexp);
        validator.setMessage(loadMessage(element, messageGroup));

        return validator;
    }

    @SuppressWarnings("rawtypes")
    protected Validator loadSizeValidator(Element element, String messageGroup) {
        SizeValidator validator = applicationContext.getBean(SizeValidator.class);

        String min = element.attributeValue("min");
        if (min != null) {
            validator.setMin(Integer.parseInt(min));
        }
        String max = element.attributeValue("max");
        if (max != null) {
            validator.setMax(Integer.parseInt(max));
        }

        validator.setMessage(loadMessage(element, messageGroup));
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
    protected String loadMessage(Element element, String messageGroup) {
        String message = element.attributeValue("message");
        if (!Strings.isNullOrEmpty(message)) {
            return messageTools.loadString(messageGroup, message);
        }
        return null;
    }
}
