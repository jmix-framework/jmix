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

package com.haulmont.cuba.core.global;

import com.haulmont.chile.core.model.Session;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;

/**
 * Legacy interface to provide metadata-related functionality.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.Metadata}.
 */
@Deprecated
public interface Metadata extends io.jmix.core.Metadata, Session {

    @Override
    Session getSession();

    /**
     * @deprecated Use dependency injection.
     */
    @Deprecated
    ViewRepository getViewRepository();

    /**
     * @deprecated Use dependency injection.
     */
    @Deprecated
    ExtendedEntities getExtendedEntities();

    /**
     * @deprecated Use dependency injection.
     */
    @Deprecated
    MetadataTools getTools();

    /**
     * @deprecated Use dependency injection.
     */
    @Deprecated
    DatatypeRegistry getDatatypes();

}
