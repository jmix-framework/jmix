/*
 * Copyright 2020 Haulmont.
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

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.ConstraintDefinitionContext;
import org.hibernate.validator.internal.constraintvalidators.bv.time.future.*;
import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.*;
import org.hibernate.validator.internal.constraintvalidators.bv.time.past.*;
import org.hibernate.validator.internal.constraintvalidators.bv.time.pastorpresent.*;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ClockProvider;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.Date;

public class JmixLocalValidatorFactoryBean extends LocalValidatorFactoryBean {
    protected ClockProvider clockProvider;
    protected MessageInterpolator jmixMessageInterpolator;

    public void setClockProvider(ClockProvider clockProvider) {
        this.clockProvider = clockProvider;
    }

    public void setJmixMessageInterpolator(MessageInterpolator messageInterpolator) {
        this.jmixMessageInterpolator = messageInterpolator;
    }

    @Override
    protected void postProcessConfiguration(jakarta.validation.Configuration<?> configuration) {
        super.postProcessConfiguration(configuration);

        if (clockProvider != null) {
            configuration.clockProvider(clockProvider);
        }

        // Set message interpolator explicitly as in the configuration
        // it is wrapped by LocaleContextMessageInterpolator that return
        // messages in a locale different from the locale of logged-in user

        if (jmixMessageInterpolator != null) {
            configuration.messageInterpolator(jmixMessageInterpolator);
        }

        ConstraintMapping constraintMapping = ((HibernateValidatorConfiguration) configuration).createConstraintMapping();

        //Hibernate validators doesn't support java.sql.Date.
        //Replace standard validators for java.util.Date with support java.sql.Date
        registerPastValidators(constraintMapping.constraintDefinition(Past.class));
        registerPastOrPresentValidators(constraintMapping.constraintDefinition(PastOrPresent.class));
        registerFutureValidators(constraintMapping.constraintDefinition(Future.class));
        registerFutureOrPresentValidators(constraintMapping.constraintDefinition(FutureOrPresent.class));

        ((HibernateValidatorConfiguration) configuration).addMapping(constraintMapping);
    }

    protected void registerPastValidators(ConstraintDefinitionContext<Past> context) {
        context.includeExistingValidators(false)
                .validatedBy(PastValidatorForCalendar.class)
                .validatedBy(JmixPastValidatorForDate.class)
                // Java 8 date/time API validators
                .validatedBy(PastValidatorForHijrahDate.class)
                .validatedBy(PastValidatorForInstant.class)
                .validatedBy(PastValidatorForJapaneseDate.class)
                .validatedBy(PastValidatorForLocalDate.class)
                .validatedBy(PastValidatorForLocalDateTime.class)
                .validatedBy(PastValidatorForLocalTime.class)
                .validatedBy(PastValidatorForMinguoDate.class)
                .validatedBy(PastValidatorForMonthDay.class)
                .validatedBy(PastValidatorForOffsetDateTime.class)
                .validatedBy(PastValidatorForOffsetTime.class)
                .validatedBy(PastValidatorForThaiBuddhistDate.class)
                .validatedBy(PastValidatorForYear.class)
                .validatedBy(PastValidatorForYearMonth.class)
                .validatedBy(PastValidatorForZonedDateTime.class);
    }

    protected void registerPastOrPresentValidators(ConstraintDefinitionContext<PastOrPresent> context) {
        context.includeExistingValidators(false)
                .validatedBy(PastOrPresentValidatorForCalendar.class)
                .validatedBy(JmixPastOrPresentValidatorForDate.class)
                // Java 8 date/time API validators
                .validatedBy(PastOrPresentValidatorForHijrahDate.class)
                .validatedBy(PastOrPresentValidatorForInstant.class)
                .validatedBy(PastOrPresentValidatorForJapaneseDate.class)
                .validatedBy(PastOrPresentValidatorForLocalDate.class)
                .validatedBy(PastOrPresentValidatorForLocalDateTime.class)
                .validatedBy(PastOrPresentValidatorForLocalTime.class)
                .validatedBy(PastOrPresentValidatorForMinguoDate.class)
                .validatedBy(PastOrPresentValidatorForMonthDay.class)
                .validatedBy(PastOrPresentValidatorForOffsetDateTime.class)
                .validatedBy(PastOrPresentValidatorForOffsetTime.class)
                .validatedBy(PastOrPresentValidatorForThaiBuddhistDate.class)
                .validatedBy(PastOrPresentValidatorForYear.class)
                .validatedBy(PastOrPresentValidatorForYearMonth.class)
                .validatedBy(PastOrPresentValidatorForZonedDateTime.class);
    }

    protected void registerFutureValidators(ConstraintDefinitionContext<Future> context) {
        context.includeExistingValidators(false)
                .validatedBy(FutureValidatorForCalendar.class)
                .validatedBy(JmixFutureValidatorForDate.class)
                // Java 8 date/time API validators
                .validatedBy(FutureValidatorForHijrahDate.class)
                .validatedBy(FutureValidatorForInstant.class)
                .validatedBy(FutureValidatorForJapaneseDate.class)
                .validatedBy(FutureValidatorForLocalDate.class)
                .validatedBy(FutureValidatorForLocalDateTime.class)
                .validatedBy(FutureValidatorForLocalTime.class)
                .validatedBy(FutureValidatorForMinguoDate.class)
                .validatedBy(FutureValidatorForMonthDay.class)
                .validatedBy(FutureValidatorForOffsetDateTime.class)
                .validatedBy(FutureValidatorForOffsetTime.class)
                .validatedBy(FutureValidatorForThaiBuddhistDate.class)
                .validatedBy(FutureValidatorForYear.class)
                .validatedBy(FutureValidatorForYearMonth.class)
                .validatedBy(FutureValidatorForZonedDateTime.class);
    }

    protected void registerFutureOrPresentValidators(ConstraintDefinitionContext<FutureOrPresent> context) {
        context.includeExistingValidators(false)
                .validatedBy(FutureOrPresentValidatorForCalendar.class)
                .validatedBy(JmixFutureOrPresentValidatorForDate.class)
                // Java 8 date/time API validators
                .validatedBy(FutureOrPresentValidatorForHijrahDate.class)
                .validatedBy(FutureOrPresentValidatorForInstant.class)
                .validatedBy(FutureOrPresentValidatorForJapaneseDate.class)
                .validatedBy(FutureOrPresentValidatorForLocalDate.class)
                .validatedBy(FutureOrPresentValidatorForLocalDateTime.class)
                .validatedBy(FutureOrPresentValidatorForLocalTime.class)
                .validatedBy(FutureOrPresentValidatorForMinguoDate.class)
                .validatedBy(FutureOrPresentValidatorForMonthDay.class)
                .validatedBy(FutureOrPresentValidatorForOffsetDateTime.class)
                .validatedBy(FutureOrPresentValidatorForOffsetTime.class)
                .validatedBy(FutureOrPresentValidatorForThaiBuddhistDate.class)
                .validatedBy(FutureOrPresentValidatorForYear.class)
                .validatedBy(FutureOrPresentValidatorForYearMonth.class)
                .validatedBy(FutureOrPresentValidatorForZonedDateTime.class);
    }

    protected static class JmixPastValidatorForDate extends PastValidatorForDate {
        public JmixPastValidatorForDate() {
        }

        @Override
        protected Instant getInstant(Date value) {
            if (value instanceof java.sql.Date) {
                return Instant.ofEpochMilli(value.getTime());
            } else {
                return super.getInstant(value);
            }
        }
    }

    protected static class JmixPastOrPresentValidatorForDate extends PastOrPresentValidatorForDate {
        public JmixPastOrPresentValidatorForDate() {
        }

        @Override
        protected Instant getInstant(Date value) {
            if (value instanceof java.sql.Date) {
                return Instant.ofEpochMilli(value.getTime());
            } else {
                return super.getInstant(value);
            }
        }
    }

    protected static class JmixFutureValidatorForDate extends FutureValidatorForDate {
        public JmixFutureValidatorForDate() {
        }

        @Override
        protected Instant getInstant(Date value) {
            if (value instanceof java.sql.Date) {
                return Instant.ofEpochMilli(value.getTime());
            } else {
                return super.getInstant(value);
            }
        }
    }

    protected static class JmixFutureOrPresentValidatorForDate extends FutureOrPresentValidatorForDate {
        public JmixFutureOrPresentValidatorForDate() {
        }

        @Override
        protected Instant getInstant(Date value) {
            if (value instanceof java.sql.Date) {
                return Instant.ofEpochMilli(value.getTime());
            } else {
                return super.getInstant(value);
            }
        }
    }
}
