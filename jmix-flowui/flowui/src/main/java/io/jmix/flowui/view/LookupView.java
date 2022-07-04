package io.jmix.flowui.view;


import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface LookupView<E> {

    OperationResult handleSelection();

    OperationResult closeWithDiscard();

    LookupComponent<E> getLookupComponent();

    Optional<LookupComponent<E>> findLookupComponent();

    Optional<Consumer<Collection<E>>> getSelectionHandler();

    void setSelectionHandler(@Nullable Consumer<Collection<E>> selectionHandler);

    Optional<Predicate<ValidationContext<E>>> getSelectionValidator();

    void setSelectionValidator(@Nullable Predicate<ValidationContext<E>> selectionValidator);

    class ValidationContext<T> {
        protected final View<?> view;
        protected final Collection<T> selectedItems;

        public ValidationContext(View<?> view, Collection<T> selectedItems) {
            this.view = view;
            this.selectedItems = selectedItems;
        }

        public View<?> getView() {
            return view;
        }

        public Collection<T> getSelectedItems() {
            return selectedItems;
        }
    }
}
