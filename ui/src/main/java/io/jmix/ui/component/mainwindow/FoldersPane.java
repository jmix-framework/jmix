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

package io.jmix.ui.component.mainwindow;

import io.jmix.ui.component.Component;

/**
 * A component displaying a panel with application and search folders.
 */
public interface FoldersPane extends Component.BelongToFrame {

    String NAME = "foldersPane";

    /**
     * Initially show the panel and load folders. Invoked by the component loader.
     */
    void loadFolders();

    /**
     * Refresh folders if the panel is visible.
     */
    void refreshFolders();

    // todo search folders
//    /**
//     * Sets the icon provider for the folders.
//     *
//     * @param iconProvider provider which provides icons for the folders
//     */
//    void setFolderIconProvider(Function<AbstractSearchFolder, String> iconProvider);
//
//    /**
//     * @return icon provider of the folders
//     */
//    Function<AbstractSearchFolder, String> getFolderIconProvider();
}
