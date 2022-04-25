package io.jmix.flowui.component.validation;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.SimilarToUi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SimilarToUi
public class ValidationErrors {

    protected List<Item> items = new ArrayList<>();

    /**
     * Add an error without reference to component causing it.
     *
     * @param description error description
     */
    public void add(String description) {
        add(null, description);
    }

    /**
     * Adds an error.
     *
     * @param component   component causing the error
     * @param description error description
     */
    public void add(@Nullable Component component, String description) {
        items.add(new Item(component, description));
    }

    /**
     * Adds an error item.
     *
     * @param item item to add
     */
    public void add(Item item) {
        items.add(item);
    }

    /**
     * Add all errors.
     *
     * @param errors errors
     */
    public void addAll(ValidationErrors errors) {
        items.addAll(errors.items);
    }

    /**
     * @return errors list
     */
    public List<Item> getAll() {
        return items;
    }

    /**
     * @return true if there are no errors
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * @return component of the first validation problem or null if no validation errors
     */
    @Nullable
    public Component getFirstComponent() {
        if (items.isEmpty()) {
            return null;
        }

        return items.get(0).component;
    }

    /**
     * Creates new object with one validation error.
     *
     * @param description error description
     * @return object with one validation error
     */
    public static ValidationErrors of(String description) {
        ValidationErrors errors = new ValidationErrors();
        errors.add(description);
        return errors;
    }

    /**
     * @return immutable empty object
     */
    public static ValidationErrors none() {
        return new ValidationErrors() {
            @Override
            public void add(String description) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(@Nullable Component component, String description) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addAll(ValidationErrors errors) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static class Item {
        protected final Component component;
        protected final String description;

        public Item(@Nullable Component component, String description) {
            this.component = component;
            this.description = description;
        }

        public Component getComponent() {
            return component;
        }

        public String getDescription() {
            return description;
        }
    }
}
