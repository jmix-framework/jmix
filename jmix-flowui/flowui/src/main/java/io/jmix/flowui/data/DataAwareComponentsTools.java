/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.data;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.timepicker.TimePicker;
import io.jmix.core.DateTimeTransformations;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.annotation.IgnoreUserTimeZone;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.component.HasLengthLimited;
import io.jmix.flowui.component.HasZoneId;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Utility bean that provides typical data aware operations with UI components.
 */
@Component("flowui_DataAwareComponentsTools")
public class DataAwareComponentsTools {

    protected final CurrentAuthentication currentAuthentication;
    protected final MessageTools messageTools;
    protected final TimeSource timeSource;
    protected final DateTimeTransformations dateTimeTransformations;
    protected final MetadataTools metadataTools;

    public DataAwareComponentsTools(CurrentAuthentication currentAuthentication,
                                    MessageTools messageTools,
                                    TimeSource timeSource,
                                    DateTimeTransformations dateTimeTransformations,
                                    MetadataTools metadataTools) {
        this.currentAuthentication = currentAuthentication;
        this.messageTools = messageTools;
        this.timeSource = timeSource;
        this.dateTimeTransformations = dateTimeTransformations;
        this.metadataTools = metadataTools;
    }

    /**
     * Sets length limits for textual UI component using Entity metadata.
     *
     * @param component   UI component
     * @param valueSource value source
     */
    public void setupLength(HasLengthLimited component, EntityValueSource<?, ?> valueSource) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
        Map<String, Object> annotations = metaProperty.getAnnotations();

        Integer maxLength = (Integer) annotations.get(MetadataTools.LENGTH_ANN_NAME);
        if (maxLength != null) {
            component.setMaxLength(maxLength);
        }

        Integer sizeMax = (Integer) annotations.get(Size.class.getName() + "_max");
        if (sizeMax != null) {
            component.setMaxLength(sizeMax);
        }

        Integer sizeMin = (Integer) annotations.get(Size.class.getName() + "_min");
        if (sizeMin != null) {
            component.setMinLength(sizeMin);
        }

        Integer lengthMax = (Integer) annotations.get(Length.class.getName() + "_max");
        if (lengthMax != null) {
            component.setMaxLength(lengthMax);
        }

        Integer lengthMin = (Integer) annotations.get(Length.class.getName() + "_min");
        if (lengthMin != null) {
            component.setMinLength(lengthMin);
        }
    }

    /**
     * Sets {@link ZoneId} for {@link HasZoneId} UI component using Entity metadata.
     *
     * @param component   UI component
     * @param valueSource value source
     */
    public void setupZoneId(HasZoneId component, EntityValueSource<?, ?> valueSource) {
        if (component.getZoneId() == null) {
            MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
            Class<?> javaType = metaProperty.getRange().asDatatype().getJavaClass();

            if (dateTimeTransformations.isDateTypeSupportsTimeZones(javaType)) {
                Boolean ignoreUserTimeZone = metadataTools.getMetaAnnotationValue(metaProperty,
                        IgnoreUserTimeZone.class);
                if (!Boolean.TRUE.equals(ignoreUserTimeZone)) {
                    TimeZone timeZone = currentAuthentication.getTimeZone();
                    component.setZoneId(timeZone.toZoneId());
                }
            }
        }
    }

    /**
     * Sets the selectable date range in {@link LocalDateTime} format for {@link DateTimePicker} component
     * using Entity metadata.
     *
     * @param component   {@link DateTimePicker} component
     * @param valueSource value source
     */
    public void setupRange(DateTimePicker component, EntityValueSource<?, ?> valueSource) {
        getMaxRange(component.getMax(), valueSource, LocalDateTime.class)
                .ifPresent(component::setMax);
        getMinRange(component.getMin(), valueSource, LocalDateTime.class)
                .ifPresent(component::setMin);
    }

    /**
     * Sets the selectable date range in {@link LocalDate} format for {@link DatePicker} component
     * using Entity metadata.
     *
     * @param component   {@link DatePicker} component
     * @param valueSource value source
     */
    public void setupRange(DatePicker component, EntityValueSource<?, ?> valueSource) {
        getMaxRange(component.getMax(), valueSource, LocalDate.class)
                .ifPresent(component::setMax);
        getMinRange(component.getMin(), valueSource, LocalDate.class)
                .ifPresent(component::setMin);
    }

    /**
     * Sets the selectable time range in {@link LocalTime} format for {@link TimePicker} component
     * using Entity metadata.
     *
     * @param component   {@link TimePicker} component
     * @param valueSource value source
     */
    public void setupRange(TimePicker component, EntityValueSource<?, ?> valueSource) {
        getMaxRange(component.getMax(), valueSource, LocalTime.class)
                .ifPresent(component::setMax);
        getMinRange(component.getMin(), valueSource, LocalTime.class)
                .ifPresent(component::setMin);
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> getMaxRange(@Nullable Object currentMaxValue, EntityValueSource<?, ?> valueSource,
                                          Class<T> datatype) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();

        if (currentMaxValue == null
                && (metaProperty.getAnnotations().get(Past.class.getName()) != null
                || metaProperty.getAnnotations().get(PastOrPresent.class.getName()) != null)) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            return Optional.of((T) dateTimeTransformations.transformFromZDT(zonedDateTime, datatype));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> getMinRange(@Nullable Object currentMinValue, EntityValueSource<?, ?> valueSource,
                                          Class<T> datatype) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
        Class<?> javaType = metaProperty.getRange().asDatatype().getJavaClass();
        TemporalType temporalType = getTemporalType(metaProperty, javaType);

        if (currentMinValue == null
                && metaProperty.getAnnotations().get(Future.class.getName()) != null) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            // In case of date and time we can select the current date with future time,
            // so we start from the next day only if the time isn't displayed
            if (temporalType == TemporalType.DATE) {
                dateTime = dateTime.plusDays(1);
            }
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            return Optional.of((T) dateTimeTransformations.transformFromZDT(zonedDateTime, datatype));

        } else if (currentMinValue == null
                && metaProperty.getAnnotations().get(FutureOrPresent.class.getName()) != null) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            return Optional.of((T) dateTimeTransformations.transformFromZDT(zonedDateTime, datatype));
        }

        return Optional.empty();
    }

    @Nullable
    protected TemporalType getTemporalType(@Nullable MetaProperty metaProperty, Class<?> javaType) {
        TemporalType temporalType = null;

        if (java.sql.Date.class.equals(javaType) || LocalDate.class.equals(javaType)) {
            temporalType = TemporalType.DATE;
        } else if (metaProperty != null) {
            temporalType = (TemporalType) metaProperty.getAnnotations().get(MetadataTools.TEMPORAL_ANN_NAME);
        }
        return temporalType;
    }
}
