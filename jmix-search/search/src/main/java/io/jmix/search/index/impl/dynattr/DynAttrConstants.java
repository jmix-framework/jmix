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

package io.jmix.search.index.impl.dynattr;

public interface DynAttrConstants {
    /**
     * The duplicate of the io.jmix.dynattr.DynAttrQueryHints#LOAD_DYN_ATTR
     * This duplication is need because of independence of the Dynamic Attributes Add-on
     */
    String LOAD_DYN_ATTR = "jmix.dynattr";
}
