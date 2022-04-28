package io.jmix.flowui.screen;


import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface LookupScreen<E> {

    OperationResult handleSelection();

    OperationResult closeWithDiscard();

    LookupComponent<E> getLookupComponent();

    Optional<LookupComponent<E>> findLookupComponent();

    Optional<Consumer<Collection<E>>> getSelectionHandler();

    void setSelectionHandler(@Nullable Consumer<Collection<E>> selectionHandler);

    Optional<Predicate<ValidationContext<E>>> getSelectionValidator();

    void setSelectionValidator(@Nullable Predicate<ValidationContext<E>> selectionValidator);

    class ValidationContext<T> {
        private final Screen screen;
        private final Collection<T> selectedItems;

        public ValidationContext(Screen screen, Collection<T> selectedItems) {
            this.screen = screen;
            this.selectedItems = selectedItems;
        }

        public Screen getScreen() {
            return screen;
        }

        public Collection<T> getSelectedItems() {
            return selectedItems;
        }
    }
}
