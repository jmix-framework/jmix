/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.builder;

import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.*;
import org.springframework.context.ApplicationContext;

/**
 * The processor that builds a {@link DialogWindow} showing an entity in a read view.
 * <p>
 * If view resolution falls back to a detail view, the entity is set to it and the view is switched
 * to the read-only mode.
 */
public class ReadWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    protected DetailWindowBuilderProcessor detailBuilderProcessor;

    public ReadWindowBuilderProcessor(ApplicationContext applicationContext,
                                      Views views,
                                      ViewRegistry viewRegistry,
                                      UiAccessChecker uiAccessChecker,
                                      DetailWindowBuilderProcessor detailBuilderProcessor) {
        super(applicationContext, views, viewRegistry, uiAccessChecker);

        this.detailBuilderProcessor = detailBuilderProcessor;
    }

    /**
     * Builds a dialog window for the given builder.
     *
     * @param builder builder that provides the shown entity and the dialog configuration
     * @param <E>     shown entity type
     * @param <V>     view type
     * @return built dialog window
     */
    public <E, V extends View<?>> DialogWindow<V> build(ReadWindowBuilder<E, V> builder) {
        Class<V> viewClass = getViewClass(builder);

        if (!ReadView.class.isAssignableFrom(viewClass)) {
            return buildFallbackDetailWindow(builder, viewClass);
        }

        V view = createView(builder);

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        setupEntity(builder, view);

        return dialog;
    }

    /**
     * Builds a dialog window for a detail view that view resolution returned instead of a read view.
     * <p>
     * The window is built by {@link DetailWindowBuilderProcessor}, so the detail view behaves exactly as it
     * does when opened for editing - in particular, the list data component and the field are updated if
     * the user enables editing and saves the entity - and then the view is switched to the read-only mode.
     *
     * @param builder   builder that provides the shown entity and the dialog configuration
     * @param viewClass resolved detail view class
     * @param <E>       shown entity type
     * @param <V>       view type
     * @return built dialog window
     */
    protected <E, V extends View<?>> DialogWindow<V> buildFallbackDetailWindow(ReadWindowBuilder<E, V> builder,
                                                                              Class<V> viewClass) {
        DetailWindowBuilder<E, V> detailBuilder = new DetailWindowBuilder<>(builder.getOrigin(),
                builder.getEntityClass(), detailBuilderProcessor::build);

        detailBuilder.withViewId(ViewDescriptorUtils.getInferredViewId(viewClass))
                .editEntity(getEntity(builder));

        builder.getListDataComponent().ifPresent(detailBuilder::withListDataComponent);
        builder.getField().ifPresent(detailBuilder::withField);
        builder.getAfterOpenListener().ifPresent(detailBuilder::withAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(detailBuilder::withAfterCloseListener);
        builder.getDraggedListener().ifPresent(detailBuilder::withDraggedListener);
        builder.getResizeListener().ifPresent(detailBuilder::withResizeListener);
        builder.getViewConfigurer().ifPresent(detailBuilder::withViewConfigurer);

        DialogWindow<V> dialog = detailBuilder.build();

        View<?> view = dialog.getView();
        if (view instanceof ReadOnlyAwareView readOnlyAwareView) {
            readOnlyAwareView.setReadOnly(true);
        } else {
            throw new IllegalStateException(String.format("%s '%s' does not implement %s: %s",
                    View.class.getSimpleName(), view.getId().orElse(null),
                    ReadOnlyAwareView.class.getSimpleName(), view.getClass()));
        }

        return dialog;
    }

    @SuppressWarnings("unchecked")
    protected <E, V extends View<?>> void setupEntity(ReadWindowBuilder<E, V> builder, V view) {
        ((ReadView<E>) view).setEntityToRead(getEntity(builder));
    }

    protected <E, V extends View<?>> E getEntity(ReadWindowBuilder<E, V> builder) {
        return builder.getEntity().orElseThrow(() -> new IllegalStateException(
                String.format("Read view of %s cannot be opened, entity is not set",
                        builder.getEntityClass())));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected <V extends View<?>> Class<V> inferViewClass(DialogWindowBuilder<V> builder) {
        ReadWindowBuilder<?, V> readBuilder = (ReadWindowBuilder<?, V>) builder;

        Class<?> entityClass = readBuilder.getEntity()
                .map(Object::getClass)
                .orElse((Class) readBuilder.getEntityClass());

        return (Class<V>) viewRegistry.getReadViewInfo(entityClass).getControllerClass();
    }
}
