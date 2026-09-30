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

package io.jmix.eclipselink.impl.dbms;

import org.eclipse.persistence.core.sessions.CoreSession;
import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.platform.database.oracle.Oracle23Platform;
import org.eclipse.persistence.queries.Call;

import java.io.Writer;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

public class JmixOraclePlatform extends Oracle23Platform implements UuidMappingInfo {

    @Override
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException {
        if (sourceObject instanceof UUID && javaClass == String.class) {
            return String32UuidConverter.getInstance().uuidToString(sourceObject);
        }
        return super.convertObject(sourceObject, javaClass);
    }

    /**
     * Fixes {@link NullPointerException} on conversion of an empty string to CLOB when the session has no
     * connection at the moment, e.g. when attribute values are compared on merge outside a database transaction.
     * <p>
     * Outside a database transaction, the empty string is returned as is, because values are written to the
     * database only inside a transaction, and other usages, like the comparison on merge, do not need a CLOB.
     * It avoids acquiring a connection and creating a temporary LOB that is never freed.
     * <p>
     * Inside a transaction, repeats the fix made in EclipseLink for the same method of
     * {@code org.eclipse.persistence.platform.database.Oracle23Platform} in the core module
     * (eclipse-ee4j/eclipselink#2601). The copy of the method in the oracle module is not fixed as of 5.0.0.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T convertObject(Object sourceObject, Class<T> javaClass, CoreSession<?, ?, ?, ?, ?> session)
            throws ConversionException, DatabaseException {
        if (ClassConstants.CLOB.equals(javaClass) && "".equals(sourceObject)) {
            AbstractSession abstractSession = (AbstractSession) session;
            if (!abstractSession.isInTransaction()) {
                return (T) convertObject(sourceObject, javaClass);
            }
            Accessor accessor = abstractSession.getAccessor();
            try {
                // Acquires a connection if the accessor is not connected, as it happens with external connection
                // pooling. It is called inside try, so that the call count is restored if the acquisition fails.
                accessor.incrementCallCount(abstractSession);
                Clob clob = accessor.getConnection().createClob();
                clob.setString(1, (String) sourceObject);
                return (T) clob;
            } catch (SQLException e) {
                throw ConversionException.couldNotBeConvertedToClass(sourceObject, ClassConstants.CLOB, e);
            } finally {
                accessor.decrementCallCount();
            }
        }
        return super.convertObject(sourceObject, javaClass, session);
    }

    @Override
    public int appendParameterInternal(Call call, Writer writer, Object parameter) {
        return super.appendParameterInternal(call, writer, convertToDataValueIfUUID(parameter));
    }

    @Override
    public void setParameterValueInDatabaseCall(Object parameter, PreparedStatement statement, int index, AbstractSession session) throws SQLException {
        super.setParameterValueInDatabaseCall(convertToDataValueIfUUID(parameter), statement, index, session);
    }

    @Override
    public int getUuidSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public Class<?> getUuidType() {
        return String.class;
    }

    @Override
    public String getUuidColumnDefinition() {
        return "varchar2(32)";
    }

    @Override
    public Converter getUuidConverter() {
        return String32UuidConverter.getInstance();
    }
}