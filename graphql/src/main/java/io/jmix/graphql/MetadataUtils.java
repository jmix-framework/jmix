/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql;

import io.jmix.core.metamodel.model.MetaProperty;

public class MetadataUtils {

    public static final String DATATYPE_ID_DATE = "date";
    public static final String DATATYPE_ID_TIME = "time";
    public static final String DATATYPE_ID_DATETIME = "dateTime";

    public static boolean isDateTime(MetaProperty metaProperty) {
        return DATATYPE_ID_DATETIME.equals(getDatatypeId(metaProperty));
    }

    public static boolean isTime(MetaProperty metaProperty) {
        return DATATYPE_ID_TIME.equals(getDatatypeId(metaProperty));
    }

    public static boolean isDate(MetaProperty metaProperty) {
        return DATATYPE_ID_DATE.equals(getDatatypeId(metaProperty));
    }

    public static String getDatatypeId(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getId();
    }


}
