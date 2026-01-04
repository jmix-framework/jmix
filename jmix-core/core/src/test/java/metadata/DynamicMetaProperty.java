package metadata;

import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class DynamicMetaProperty extends MetadataObjectImpl implements MetaProperty {

    protected final MetaClass metaClass;
    protected final Range range;
    protected final Class<?> javaClass;
    protected final Boolean mandatory;
    protected final AnnotatedElement annotatedElement = new FakeAnnotatedElement();
    protected final Type type;
    protected Store store;

    public DynamicMetaProperty(MetaClass metaClass, String name, Class<?> javaClass, Range range, Type type) {
        this.metaClass = metaClass;
        this.name = name;
        this.range = range;
        this.javaClass = javaClass;
        this.type = type;
        this.mandatory = false;
        this.store = metaClass.getStore();
    }

    @Override
    public Session getSession() {
        return metaClass.getSession();
    }

    @Override
    public MetaClass getDomain() {
        return metaClass;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isMandatory() {
        return Boolean.TRUE.equals(mandatory);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public MetaProperty getInverse() {
        return null;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    @Override
    public Class<?> getJavaType() {
        return javaClass;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return null;
    }

    @Override
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    protected static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicMetaProperty)) return false;

        DynamicMetaProperty that = (DynamicMetaProperty) o;

        return metaClass.equals(that.metaClass) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return 31 * metaClass.hashCode() + name.hashCode();
    }
}
