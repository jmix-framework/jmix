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

package io.jmix.datatoolsflowui.accesscontext;

import io.jmix.core.accesscontext.SpecificOperationAccessContext;

/**
 * An access context to check permissions to import/export entities using Data Tools views.
 */
public class UiImportExportEntityContext extends SpecificOperationAccessContext {
    
    public static final String NAME = "datatools.importExportEntity";

    public UiImportExportEntityContext() {
        super(NAME);
    }
}
