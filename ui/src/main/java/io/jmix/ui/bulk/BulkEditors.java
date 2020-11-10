/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.bulk;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.Screens;
import io.jmix.ui.app.bulk.BulkEditorController.BulkEditorContext;
import io.jmix.ui.app.bulk.BulkEditorWindow;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.DataUnit;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import io.jmix.ui.screen.CloseAction;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardCloseAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static io.jmix.ui.screen.UiControllerUtils.getScreenContext;


/**
 * A bean that creates an instance of {@link BulkEditorBuilder}.
 */
@Component("ui_BulkEditors")
public class BulkEditors {

    private static final Logger log = LoggerFactory.getLogger(BulkEditors.class);

    public <E> BulkEditorBuilder<E> builder(MetaClass metaClass,
                                            Collection<E> entities, FrameOwner origin) {
        checkNotNullArgument(metaClass);
        checkNotNullArgument(entities);
        checkNotNullArgument(origin);

        return new BulkEditorBuilder<>(metaClass, entities, origin, this::buildEditor);
    }

    protected <E> BulkEditorWindow<E> buildEditor(BulkEditorBuilder<E> builder) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = getScreenContext(origin).getScreens();

        if (CollectionUtils.isEmpty(builder.getEntities())) {
            throw new IllegalStateException(String.format("BulkEditor of %s cannot be open with no entities were set",
                    builder.getMetaClass()));
        }

        //noinspection unchecked
        BulkEditorWindow<E> bulkEditorWindow = screens.create(BulkEditorWindow.class, builder.openMode);

        BulkEditorContext<E> context = createBulkEditorContext(builder);
        bulkEditorWindow.setBulkEditorContext(context);

        bulkEditorWindow.addAfterCloseListener(createAfterCloseHandler(builder));

        return bulkEditorWindow;
    }

    protected <E> BulkEditorContext<E> createBulkEditorContext(BulkEditorBuilder<E> builder) {
        BulkEditorContext<E> context = new BulkEditorContext<>(builder.metaClass, builder.entities);
        context.setExclude(builder.getExclude());
        context.setIncludeProperties(builder.getIncludeProperties());
        context.setFieldValidators(builder.getFieldValidators());
        context.setModelValidators(builder.getModelValidators());
        context.setUseConfirmDialog(!Boolean.FALSE.equals(builder.isUseConfirmDialog()));
        context.setFieldSorter(builder.getFieldSorter());
        context.setColumnsMode(builder.getColumnsMode());

        return context;
    }

    protected <E> Consumer<Screen.AfterCloseEvent> createAfterCloseHandler(BulkEditorBuilder<E> builder) {
        return afterCloseEvent -> {
            ListComponent<E> listComponent = builder.getListComponent();
            CloseAction closeAction = afterCloseEvent.getCloseAction();
            if (isCommitCloseAction(closeAction)
                    && listComponent != null) {
                refreshItems(listComponent.getItems());
            }
            if (listComponent instanceof io.jmix.ui.component.Component.Focusable) {
                ((io.jmix.ui.component.Component.Focusable) listComponent).focus();
            }
        };
    }

    protected void refreshItems(@Nullable DataUnit dataSource) {
        if (dataSource instanceof ContainerDataUnit) {
            CollectionContainer<?> container = ((ContainerDataUnit<?>) dataSource).getContainer();

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

    protected boolean isCommitCloseAction(CloseAction closeAction) {
        return (closeAction instanceof StandardCloseAction)
                && ((StandardCloseAction) closeAction).getActionId().equals(Window.COMMIT_ACTION_ID);
    }
}