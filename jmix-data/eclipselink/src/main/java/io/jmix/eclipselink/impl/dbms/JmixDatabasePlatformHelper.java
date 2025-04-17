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

package io.jmix.eclipselink.impl.dbms;

import io.jmix.eclipselink.impl.support.JmixIsNullExpressionOperator;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.platform.database.DatabasePlatform;

public class JmixDatabasePlatformHelper {

    public static void replaceIsNullOperator(DatabasePlatform platform){
        ExpressionOperator operator = ExpressionOperator.getOperator(ExpressionOperator.IsNull);
        if (operator instanceof JmixIsNullExpressionOperator) {
            //noinspection unchecked
            platform.getPlatformOperators().put(Integer.valueOf(operator.getSelector()), operator);
        } else {
            platform.getPlatformOperators().remove(ExpressionOperator.IsNull);
        }
    }
}
