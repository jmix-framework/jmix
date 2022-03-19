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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.EntityLinkField;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import io.jmix.core.Entity;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.impl.EntityLinkFieldImpl;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Deprecated
public class WebEntityLinkField<V> extends EntityLinkFieldImpl<V> implements EntityLinkField<V> {

    protected OpenType screenOpenType = OpenType.THIS_TAB;
    protected ScreenCloseListener screenCloseListener;

    @Override
    public OpenType getScreenOpenType() {
        return screenOpenType;
    }

    @Override
    public void setScreenOpenType(OpenType screenOpenType) {
        Preconditions.checkNotNullArgument(screenOpenType);

        this.screenOpenType = screenOpenType;
        this.screenOpenMode = screenOpenType.getOpenMode();
    }

    @Nullable
    @Override
    public ScreenCloseListener getScreenCloseListener() {
        return screenCloseListener;
    }

    @Override
    public void setScreenCloseListener(@Nullable ScreenCloseListener closeListener) {
        this.screenCloseListener = closeListener;

        if (closeListenerSubscription != null) {
            closeListenerSubscription.remove();
        }

        if (screenCloseListener != null) {
            closeListenerSubscription = addEditorCloseListener(event -> {
                if (event.getEditorScreen() instanceof AbstractEditor) {
                    screenCloseListener.windowClosed((Window) event.getEditorScreen(), event.getActionId());
                } else {
                    screenCloseListener.windowClosed(null, event.getActionId());
                }
            });
        }
    }

    @Override
    public void setCustomClickHandler(@Nullable EntityLinkField.EntityLinkClickHandler clickHandler) {
        this.clickHandler = clickHandler::onClick;
    }

    @Override
    public void addValidator(Consumer<? super V> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<V> validator) {
        removeValidator(validator::accept);
    }

    @Nullable
    @Override
    public ListComponent getOwner() {
        return (ListComponent) super.getOwner();
    }

    @Override
    protected void afterCommitOpenedEntity(Object item) {
        if (getCollectionDatasourceFromOwner() == null) {
            super.afterCommitOpenedEntity(item);
            return;
        }

        MetaProperty metaProperty = getMetaPropertyForEditedValue();
        if (metaProperty != null && metaProperty.getRange().isClass()) {
            if (getValueSource() != null) {
                boolean ownerDsModified = false;
                boolean nonModifiedInTable = false;

                DatasourceImplementation ownerDs = null;

                if (getCollectionDatasourceFromOwner() != null) {
                    ownerDs = ((DatasourceImplementation) getCollectionDatasourceFromOwner());
                    nonModifiedInTable = !ownerDs.getItemsToUpdate().contains(
                            ((EntityValueSource) getValueSource()).getItem());
                    ownerDsModified = ownerDs.isModified();
                }

                //noinspection unchecked
                setValueSilently((V) item);

                // restore modified for owner datasource
                // remove from items to update if it was not modified before setValue
                if (ownerDs != null) {
                    if (nonModifiedInTable) {
                        ownerDs.getItemsToUpdate().remove(getDatasource().getItem());
                    }
                    ownerDs.setModified(ownerDsModified);
                }
            } else {
                //noinspection unchecked
                setValue((V) item);
            }
            // if we edit property with non Entity type and set ListComponent owner
        } else if (owner != null) {
            if (getCollectionDatasourceFromOwner() != null) {
                //noinspection unchecked
                getCollectionDatasourceFromOwner().updateItem((Entity) item);
            }

            if (owner instanceof Focusable) {
                // focus owner
                ((Focusable) owner).focus();
            }
            // if we edit property with non Entity type
        } else {
            //noinspection unchecked
            setValueSilently((V) item);
        }
    }

    /**
     * Sets value to the component without changing a modify state of Datasource.
     *
     * @param item value
     */
    @Override
    protected void setValueSilently(@Nullable V item) {
        if (getDatasource() != null) {
            boolean modified = getDatasource().isModified();
            setValue(item);
            ((DatasourceImplementation) getDatasource()).setModified(modified);
        } else {
            super.setValueSilently(item);
        }
    }

    @Deprecated
    protected CollectionDatasource getCollectionDatasourceFromOwner() {
        if (getOwner() != null && getOwner() != null) {
            return getOwner().getDatasource();
        }
        return null;
    }
}
