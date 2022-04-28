package io.jmix.flowui.component.delegate;

import com.vaadin.flow.component.AbstractField;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.data.binding.impl.AbstractValueBinding;
import io.jmix.flowui.data.binding.impl.FieldValueBinding;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.component.SupportsMetaClass;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowui_EntityFieldDelegate")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EntityFieldDelegate<C extends AbstractField<?, V> & SupportsMetaClass, T, V>
        extends AbstractFieldDelegate<C, T, V> {

    protected MetaClass metaClass;

    public EntityFieldDelegate(C component) {
        super(component);
    }

    @Nullable
    public MetaClass getMetaClass() {
        ValueSource<T> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaProperty metaProperty = ((EntityValueSource<?, ?>) valueSource).getMetaPropertyPath().getMetaProperty();
            return metaProperty.getRange().asClass();
        } else {
            return metaClass;
        }
    }

    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<T> valueSource = getValueSource();
        if (valueSource != null) {
            throw new IllegalStateException(ValueSource.class.getSimpleName() + " is not null");
        }
        this.metaClass = metaClass;
    }

    public void checkValueType(@Nullable T value) {
        if (value == null) {
            return;
        }

        MetaClass metaClass = getMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("Neither metaClass nor valueSource is set for " +
                    component.getClass().getSimpleName());
        }

        Class<?> fieldClass = metaClass.getJavaClass();
        Class<?> valueClass = value.getClass();
        if (!fieldClass.isAssignableFrom(valueClass)) {
            throw new IllegalArgumentException(
                    String.format("Could not set value with class %s to field with class %s",
                            fieldClass.getCanonicalName(),
                            valueClass.getCanonicalName())
            );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractValueBinding<T> createValueBinding(ValueSource<T> valueSource) {
        return applicationContext.getBean(FieldValueBinding.class, valueSource, component);
    }
}
