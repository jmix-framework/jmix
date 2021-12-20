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

package com.haulmont.cuba.core;

import io.jmix.core.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.util.*;

@ManagedResource(objectName = "jmix.cuba:type=MigrationHelper", description = "Contains CUBA to Jmix migration actions")
@Component("cuba_MigrationHelperBean")
public class MigrationHelperManagementFacade {
    private static final String DEFAULT_STORAGE_NAME = "fs";
    private static final int BATCH_SIZE = 50000;

    private static final String COUNT_TEMPLATE = "select count(*) from %s where (%s is null and %s is not null)";
    private static final String LOAD_TEMPLATE = "select t.ID as tid, f.create_date as file_date, f.id as file_id, " +
            "f.ext as file_ext, f.name as file_name, f.file_size as file_size from %s t join sys_file f on t.%s = f.id " +
            "where(t.%s is null and t.%s is not null);";
    private static final String UPDATE_TEMPLATE = "update %s set %s = ? where ID = ?";


    @Autowired
    private JmixModules jmixModules;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @ManagedOperation(description = "Checks cuba tables and fills FileRef column value for entities with FileDescriptor fields")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "storageName", description = "File Storage prefix. Leave empty to set default prefix \"fs\"")
    })
    public String convertCubaFileDescriptors(@Nullable String storageName) {
        StringBuilder builder = new StringBuilder("Looking for modules with tables to migrate...\n");
        for (JmixModuleDescriptor descriptor : jmixModules.getAll()) {

            if ("io.jmix.reports".equals(descriptor.getId())) {
                builder.append("REPORTS module found. Processing...\n");

                builder.append('\t')
                        .append(migrateTableData("REPORT_EXECUTION",
                                "OUTPUT_DOCUMENT_ID", "OUTPUT_DOCUMENT", storageName))
                        .append('\n');
            }

            if ("io.jmix.email".equals(descriptor.getId())) {
                builder.append("EMAIL module found. Processing...\n");

                builder.append('\t')
                        .append(migrateTableData("EMAIL_SENDING_MESSAGE",
                                "CONTENT_TEXT_FILE_ID", "CONTENT_TEXT_FILE", storageName))
                        .append('\n');

                builder.append('\t').append(migrateTableData("EMAIL_SENDING_ATTACHMENT",
                        "CONTENT_FILE_ID", "CONTENT_FILE", storageName))
                        .append('\n');
            }
        }
        builder.append("Finished.");
        return builder.toString();
    }

    @ManagedOperation(description = "Fills FileRef column value for migrated WebDAV entities")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "storageName", description = "File Storage prefix. Leave empty to set default prefix \"fs\"")
    })
    public String convertCubaFileDescriptorsForWebdav(@Nullable String storageName) {
        StringBuilder builder = new StringBuilder("Looking for modules with tables to migrate...\n");
        for (JmixModuleDescriptor descriptor : jmixModules.getAll()) {

            if ("io.jmix.webdav".equals(descriptor.getId())) {
                builder.append("WebDAV module found. Processing...\n");

                builder.append('\t')
                        .append(migrateTableData("WEBDAV_WEBDAV_DOCUMENT_VERSION",
                                "FILE_DESCRIPTOR_ID", "FILE_REFERENCE", storageName))
                        .append('\n');
            }
        }
        builder.append("Finished.");
        return builder.toString();
    }

    @ManagedOperation(description = "Fills FileRef column for custom table")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "tableName", description = "Database table name"),
            @ManagedOperationParameter(name = "descriptorColumn", description = "Name of column used to store FileDescriptor id"),
            @ManagedOperationParameter(name = "refColumn", description = "Name of column related with FileRef field"),
            @ManagedOperationParameter(name = "storageName", description = "File Storage prefix. Leave empty to set default prefix \"fs\"")
    })
    @Transactional
    public String migrateTableData(String tableName, String descriptorColumn, String refColumn, @Nullable String storageName) {
        if (storageName == null)
            storageName = DEFAULT_STORAGE_NAME;

        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setMaxRows(BATCH_SIZE);

        Long initial = template.queryForObject(String.format(COUNT_TEMPLATE, tableName, refColumn, descriptorColumn), Long.class);
        long rest = initial != null ? initial : 0;
        long migrated = 0;

        while (rest > 0) {
            String loadQuery = String.format(LOAD_TEMPLATE, tableName, descriptorColumn, refColumn, descriptorColumn);
            List<Map<String, Object>> records = template.queryForList(loadQuery);

            List<Object[]> fileRefs = new ArrayList<>(records.size());

            for (Map<String, Object> record : records) {
                String ref = buildFileRef(storageName,
                        (Date) record.get("file_date"),
                        getUUIDString(record.get("file_id")),
                        (String) record.get("file_ext"),
                        (String) record.get("file_name"),
                        String.valueOf(record.get("file_size")));
                fileRefs.add(new Object[]{ref, record.get("tid")});
            }

            template.batchUpdate(String.format(UPDATE_TEMPLATE, tableName, refColumn), fileRefs);

            //noinspection ConstantConditions
            rest = template.queryForObject(String.format(COUNT_TEMPLATE, tableName, refColumn, descriptorColumn), Long.class);
            migrated += fileRefs.size();
        }

        return String.format("%s/%s FileRefs created for table '%s' at column '%s' using descriptor from column '%s'",
                migrated, initial, tableName, refColumn, descriptorColumn);
    }

    protected String buildFileRef(String storageName, Date createdDate, String uuid,
                                  String extension, String fileName, String fileSize) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String datePath = year + "/"
                + StringUtils.leftPad(String.valueOf(month), 2, '0') + "/"
                + StringUtils.leftPad(String.valueOf(day), 2, '0');

        String fileExtension = StringUtils.isNoneBlank(extension)
                ? "." + extension
                : StringUtils.EMPTY;

        String path = datePath + "/" + uuid + fileExtension;
        FileRef fileRef = new FileRef(storageName, path, fileName);
        fileRef.addParameter("size", fileSize);
        return fileRef.toString();
    }

    private String getUUIDString(Object dbValue) {
        if (dbValue instanceof UUID) {
            return dbValue.toString();
        } else if (dbValue instanceof String) {
            return UuidProvider.fromString((String) dbValue).toString();
        } else {
            throw new RuntimeException("Cannot convert '" + dbValue.getClass().getName() + "' to UUID. Check that descriptorColumn specified correctly.");
        }
    }
}
