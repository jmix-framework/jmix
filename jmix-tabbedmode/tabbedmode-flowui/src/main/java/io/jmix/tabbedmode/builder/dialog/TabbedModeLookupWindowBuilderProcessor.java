/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder.dialog;

import io.jmix.core.*;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.DialogWindowBuilder;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import io.jmix.flowui.view.builder.LookupWindowBuilderProcessor;
import io.jmix.tabbedmode.builder.LookupViewBuilder;
import io.jmix.tabbedmode.builder.LookupViewBuilderAdapter;
import io.jmix.tabbedmode.builder.LookupViewBuilderProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("tabmod_TabbedLookupWindowBuilderProcessor")
public class TabbedModeLookupWindowBuilderProcessor extends LookupWindowBuilderProcessor {

    protected final LookupViewBuilderProcessor lookupViewBuilderProcessor;

    public TabbedModeLookupWindowBuilderProcessor(ApplicationContext applicationContext,
                                                  Views views,
                                                  ViewRegistry viewRegistry,
                                                  Metadata metadata,
                                                  MetadataTools metadataTools,
                                                  DataManager dataManager,
                                                  FetchPlans fetchPlans,
                                                  EntityStates entityStates,
                                                  ExtendedEntities extendedEntities,
                                                  UiViewProperties viewProperties,
                                                  UiAccessChecker uiAccessChecker,
                                                  LookupViewBuilderProcessor lookupViewBuilderProcessor) {
        super(applicationContext, views, viewRegistry, metadata, metadataTools, dataManager,
                fetchPlans, entityStates, extendedEntities, viewProperties, uiAccessChecker);

        this.lookupViewBuilderProcessor = lookupViewBuilderProcessor;
    }

    @Override
    public <E, V extends View<?>> DialogWindow<V> build(LookupWindowBuilder<E, V> builder) {
        LookupViewBuilder<E, V> viewBuilder = new LookupViewBuilderAdapter<>(builder,
                getViewClass(builder),
                lookupViewBuilderProcessor::build, __ -> {});
        V view = viewBuilder.build();

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <V extends View<?>> void initDialog(DialogWindowBuilder<V> builder, DialogWindow<V> dialog) {
        builder.getAfterOpenListener().ifPresent(dialog::addAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(dialog::addAfterCloseListener);
        builder.getDraggedListener().ifPresent(dialog::addDraggedListener);
        builder.getResizeListener().ifPresent(dialog::addResizeListener);
    }
}
