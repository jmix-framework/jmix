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

package io.jmix.ui.widget.client.downloader;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;
import io.jmix.ui.widget.JmixFileDownloader;

@Connect(JmixFileDownloader.class)
public class JmixFileDownloaderConnector extends AbstractExtensionConnector {

    public JmixFileDownloaderConnector() {
        registerRpc(JmixFileDownloaderClientRPC.class, new JmixFileDownloaderClientRPC() {
            @Override
            public void downloadFile(String resourceId) {
                downloadFileById(resourceId);
            }

            @Override
            public void viewDocument(String resourceId) {
                viewDocumentById(resourceId);
            }
        });
    }

    @Override
    protected void extend(ServerConnector target) {
    }

    public void downloadFileById(String resourceId) {
        final String url = getResourceUrl(resourceId);
        if (url != null && !url.isEmpty()) {
            final IFrameElement iframe = Document.get().createIFrameElement();

            Style style = iframe.getStyle();
            style.setVisibility(Style.Visibility.HIDDEN);
            style.setHeight(0, Style.Unit.PX);
            style.setWidth(0, Style.Unit.PX);

            iframe.setFrameBorder(0);
            iframe.setTabIndex(-1);
            iframe.setSrc(url);
            RootPanel.getBodyElement().appendChild(iframe);

            Timer removeTimer = new Timer() {
                @Override
                public void run() {
                    iframe.removeFromParent();
                }
            };
            removeTimer.schedule(60 * 1000);
        }
    }

    public void viewDocumentById(String resourceId) {
        final String url = getResourceUrl(resourceId);
        if (url != null && !url.isEmpty()) {
            Window.open(url, "_blank", "");
        }
    }
}
