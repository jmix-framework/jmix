
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

package io.jmix.eclipselink.impl.dbms;

import org.eclipse.persistence.exceptions.ConversionException;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.platform.database.FirebirdPlatform;
import org.eclipse.persistence.queries.Call;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;

import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;
import java.util.UUID;

public class JmixFirebirdPlatform extends FirebirdPlatform implements UuidMappingInfo {

    // TODO: remove when fixed in EclipseLink https://github.com/eclipse-ee4j/eclipselink/issues/2788
    @Override
    protected Map<Class<?>, FieldDefinition.DatabaseType> buildDatabaseTypes() {
        Map<Class<?>, FieldDefinition.DatabaseType> types = super.buildDatabaseTypes();
        types.put(Clob.class, new FieldDefinition.DatabaseType("BLOB SUB_TYPE TEXT", false));
        types.put(Character[].class, new FieldDefinition.DatabaseType("BLOB SUB_TYPE TEXT", false));
        types.put(char[].class, new FieldDefinition.DatabaseType("BLOB SUB_TYPE TEXT", false));
        types.put(OffsetDateTime.class, new FieldDefinition.DatabaseType("TIMESTAMP WITH TIME ZONE", false));
        types.put(OffsetTime.class, new FieldDefinition.DatabaseType("TIME WITH TIME ZONE", false));
        return types;
    }

    @Override
    public int appendParameterInternal(Call call, Writer writer, Object parameter) {
        return super.appendParameterInternal(call, writer, convertToDataValueIfUUID(parameter));
    }

    @Override
    public void setParameterValueInDatabaseCall(Object parameter,
                                                PreparedStatement statement,
                                                int index,
                                                AbstractSession session)
            throws SQLException {
        super.setParameterValueInDatabaseCall(convertToDataValueIfUUID(parameter), statement, index, session);
    }

    @Override
    public void setParameterValueInDatabaseCall(Object parameter,
                                                CallableStatement statement,
                                                String name,
                                                AbstractSession session)
            throws SQLException {
        super.setParameterValueInDatabaseCall(convertToDataValueIfUUID(parameter), statement, name, session);
    }

    @Override
    public Object convertObject(Object sourceObject, Class javaClass) throws ConversionException {
        if (sourceObject instanceof UUID && javaClass == String.class) {
            return String36UuidConverter.getInstance().convertObjectValueToDataValue(sourceObject, null);
        }
        return super.convertObject(sourceObject, javaClass);
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
        return "varchar(36)";
    }

    @Override
    public Converter getUuidConverter() {
        return String36UuidConverter.getInstance();
    }
}