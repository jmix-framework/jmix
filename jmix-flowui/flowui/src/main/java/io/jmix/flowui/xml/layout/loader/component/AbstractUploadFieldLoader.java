/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Splitter;
import com.vaadin.flow.component.upload.UploadI18N;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.upload.AbstractSingleUploadField;
import io.jmix.flowui.kit.component.upload.JmixUploadI18N;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public abstract class AbstractUploadFieldLoader<C extends AbstractSingleUploadField & SupportsValueSource>
        extends AbstractComponentLoader<C> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);

        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);

        getLoaderSupport().loadResourceString(element, "clearButtonAriaLabel", context.getMessageGroup(),
                resultComponent::setClearButtonAriaLabel);

        getLoaderSupport().loadInteger(element, "maxFileSize", resultComponent::setMaxFileSize);
        loadAcceptedFileTypes(element)
                .ifPresent(types-> resultComponent.setAcceptedFileTypes(types.toArray(new String[0])));

        getLoaderSupport().loadBoolean(element, "dropAllowed", resultComponent::setDropAllowed);

        getLoaderSupport().loadString(element, "uploadIcon")
                .map(FlowuiComponentUtils::parseIcon)
                .ifPresent(resultComponent::setUploadIcon);

        getLoaderSupport().loadResourceString(element, "uploadText", context.getMessageGroup(),
                resultComponent::setUploadText);
        getLoaderSupport().loadResourceString(element, "fileNotSelectedText", context.getMessageGroup(),
                resultComponent::setFileNotSelectedText);

        getLoaderSupport().loadBoolean(element, "fileNameVisible", resultComponent::setFileNameVisible);
        getLoaderSupport().loadBoolean(element, "clearButtonVisible", resultComponent::setClearButtonVisible);

        loadI18N(resultComponent, element);
    }

    protected void loadI18N(C resultComponent, Element element) {
        JmixUploadI18N jmixUploadI18n = new JmixUploadI18N();

        getLoaderSupport().loadResourceString(element, "fileTooBigText", context.getMessageGroup(),
                value -> jmixUploadI18n.setError(getOrCreateError(jmixUploadI18n).setFileIsTooBig(value)));
        getLoaderSupport().loadResourceString(element, "incorrectFileTypeText", context.getMessageGroup(),
                value -> jmixUploadI18n.setError(getOrCreateError(jmixUploadI18n).setIncorrectFileType(value)));

        getLoaderSupport().loadResourceString(element, "processingStatusText", context.getMessageGroup(),
                value -> {
                    UploadI18N.Uploading uploading = getOrCreateUploading(jmixUploadI18n);
                    jmixUploadI18n.setUploading(uploading.setStatus(getOrCreateStatus(uploading).setProcessing(value)));
                });

        getLoaderSupport().loadResourceString(element, "connectingStatusText", context.getMessageGroup(),
                value -> {
                    UploadI18N.Uploading uploading = getOrCreateUploading(jmixUploadI18n);
                    jmixUploadI18n.setUploading(uploading.setStatus(getOrCreateStatus(uploading).setConnecting(value)));
                });

        getLoaderSupport().loadResourceString(element, "remainingTimeText", context.getMessageGroup(),
                value -> {
                    UploadI18N.Uploading uploading = getOrCreateUploading(jmixUploadI18n);
                    jmixUploadI18n.setUploading(uploading.setRemainingTime(
                            getCreateRemainingTime(uploading).setPrefix(value)));
                });

        getLoaderSupport().loadResourceString(element, "remainingTimeUnknownText", context.getMessageGroup(),
                value -> {
                    UploadI18N.Uploading uploading = getOrCreateUploading(jmixUploadI18n);
                    jmixUploadI18n.setUploading(uploading.setRemainingTime(
                            getCreateRemainingTime(uploading).setUnknown(value)));
                });

        getLoaderSupport().loadResourceString(element, "uploadDialogTitle", context.getMessageGroup(),
                value -> jmixUploadI18n.setUploadDialog(getOrCreateUploadDialog(jmixUploadI18n).setTitle(value)));
        getLoaderSupport().loadResourceString(element, "uploadDialogCancelText", context.getMessageGroup(),
                value -> jmixUploadI18n.setUploadDialog(getOrCreateUploadDialog(jmixUploadI18n).setCancel(value)));

        resultComponent.setI18n(jmixUploadI18n);
    }

    protected JmixUploadI18N.UploadDialog getOrCreateUploadDialog(JmixUploadI18N jmixUploadI18n) {
        return jmixUploadI18n.getUploadDialog() == null
                ? new JmixUploadI18N.UploadDialog()
                : jmixUploadI18n.getUploadDialog();
    }

    protected UploadI18N.Error getOrCreateError(JmixUploadI18N jmixUploadI18n) {
        return jmixUploadI18n.getError() == null
                ? new UploadI18N.Error()
                : jmixUploadI18n.getError();
    }

    protected UploadI18N.Uploading getOrCreateUploading(JmixUploadI18N jmixUploadI18n) {
        return jmixUploadI18n.getUploading() == null
                ? new UploadI18N.Uploading()
                : jmixUploadI18n.getUploading();
    }

    protected UploadI18N.Uploading.Status getOrCreateStatus(UploadI18N.Uploading uploading) {
        return uploading.getStatus() == null
                ? new UploadI18N.Uploading.Status()
                : uploading.getStatus();
    }

    protected UploadI18N.Uploading.RemainingTime getCreateRemainingTime(UploadI18N.Uploading uploading) {
        return uploading.getRemainingTime() == null
                ? new UploadI18N.Uploading.RemainingTime()
                : uploading.getRemainingTime();
    }

    protected Optional<List<String>> loadAcceptedFileTypes(Element element) {
        return loadString(element, "acceptedFileTypes")
                .map(s -> Splitter.onPattern("[\\s,]+")
                        .omitEmptyStrings()
                        .trimResults()
                        .splitToList(s));
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
