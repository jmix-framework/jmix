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


import org.eclipse.persistence.platform.database.oracle.annotations.NamedPLSQLStoredProcedureQuery;
import org.eclipse.persistence.platform.database.oracle.annotations.PLSQLParameter;
import org.eclipse.persistence.platform.database.oracle.plsql.OraclePLSQLTypes;

/**
 * JmixOraclePlatform for oracle 22 and lower versions.
 * It only needed if {@link NamedPLSQLStoredProcedureQuery} with {@link PLSQLParameter}
 * of type {@link OraclePLSQLTypes#PLSQLBoolean} is used.
 * <p>
 * Helps to avoid conversion exception (PLS-00382).
 * <p>
 * Use next application property to enable:
 * <pre>
 *     jmix.eclipselink.enable-oracle-legacy-boolean-conversion=true
 * </pre>
 */
public class JmixOracle21Platform extends JmixOraclePlatform {

    @Override
    public boolean isOracle23() {
        return false;
    }
}
