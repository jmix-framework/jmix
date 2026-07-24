/*
 * Copyright 2026 Haulmont.
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

package io.jmix.data.impl.dbms;

import io.jmix.core.common.util.Preconditions;
import io.jmix.data.persistence.SequenceSupport;
import org.springframework.stereotype.Component;

@Component("firebirdSequenceSupport")
public class FirebirdSequenceSupport implements SequenceSupport {

    @Override
    public String sequenceExistsSql(String sequenceName) {
        return "select rdb$generator_name from rdb$generators where rdb$generator_name = '"
                + sequenceName.toUpperCase() + "'";
    }

    @Override
    public String createSequenceSql(String sequenceName, long startValue, long increment) {
        Preconditions.checkNotNullArgument(sequenceName, "sequenceName is null");
        return "create sequence " + sequenceName
                + " start with " + startValue + " increment by " + increment;
    }

    @Override
    public String modifySequenceSql(String sequenceName, long startWith) {
        Preconditions.checkNotNullArgument(sequenceName, "sequenceName is null");
        // 'alter sequence ... restart with N' means "next value is exactly N" since Firebird 4,
        // while this method must set the current value so that the next value is N + increment.
        return "set generator " + sequenceName + " to " + startWith;
    }

    @Override
    public String deleteSequenceSql(String sequenceName) {
        Preconditions.checkNotNullArgument(sequenceName, "sequenceName is null");
        return "drop sequence " + sequenceName;
    }

    @Override
    public String getNextValueSql(String sequenceName) {
        Preconditions.checkNotNullArgument(sequenceName, "sequenceName is null");
        return "select next value for " + sequenceName + " from rdb$database";
    }

    @Override
    public String getCurrentValueSql(String sequenceName) {
        Preconditions.checkNotNullArgument(sequenceName, "sequenceName is null");
        return "select gen_id(" + sequenceName + ", 0) from rdb$database";
    }
}