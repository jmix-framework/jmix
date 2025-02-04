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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiViewProperties;
import io.jmix.flowui.Views;
import io.jmix.flowui.sys.UiAccessChecker;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.builder.DetailWindowBuilderProcessor;
import io.jmix.flowui.view.builder.DialogWindowBuilder;
import io.jmix.flowui.view.builder.EditedEntityTransformer;
import io.jmix.tabbedmode.builder.DetailViewBuilder;
import io.jmix.tabbedmode.builder.DetailViewBuilderAdapter;
import io.jmix.tabbedmode.builder.DetailViewBuilderProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component("tabmod_TabbedDetailWindowBuilderProcessor")
public class TabbedModeDetailWindowBuilderProcessor extends DetailWindowBuilderProcessor {

    protected final DetailViewBuilderProcessor detailViewBuilderProcessor;

    public TabbedModeDetailWindowBuilderProcessor(ApplicationContext applicationContext,
                                                  Views views,
                                                  ViewRegistry viewRegistry,
                                                  Metadata metadata,
                                                  ExtendedEntities extendedEntities,
                                                  UiViewProperties viewProperties,
                                                  UiAccessChecker uiAccessChecker,
                                                  List<EditedEntityTransformer> editedEntityTransformers,
                                                  DetailViewBuilderProcessor detailViewBuilderProcessor) {
        super(applicationContext, views, viewRegistry, metadata,
                extendedEntities, viewProperties, uiAccessChecker, editedEntityTransformers);

        this.detailViewBuilderProcessor = detailViewBuilderProcessor;
    }

    @Override
    public <E, V extends View<?>> DialogWindow<V> build(DetailWindowBuilder<E, V> builder) {
        DetailViewBuilder<E, V> viewBuilder = new DetailViewBuilderAdapter<>(builder,
                getViewClass(builder),
                detailViewBuilderProcessor::build, __ -> {});
        V view = viewBuilder.build();

        DialogWindow<V> dialog = createDialog(view);
        initDialog(builder, dialog);

        return dialog;
    }

    @Override
    protected <V extends View<?>> void initDialog(DialogWindowBuilder<V> builder, DialogWindow<V> dialog) {
        builder.getAfterOpenListener().ifPresent(dialog::addAfterOpenListener);
        builder.getAfterCloseListener().ifPresent(dialog::addAfterCloseListener);
    }
}
