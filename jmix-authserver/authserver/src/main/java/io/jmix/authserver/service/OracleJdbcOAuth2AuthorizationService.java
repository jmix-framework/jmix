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

package io.jmix.authserver.service;

import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that provides JdbcOAuth2AuthorizationService for Oracle database.
 */
public class OracleJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {

    private static final String COLUMN_NAMES = "id, "
            + "registered_client_id, "
            + "principal_name, "
            + "authorization_grant_type, "
            + "authorized_scopes, "
            + "attributes, "
            + "state, "
            + "authorization_code_value, "
            + "authorization_code_issued_at, "
            + "authorization_code_expires_at,"
            + "authorization_code_metadata,"
            + "access_token_value,"
            + "access_token_issued_at,"
            + "access_token_expires_at,"
            + "access_token_metadata,"
            + "access_token_type,"
            + "access_token_scopes,"
            + "oidc_id_token_value,"
            + "oidc_id_token_issued_at,"
            + "oidc_id_token_expires_at,"
            + "oidc_id_token_metadata,"
            + "refresh_token_value,"
            + "refresh_token_issued_at,"
            + "refresh_token_expires_at,"
            + "refresh_token_metadata,"
            + "user_code_value,"
            + "user_code_issued_at,"
            + "user_code_expires_at,"
            + "user_code_metadata,"
            + "device_code_value,"
            + "device_code_issued_at,"
            + "device_code_expires_at,"
            + "device_code_metadata";

    private static Map<String, ColumnMetadata> columnMetadataMap;

    private final JdbcOperations jdbcOperations;

    private static final String TABLE_NAME = "oauth2_authorization";

    private static final String UNKNOWN_TOKEN_TYPE_FILTER = "state = ? " +
            "OR dbms_lob.compare(authorization_code_value,?) = 0 " +
            "OR dbms_lob.compare(access_token_value,?) = 0 " +
            "OR dbms_lob.compare(oidc_id_token_value,?) = 0 " +
            "OR dbms_lob.compare(refresh_token_value,?) = 0 " +
            "OR dbms_lob.compare(user_code_value,?) = 0 " +
            "OR dbms_lob.compare(device_code_value,?) = 0";

    private static final String STATE_FILTER = "state = ?";
    private static final String AUTHORIZATION_CODE_FILTER = "dbms_lob.compare(authorization_code_value,?) = 0";
    private static final String ACCESS_TOKEN_FILTER = "dbms_lob.compare(access_token_value,?) = 0";
    private static final String ID_TOKEN_FILTER = "dbms_lob.compare(oidc_id_token_value,?) = 0";
    private static final String REFRESH_TOKEN_FILTER = "dbms_lob.compare(refresh_token_value,?) = 0";
    private static final String USER_CODE_FILTER = "dbms_lob.compare(user_code_value,?) = 0";
    private static final String DEVICE_CODE_FILTER = "dbms_lob.compare(device_code_value,?) = 0";
    private static final String LOAD_AUTHORIZATION_SQL = "SELECT " + COLUMN_NAMES
            + " FROM " + TABLE_NAME
            + " WHERE ";


    public OracleJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
        super(jdbcOperations, registeredClientRepository);
        this.jdbcOperations = jdbcOperations;
        initColumnMetadata(jdbcOperations);
    }

    @Nullable
    @Override
    public OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType) {
        Assert.hasText(token, "token cannot be empty");
        List<SqlParameterValue> parameters = new ArrayList<>();
        if (tokenType == null) {
            parameters.add(new SqlParameterValue(Types.VARCHAR, token));
            parameters.add(mapToSqlParameter("authorization_code_value", token));
            parameters.add(mapToSqlParameter("access_token_value", token));
            parameters.add(mapToSqlParameter("oidc_id_token_value", token));
            parameters.add(mapToSqlParameter("refresh_token_value", token));
            parameters.add(mapToSqlParameter("user_code_value", token));
            parameters.add(mapToSqlParameter("device_code_value", token));
            return findBy(UNKNOWN_TOKEN_TYPE_FILTER, parameters);
        } else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
            parameters.add(new SqlParameterValue(Types.VARCHAR, token));
            return findBy(STATE_FILTER, parameters);
        } else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
            parameters.add(mapToSqlParameter("authorization_code_value", token));
            return findBy(AUTHORIZATION_CODE_FILTER, parameters);
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            parameters.add(mapToSqlParameter("access_token_value", token));
            return findBy(ACCESS_TOKEN_FILTER, parameters);
        } else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
            parameters.add(mapToSqlParameter("oidc_id_token_value", token));
            return findBy(ID_TOKEN_FILTER, parameters);
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            parameters.add(mapToSqlParameter("refresh_token_value", token));
            return findBy(REFRESH_TOKEN_FILTER, parameters);
        } else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
            parameters.add(mapToSqlParameter("user_code_value", token));
            return findBy(USER_CODE_FILTER, parameters);
        } else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
            parameters.add(mapToSqlParameter("device_code_value", token));
            return findBy(DEVICE_CODE_FILTER, parameters);
        }
        return null;
    }

    @Nullable
    protected OAuth2Authorization findBy(String filter, List<SqlParameterValue> parameters) {
        LobCreator lobCreator = this.getLobHandler().getLobCreator();
        OAuth2Authorization authorization;
        try {
            PreparedStatementSetter pss = new LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toArray());
            List<OAuth2Authorization> result = this.jdbcOperations.query(LOAD_AUTHORIZATION_SQL + filter, pss, getAuthorizationRowMapper());
            authorization = !result.isEmpty() ? result.get(0) : null;
        } catch (Throwable e) {
            try {
                lobCreator.close();
            } catch (Throwable e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }

        lobCreator.close();

        return authorization;
    }

    protected static void initColumnMetadata(JdbcOperations jdbcOperations) {
        columnMetadataMap = new HashMap<>();
        OracleJdbcOAuth2AuthorizationService.ColumnMetadata columnMetadata;

        columnMetadata = getColumnMetadata(jdbcOperations, "attributes", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "access_token_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "access_token_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "user_code_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "user_code_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "device_code_value", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "device_code_metadata", Types.BLOB);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
    }

    private static OracleJdbcOAuth2AuthorizationService.ColumnMetadata getColumnMetadata(JdbcOperations jdbcOperations, String columnName, int defaultDataType) {
        Integer dataType = jdbcOperations.execute((ConnectionCallback<Integer>) conn -> {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet rs = databaseMetaData.getColumns(null, null, TABLE_NAME, columnName);
            if (rs.next()) {
                return rs.getInt("DATA_TYPE");
            }

            rs = databaseMetaData.getColumns(null, null, TABLE_NAME.toUpperCase(), columnName.toUpperCase());
            if (rs.next()) {
                return rs.getInt("DATA_TYPE");
            }
            return null;
        });
        return new OracleJdbcOAuth2AuthorizationService.ColumnMetadata(columnName, dataType != null ? dataType : defaultDataType);
    }

    protected static SqlParameterValue mapToSqlParameter(String columnName, String value) {
        ColumnMetadata columnMetadata = columnMetadataMap.get(columnName);
        return Types.BLOB == columnMetadata.getDataType() && StringUtils.hasText(value) ?
                new SqlParameterValue(Types.BLOB, value.getBytes(StandardCharsets.UTF_8)) :
                new SqlParameterValue(columnMetadata.getDataType(), value);
    }

    private static final class ColumnMetadata {
        private final String columnName;
        private final int dataType;

        private ColumnMetadata(String columnName, int dataType) {
            this.columnName = columnName;
            this.dataType = dataType;
        }

        private String getColumnName() {
            return this.columnName;
        }

        private int getDataType() {
            return this.dataType;
        }
    }

    private static final class LobCreatorArgumentPreparedStatementSetter extends ArgumentPreparedStatementSetter {
        private final LobCreator lobCreator;

        private LobCreatorArgumentPreparedStatementSetter(LobCreator lobCreator, Object[] args) {
            super(args);
            this.lobCreator = lobCreator;
        }

        @Override
        protected void doSetValue(PreparedStatement ps, int parameterPosition, @Nullable Object argValue) throws SQLException {
            if (argValue instanceof SqlParameterValue paramValue) {
                if (paramValue.getSqlType() == Types.BLOB) {
                    if (paramValue.getValue() != null) {
                        Assert.isInstanceOf(byte[].class, paramValue.getValue(),
                                "Value of blob parameter must be byte[]");
                    }
                    byte[] valueBytes = (byte[]) paramValue.getValue();
                    this.lobCreator.setBlobAsBytes(ps, parameterPosition, valueBytes);
                    return;
                }
            }
            super.doSetValue(ps, parameterPosition, argValue);
        }
    }
}
