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

package io.jmix.flowui.app.inputdialog;

import com.vaadin.flow.component.Component;
import io.jmix.core.FileRef;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.DateTimeDatatype;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.datatype.impl.OffsetDateTimeDatatype;

import org.springframework.lang.Nullable;
import java.math.BigDecimal;
import java.time.*;
import java.util.TimeZone;
import java.util.function.Supplier;

/**
 * Describes field that can be used in {@link InputDialog}.
 */
public class InputParameter {

    protected String id;
    protected String label;
    protected String requiredMessage;
    protected boolean required;
    protected Datatype<?> datatype;
    protected Supplier<Component> field;
    protected Object defaultValue;
    protected Class<?> entityClass;
    protected Class<? extends EnumClass<?>> enumClass;

    protected TimeZone timeZone;
    protected boolean useUserTimeZone = false;

    protected Class<?> datatypeJavaClass;

    /**
     * @param id field id
     */
    public InputParameter(String id) {
        Preconditions.checkNotNullArgument(id);

        this.id = id;
    }

    /**
     * @return field id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets field id.
     *
     * @param id field id
     * @return input parameter
     */
    public InputParameter withId(String id) {
        Preconditions.checkNotNullArgument(id);

        this.id = id;
        return this;
    }

    /**
     * @return field caption
     */
    @Nullable
    public String getLabel() {
        return label;
    }

    /**
     * Sets label to the field.
     *
     * @param label label
     * @return input parameter
     */
    public InputParameter withLabel(@Nullable String label) {
        this.label = label;
        return this;
    }

    /**
     * @return true if field is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets required for the field.
     *
     * @param required required option
     * @return input parameter
     */
    public InputParameter withRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * @return message that will be shown if the field is not filled, and it is
     * {@link #isRequired()} or {@code null} if not set
     */
    @Nullable
    public String getRequiredMessage() {
        return requiredMessage;
    }

    /**
     * Sets message that will be shown if the field is not filled, and it is {@link #isRequired()}.
     *
     * @param requiredMessage message
     * @return input parameter
     */
    public InputParameter withRequiredMessage(@Nullable String requiredMessage) {
        this.requiredMessage = requiredMessage;
        return this;
    }

    /**
     * @return field Datatype
     */
    @Nullable
    public Datatype<?> getDatatype() {
        return datatype;
    }

    /**
     * Sets datatype to the field. Cannot be used with {@link #withEntityClass(Class)}, {@link #withEnumClass(Class)}
     * and with predefined static methods.
     * <p>
     * Note, it doesn't support custom Datatype. Use {@link #withField(Supplier)}.
     *
     * @param datatype datatype
     * @return input parameter
     */
    public InputParameter withDatatype(Datatype<?> datatype) {
        checkNullEntityClass("Datatype cannot be used with a parameter that has already contained entity class");
        checkNullDatatypeJavaClass("Datatype cannot be used with a parameter that has already contained data type");
        checkNullEnumClass("Datatype cannot be used with a parameter that has already contained enum class");

        this.datatype = datatype;
        return this;
    }

    protected InputParameter withDatatypeJavaClass(Class<?> javaClass) {
        this.datatypeJavaClass = javaClass;
        return this;
    }

    @Nullable
    public Class<?> getDatatypeJavaClass() {
        return datatypeJavaClass;
    }

    /**
     * @return field supplier
     */
    @Nullable
    public Supplier<Component> getField() {
        return field;
    }

    /**
     * Sets field supplier.
     * <p>
     * Note, in order to get value from this field you must use an id that is set to the InputParameter, not to the
     * created field.
     *
     * @param field supplier
     * @return input parameter
     */
    public InputParameter withField(Supplier<Component> field) {
        this.field = field;
        return this;
    }

    /**
     * @return default value
     */
    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets default value to the field.
     *
     * @param defaultValue default value
     * @return input parameter
     */
    public InputParameter withDefaultValue(@Nullable Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * @return entity class
     */
    @Nullable
    public Class<?> getEntityClass() {
        return entityClass;
    }

    /**
     * Sets entity class. Cannot be used with {@link #withDatatype(Datatype)}, {@link #withEnumClass(Class)}
     * and with predefined static methods.
     *
     * @param entityClass entity class
     * @return input parameter
     */
    public InputParameter withEntityClass(Class<?> entityClass) {
        checkNullDatatype("Entity class cannot be used with a parameter that has already contained Datatype");
        checkNullDatatypeJavaClass("Entity class cannot be used with a parameter that has already contained data type");
        checkNullEnumClass("Entity class cannot be used with a parameter that has already contained enum class");

        this.entityClass = entityClass;
        return this;
    }

    /**
     * @return enum class
     */
    @Nullable
    public Class<? extends EnumClass<?>> getEnumClass() {
        return enumClass;
    }

    /**
     * Sets enum class. Cannot be used with {@link #withDatatype(Datatype)}, {@link #withEntityClass(Class)}
     * and with predefined static methods.
     *
     * @param enumClass enum class
     * @return input parameter
     */
    public InputParameter withEnumClass(Class<? extends EnumClass<?>> enumClass) {
        checkNullDatatype("Enum class cannot be used with a parameter that has already contained Datatype");
        checkNullDatatypeJavaClass("Enum class cannot be used with a parameter that has already contained data type");
        checkNullEntityClass("Enum class cannot be used with a parameter that has already contained entity class");

        this.enumClass = enumClass;
        return this;
    }

    /**
     * @return time zone
     */
    @Nullable
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets time zone to the parameter. InputDialog supports time zones for
     * <ul>
     *     <li>{@link OffsetDateTimeDatatype}</li>
     *     <li>{@link DateTimeDatatype}</li>
     * </ul>
     * If time zone not set and {@link #isUseUserTimeZone()} is false, component will use system time zone.
     *
     * @param timeZone time zone to use
     * @return input parameter
     * @see #withUseUserTimeZone(boolean)
     */
    public InputParameter withTimeZone(@Nullable TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * @return {@code true} if the parameter uses user's {@link TimeZone}.
     */
    public boolean isUseUserTimeZone() {
        return useUserTimeZone;
    }

    /**
     * Sets to the parameter whether to use user's {@link TimeZone}. InputDialog supports time zones for
     * <ul>
     *     <li>{@link OffsetDateTimeDatatype}</li>
     *     <li>{@link DateTimeDatatype}</li>
     * </ul>
     * Default value is {@code false}. If users' time zone is not used and time zone is not set, component will use
     * system time zone.
     *
     * @param useUserTimeZone whether to use user's time zone or not
     * @return input parameter
     * @see #withTimeZone(TimeZone)
     */
    public InputParameter withUseUserTimeZone(boolean useUserTimeZone) {
        this.useUserTimeZone = useUserTimeZone;
        return this;
    }

    /**
     * Creates parameter with String type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter parameter(String id) {
        return new InputParameter(id);
    }

    /**
     * Creates parameter with String type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter stringParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(String.class);
    }

    /**
     * Creates parameter with Integer type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter intParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(Integer.class);
    }

    /**
     * Creates parameter with Double type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter doubleParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(Double.class);
    }

    /**
     * Creates parameter with BigDecimal type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter bigDecimalParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(BigDecimal.class);
    }

    /**
     * Creates parameter with Long type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter longParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(Long.class);
    }

    /**
     * Creates parameter with DateTime type. To enable time zones for the component use
     * {@link #withUseUserTimeZone(boolean)} or {@link #withTimeZone(TimeZone)}.
     * <p>
     * If {@link #isUseUserTimeZone()} is false and time zone is not set, component will use system time zone.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter dateTimeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalDateTime.class);
    }

    /**
     * Creates parameter with Date type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter dateParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalDate.class);
    }

    /**
     * Creates parameter with Time type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter timeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalTime.class);
    }

    /**
     * Creates parameter with Entity type.
     *
     * @param id          field id
     * @param entityClass entity class
     * @return input parameter
     */
    public static InputParameter entityParameter(String id, Class<?> entityClass) {
        return new InputParameter(id)
                .withEntityClass(entityClass);
    }

    /**
     * Creates parameter with Boolean type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter booleanParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(Boolean.class);
    }

    /**
     * Creates parameter with byte[] type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter byteArrayParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(byte[].class);
    }

    /**
     * Creates parameter with FileRef (file reference) type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter fileParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(FileRef.class);
    }

    /**
     * Creates parameter with Enum type.
     *
     * @param id        field id
     * @param enumClass enum class
     * @return input parameter
     */
    public static InputParameter enumParameter(String id, Class<? extends EnumClass<?>> enumClass) {
        return new InputParameter(id)
                .withEnumClass(enumClass);
    }

    /**
     * Creates parameter with {@link LocalDate} type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter localDateParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalDate.class);
    }

    /**
     * Creates parameter with {@link LocalTime} type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter localTimeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalTime.class);
    }

    /**
     * Creates parameter with {@link LocalDateTime} type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter localDateTimeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(LocalDateTime.class);
    }

    /**
     * Creates parameter with {@link OffsetDateTime} type. To enable time zones for the component use
     * {@link #withUseUserTimeZone(boolean)} or {@link #withTimeZone(TimeZone)}.
     * <p>
     * If {@link #isUseUserTimeZone()} is false and time zone is not set, component will use system time zone.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter offsetDateTimeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(OffsetDateTime.class);
    }

    /**
     * Creates parameter with {@link OffsetTime} type.
     *
     * @param id field id
     * @return input parameter
     */
    public static InputParameter offsetTimeParameter(String id) {
        return new InputParameter(id)
                .withDatatypeJavaClass(OffsetTime.class);
    }

    protected void checkNullDatatype(String message) {
        if (datatype != null) {
            throw new IllegalStateException(message);
        }
    }

    protected void checkNullEntityClass(String message) {
        if (entityClass != null) {
            throw new IllegalStateException(message);
        }
    }

    protected void checkNullEnumClass(String message) {
        if (enumClass != null) {
            throw new IllegalStateException(message);
        }
    }

    protected void checkNullDatatypeJavaClass(String message) {
        if (datatypeJavaClass != null) {
            throw new IllegalStateException(message);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        InputParameter inputParameter = (InputParameter) obj;
        return id.equals(inputParameter.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
