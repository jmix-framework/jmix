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
import io.jmix.flowui.view.*;
import org.springframework.context.ApplicationContext;

/**
 * The processor that builds a {@link DialogWindow} showing an entity in a read view.
 * <p>
 * If view resolution falls back to a detail view, the entity is set to it and the view is switched
 * to the read-only mode.
 */
public class ReadWindowBuilderProcessor extends AbstractWindowBuilderProcessor {

    public ReadWindowBuilderProcessor(ApplicationContext applicationContext,
                                      Views views,
                                      ViewRegistry viewRegistry,
                                      UiAccessChecker uiAccessChecker) {
        super(applicationContext, views, viewRegistry, uiAccessChecker);
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
        V view = createView(builder);

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        setupEntity(builder, view);

        return dialog;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <E, V extends View<?>> void setupEntity(ReadWindowBuilder<E, V> builder, V view) {
        E entity = getEntity(builder);

        if (view instanceof ReadView readView) {
            readView.setEntityToRead(entity);
        } else if (view instanceof DetailView detailView) {
            // View resolution fell back to a detail view, so it must be shown in the read-only mode.
            detailView.setEntityToEdit(entity);

            if (view instanceof ReadOnlyAwareView readOnlyAwareView) {
                readOnlyAwareView.setReadOnly(true);
            }
        } else {
            throw new IllegalStateException(String.format("%s '%s' implements neither %s nor %s: %s",
                    View.class.getSimpleName(), view.getId().orElse(null),
                    ReadView.class.getSimpleName(), DetailView.class.getSimpleName(),
                    view.getClass()));
        }
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
