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

package io.jmix.core;

import io.jmix.core.metamodel.model.MetaClass;

/**
 * This exception is raised on attempt to load a deleted object.
 */
public class EntityAccessException extends RuntimeException {
    public static final String ERR_MESSAGE_1 = "Unable to load entity ";
    public static final String ERR_MESSAGE_2 = "because it has been deleted or access denied";

    public EntityAccessException() {
        super(ERR_MESSAGE_1 + ERR_MESSAGE_2);
    }

    public EntityAccessException(MetaClass metaClass, Object entityId) {
        super(ERR_MESSAGE_1 + metaClass.getName() + "-" + entityId + " " + ERR_MESSAGE_2);
    }
}
