package io.jmix.flowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import javax.annotation.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

public interface EntityValueSource<E, V> extends ValueSource<V>, EntityDataUnit {

    /**
     * @return entity
     */
    @Nullable
    E getItem();

    /**
     * @return property path
     */
    MetaPropertyPath getMetaPropertyPath();

    /**
     * @return true if data model security check is required on data binding
     */
    boolean isDataModelSecurityEnabled();

    Registration addInstanceChangeListener(Consumer<InstanceChangeEvent<E>> listener);

    /**
     * An event fired when related entity instance is changed.
     *
     * @param <E> entity type
     */
    class InstanceChangeEvent<E> extends EventObject {
        private final E prevItem;
        private final E item;

        public InstanceChangeEvent(EntityValueSource<E, ?> source, @Nullable E prevItem, @Nullable E item) {
            super(source);
            this.prevItem = prevItem;
            this.item = item;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityValueSource<E, ?> getSource() {
            return (EntityValueSource<E, ?>) super.getSource();
        }

        /**
         * @return current item
         */
        @Nullable
        public E getItem() {
            return item;
        }

        /**
         * @return previous selected item
         */
        @Nullable
        public E getPrevItem() {
            return prevItem;
        }
    }
}
