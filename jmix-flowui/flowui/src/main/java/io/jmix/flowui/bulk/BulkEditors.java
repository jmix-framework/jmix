/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.bulk;

import com.vaadin.flow.component.Focusable;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.app.bulk.BulkEditView;
import io.jmix.flowui.app.bulk.BulkEditContext;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.WindowBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;


/**
 * A bean that creates an instance of {@link BulkEditorBuilder}.
 */
@Component("ui_BulkEditors")
public class BulkEditors {

    private static final Logger log = LoggerFactory.getLogger(BulkEditors.class);

    protected final DialogWindows dialogWindows;

    public BulkEditors(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    public <E> BulkEditorBuilder<E> builder(MetaClass metaClass, Collection<E> entities, View<?> origin) {
        checkNotNullArgument(metaClass);
        checkNotNullArgument(entities);
        checkNotNullArgument(origin);

        return new BulkEditorBuilder<>(metaClass, entities, origin, this::buildEditor);
    }

    protected <E> DialogWindow<BulkEditView<E>> buildEditor(BulkEditorBuilder<E> builder) {
        if (CollectionUtils.isEmpty(builder.getEntities())) {
            throw new IllegalStateException(String.format("BulkEditor of %s cannot be open with no entities set",
                    builder.getMetaClass()));
        }

        //noinspection rawtypes
        WindowBuilder windowBuilder = dialogWindows.view(builder.getOrigin(), BulkEditView.class);
        //noinspection unchecked
        DialogWindow<BulkEditView<E>> dialogWindow = ((WindowBuilder<BulkEditView<E>>) windowBuilder)
                .withAfterCloseListener(createAfterCloseHandler(builder))
                .build();


        BulkEditContext<E> context = createBulkEditorContext(builder);
        BulkEditView<E> bulkEditorWindow = dialogWindow.getView();
        bulkEditorWindow.setBulkEditorContext(context);

        return dialogWindow;
    }

    protected <E, V extends BulkEditView<E>> Consumer<DialogWindow.AfterCloseEvent<V>> createAfterCloseHandler(
            BulkEditorBuilder<E> builder) {
        return afterCloseEvent -> {
            ListDataComponent<E> listDataComponent = builder.getListDataComponent();
            if (afterCloseEvent.closedWith(StandardOutcome.SAVE) && listDataComponent != null) {
                refreshItems(listDataComponent.getItems());
            }
            if (listDataComponent instanceof Focusable<?> focusable) {
                focusable.focus();
            }
        };
    }

    protected void refreshItems(@Nullable DataUnit dataUnit) {
        if (dataUnit instanceof ContainerDataUnit) {
            CollectionContainer<?> container = ((ContainerDataUnit<?>) dataUnit).getContainer();

            DataLoader loader = null;
            if (container instanceof HasLoader) {
                loader = ((HasLoader) container).getLoader();
            }

            if (loader != null) {
                loader.load();
            } else {
                log.warn("Target container has no loader, refresh is impossible");
            }
        }
    }

    protected <E> BulkEditContext<E> createBulkEditorContext(BulkEditorBuilder<E> builder) {
        BulkEditContext<E> context = new BulkEditContext<>(builder.getMetaClass(), builder.getEntities());
        context.setExclude(builder.getExclude());
        context.setIncludeProperties(builder.getIncludeProperties());
        context.setFieldValidators(builder.getFieldValidators());
        context.setModelValidators(builder.getModelValidators());
        context.setUseConfirmDialog(builder.isUseConfirmDialog());
        context.setFieldSorter(builder.getFieldSorter());
        context.setColumnsMode(builder.getColumnsMode());

        return context;
    }
}