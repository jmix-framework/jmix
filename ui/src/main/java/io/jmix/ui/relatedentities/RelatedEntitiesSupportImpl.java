/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.relatedentities;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import org.springframework.stereotype.Component;

@Internal
@Component("ui_RelatedEntities")
public class RelatedEntitiesSupportImpl implements RelatedEntitiesSupport {

    //    protected Messages messages;
    protected Metadata metadata;
    protected WindowConfig windowConfig;

    public RelatedEntitiesSupportImpl(Metadata metadata, WindowConfig windowConfig) {
//        this.messages = messages;
        this.metadata = metadata;
        this.windowConfig = windowConfig;
    }

    @Override
    public RelatedEntitiesBuilder builder(FrameOwner frameOwner) {
        return new RelatedEntitiesBuilder(frameOwner, this::buildScreen);
    }

    protected Screen buildScreen(RelatedEntitiesBuilder builder) {
        MetaClass metaClass = getMetaClass(builder);
        MetaProperty metaProperty = getMetaProperty(builder, metaClass);

        return createScreen(builder, metaClass, metaProperty);
        // TODO: gg, wait for Haulmont/jmix-old#90
        /*Screen screen = createScreen(builder, metaClass, metaProperty);

        Collection<? extends JmixEntity> selectedEntities = builder.getSelectedEntities() == null
                ? Collections.emptyList()
                : builder.getSelectedEntities();

        boolean found = ComponentsHelper.walkComponents(screen.getWindow(), screenComponent -> {
            if (!(screenComponent instanceof Filter)) {
                return false;
            } else {
                MetaClass actualMetaClass = ((FilterImplementation) screenComponent).getEntityMetaClass();
                MetaClass relatedMetaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(relatedMetaClass);
                if (Objects.equals(actualMetaClass, effectiveMetaClass)) {
                    MetaDataDescriptor metaDataDescriptor = new MetaDataDescriptor(metaClass, metaProperty);

                    RelatedScreenDescriptor descriptor = new RelatedScreenDescriptor();
                    descriptor.setFilterCaption(builder.getFilterCaption());
                    applyFilter(((Filter) screenComponent), selectedEntities, descriptor, metaDataDescriptor);
                    return true;
                }
                return false;
            }
        });

        if (!found) {
            screen.addAfterShowListener(event -> {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(event.getSource());
                screenContext.getNotifications()
                        .create(NotificationType.WARNING)
                        .withCaption(messages.getMessage("actions.Related.FilterNotFound"))
                        .show();
            });
        }

        return screen;*/
    }

    protected MetaClass getMetaClass(RelatedEntitiesBuilder builder) {
        MetaClass metaClass = builder.getMetaClass();
        if (metaClass != null) {
            return metaClass;
        }

        Class<?> entityClass = builder.getEntityClass();
        if (entityClass != null) {
            return metadata.getClass(entityClass);
        }

        throw new IllegalStateException("'metaClass' or 'entityClass' can't be null");
    }

    protected MetaProperty getMetaProperty(RelatedEntitiesBuilder builder, MetaClass metaClass) {
        MetaProperty metaProperty = builder.getMetaProperty();
        if (metaProperty != null) {
            return metaProperty;
        }

        String property = builder.getProperty();
        if (!Strings.isNullOrEmpty(property)) {
            return metaClass.getProperty(property);
        }

        throw new IllegalStateException("'metaProperty' or 'property' can't be null");
    }

    protected Screen createScreen(RelatedEntitiesBuilder builder, MetaClass metaClass, MetaProperty metaProperty) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = UiControllerUtils.getScreenContext(origin).getScreens();

        if (builder instanceof RelatedEntitiesClassBuilder) {
            return screens.create(((RelatedEntitiesClassBuilder<?>) builder).getScreenClass(),
                    builder.getOpenMode(), builder.getOptions());
        } else {
            String screenId = builder.getScreenId();
            if (Strings.isNullOrEmpty(screenId)) {
                // try to get default browse screen id
                screenId = windowConfig.getBrowseScreenId(metaProperty.getRange().asClass());
                if (Strings.isNullOrEmpty(screenId)) {
                    String message = String.format("Can't create related entities screen: passed screen id is null and " +
                            "there is no default browse screen for %s", metaClass.getName());
                    throw new IllegalStateException(message);
                }
            }

            return screens.create(screenId, builder.getOpenMode(), builder.getOptions());
        }
    }
}
