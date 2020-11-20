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

package io.jmix.ui.component.data;

import io.jmix.core.DateTimeTransformations;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.annotation.CaseConversion;
import io.jmix.core.entity.annotation.ConversionType;
import io.jmix.core.entity.annotation.IgnoreUserTimeZone;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.HasRange;
import io.jmix.ui.component.OptionsField;
import io.jmix.ui.component.TextInputField;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.options.EnumOptions;
import org.apache.commons.collections4.MapUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;
import java.time.*;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utillity bean that provides typical data aware operations with UI components.
 */
@Component("ui_DataAwareComponentsTools")
public class DataAwareComponentsTools {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected DateTimeTransformations dateTimeTransformations;
    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Sets case conversion using {@link CaseConversion} annotation on entity property.
     *
     * @param component UI component
     * @param valueSource value source
     */
    public void setupCaseConversion(TextInputField.CaseConversionSupported component, EntityValueSource valueSource) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
        Map<String, Object> annotations = metaProperty.getAnnotations();

        String caseConversionAnnotation = CaseConversion.class.getName();
        //noinspection unchecked
        Map<String, Object> caseConversion = (Map<String, Object>) annotations.get(caseConversionAnnotation);
        if (MapUtils.isNotEmpty(caseConversion)) {
            ConversionType conversionType = (ConversionType) caseConversion.get("type");
            TextInputField.CaseConversion conversion = TextInputField.CaseConversion.valueOf(conversionType.name());

            component.setCaseConversion(conversion);
        }
    }

    /**
     * Sets max length for textual UI component using Entity metadata.
     *
     * @param component UI component
     * @param valueSource value source
     */
    public void setupMaxLength(TextInputField.MaxLengthLimited component, EntityValueSource valueSource) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
        Map<String, Object> annotations = metaProperty.getAnnotations();

        Integer maxLength = (Integer) annotations.get("length");
        if (maxLength != null) {
            component.setMaxLength(maxLength);
        }

        Integer sizeMax = (Integer) annotations.get(Size.class.getName() + "_max");
        if (sizeMax != null) {
            component.setMaxLength(sizeMax);
        }

        Integer lengthMax = (Integer) annotations.get(Length.class.getName() + "_max");
        if (lengthMax != null) {
            component.setMaxLength(lengthMax);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void setupDateRange(HasRange component, EntityValueSource valueSource) {
        MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
        Class javaType = metaProperty.getRange().asDatatype().getJavaClass();
        TemporalType temporalType = getTemporalType(metaProperty, javaType);

        if (component.getRangeEnd() == null
                && (metaProperty.getAnnotations().get(Past.class.getName()) != null
                || metaProperty.getAnnotations().get(PastOrPresent.class.getName()) != null)) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            component.setRangeEnd(dateTimeTransformations.transformFromZDT(zonedDateTime, javaType));
        } else if (component.getRangeStart() == null
                && metaProperty.getAnnotations().get(Future.class.getName()) != null) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            // In case of date and time we can select the current date with future time,
            // so we start from the next day only if the time isn't displayed
            if (temporalType == TemporalType.DATE) {
                dateTime = dateTime.plusDays(1);
            }
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            component.setRangeStart(dateTimeTransformations.transformFromZDT(zonedDateTime, javaType));
        } else if (component.getRangeStart() == null
                && metaProperty.getAnnotations().get(FutureOrPresent.class.getName()) != null) {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
            component.setRangeStart(dateTimeTransformations.transformFromZDT(zonedDateTime, javaType));
        }
    }

    public void setupZoneId(DateField component, EntityValueSource valueSource) {
        if (component.getZoneId() == null) {
            MetaProperty metaProperty = valueSource.getMetaPropertyPath().getMetaProperty();
            Class javaType = metaProperty.getRange().asDatatype().getJavaClass();
            if (dateTimeTransformations.isDateTypeSupportsTimeZones(javaType)) {
                Boolean ignoreUserTimeZone = metadataTools.getMetaAnnotationValue(metaProperty, IgnoreUserTimeZone.class);
                if (!Boolean.TRUE.equals(ignoreUserTimeZone)) {
                    TimeZone timeZone = currentAuthentication.getTimeZone();
                    component.setTimeZone(timeZone);
                }
            }
        }
    }

    public void setupDateFormat(DateField component, EntityValueSource valueSource) {
        setupDateFormat(component, valueSource.getMetaPropertyPath().getMetaProperty());
    }

    public void setupDateFormat(DateField component, MetaProperty metaProperty) {
        Class javaType = metaProperty.getRange().asDatatype().getJavaClass();

        TemporalType temporalType = getTemporalType(metaProperty, javaType);

        component.setResolution(temporalType == TemporalType.DATE
                ? DateField.Resolution.DAY
                : DateField.Resolution.MIN);

        String formatStr = messageTools.getDefaultDateFormat(temporalType);
        component.setDateFormat(formatStr);
    }

    protected TemporalType getTemporalType(MetaProperty metaProperty, Class javaType) {
        TemporalType temporalType = null;

        if (java.sql.Date.class.equals(javaType) || LocalDate.class.equals(javaType)) {
            temporalType = TemporalType.DATE;
        } else if (metaProperty.getAnnotations() != null) {
            temporalType = (TemporalType) metaProperty.getAnnotations().get(MetadataTools.TEMPORAL_ANN_NAME);
        }
        return temporalType;
    }

    /**
     * Throws IllegalArgumentException if component's {@link ValueSource} and {@link Datatype} have different types.
     *
     * @param datatype    datatype
     * @param valueSource component's value source
     */
    public void checkValueSourceDatatypeMismatch(@Nullable Datatype datatype, @Nullable ValueSource valueSource) {
        if (valueSource != null && datatype != null) {
            if (!valueSource.getType().equals(datatype.getJavaClass())) {
                throw new IllegalArgumentException("ValueSource and Datatype have different types. ValueSource:"
                        + valueSource.getType() + "; Datatype: " + datatype.getJavaClass());
            }
        }
    }

    public void setupOptions(OptionsField optionsField, EntityValueSource valueSource) {
        MetaPropertyPath propertyPath = valueSource.getMetaPropertyPath();
        MetaProperty metaProperty = propertyPath.getMetaProperty();

        if (metaProperty.getRange().isEnum()) {
            //noinspection unchecked
            optionsField.setOptions(new EnumOptions(metaProperty.getRange().asEnumeration().getJavaClass()));
        }
        // todo dynamic attributes
//        else if (DynamicAttributesUtils.isDynamicAttribute(metaProperty)) {
//            CategoryAttribute categoryAttribute = DynamicAttributesUtils.getCategoryAttribute(metaProperty);
//
//            if (categoryAttribute != null
//                    && categoryAttribute.getDataType() == PropertyType.ENUMERATION) {
//
//                //noinspection unchecked
//                optionsField.setOptionsMap(categoryAttribute.getLocalizedEnumerationMap());
//            }
//        }
    }
}
