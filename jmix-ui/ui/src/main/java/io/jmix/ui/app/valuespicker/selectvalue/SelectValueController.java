package io.jmix.ui.app.valuespicker.selectvalue;

import io.jmix.ui.component.DateField;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.validation.Validator;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public interface SelectValueController<V> {

    void setSelectValueContext(SelectValueContext<V> context);

    List<V> getValue();

    class SelectValueContext<V> {

        protected String entityName;
        protected Class<? extends Enum> enumClass;
        protected Class<?> javaClass;

        protected boolean fieldEditable = true;

        protected Collection<V> initialValues;

        protected String lookupScreenId;

        protected boolean useComboBox = false;

        protected Options<V> options;
        protected Function<V, String> optionCaptionProvider;
        protected List<Validator<V>> validators;

        protected DateField.Resolution resolution;
        protected TimeZone timeZone;

        @Nullable
        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(@Nullable String entityName) {
            this.entityName = entityName;
        }

        @Nullable
        public Class<? extends Enum> getEnumClass() {
            return enumClass;
        }

        public void setEnumClass(@Nullable Class<? extends Enum> enumClass) {
            this.enumClass = enumClass;
        }

        @Nullable
        public Class<?> getJavaClass() {
            return javaClass;
        }

        public void setJavaClass(@Nullable Class<?> javaClass) {
            this.javaClass = javaClass;
        }

        public boolean isFieldEditable() {
            return fieldEditable;
        }

        public void setFieldEditable(boolean editable) {
            this.fieldEditable = editable;
        }

        public Collection<V> getInitialValues() {
            return initialValues != null
                    ? Collections.unmodifiableCollection(initialValues)
                    : Collections.emptyList();
        }

        public void setInitialValues(@Nullable Collection<V> initialValues) {
            this.initialValues = initialValues;
        }

        @Nullable
        public String getLookupScreenId() {
            return lookupScreenId;
        }

        public void setLookupScreenId(@Nullable String lookupScreenId) {
            this.lookupScreenId = lookupScreenId;
        }

        public boolean isUseComboBox() {
            return useComboBox;
        }

        public void setUseComboBox(boolean useComboBox) {
            this.useComboBox = useComboBox;
        }

        @Nullable
        public Options<V> getOptions() {
            return options;
        }

        public void setOptions(@Nullable Options<V> options) {
            this.options = options;
        }

        @Nullable
        public Function<V, String> getOptionCaptionProvider() {
            return optionCaptionProvider;
        }

        public void setOptionCaptionProvider(@Nullable Function<V, String> optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;
        }

        public List<Validator<V>> getValidators() {
            return validators != null
                    ? Collections.unmodifiableList(validators)
                    : Collections.emptyList();
        }

        public void setValidators(@Nullable List<Validator<V>> validators) {
            this.validators = validators;
        }

        public void addValidator(Validator<V> validator) {
            if (validators == null) {
                validators = new ArrayList<>();
            }

            validators.add(validator);
        }

        @Nullable
        public DateField.Resolution getResolution() {
            return resolution;
        }

        public void setResolution(@Nullable DateField.Resolution resolution) {
            this.resolution = resolution;
        }

        @Nullable
        public TimeZone getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(@Nullable TimeZone timeZone) {
            this.timeZone = timeZone;
        }
    }
}
