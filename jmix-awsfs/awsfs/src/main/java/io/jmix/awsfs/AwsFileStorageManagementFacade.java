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

package io.jmix.awsfs;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import org.jspecify.annotations.Nullable;

@ManagedResource(description = "Manages Amazon S3 file storage client", objectName = "jmix.awsfs:type=AwsFileStorage")
@Component("awsfs_AwsFileStorageManagementFacade")
public class AwsFileStorageManagementFacade {
    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @ManagedOperation(description = "Refresh Amazon S3 file storage client")
    public String refreshS3Client() {
        FileStorage fileStorage = fileStorageLocator.getDefault();
        if (fileStorage instanceof AwsFileStorage) {
            ((AwsFileStorage) fileStorage).refreshS3Client();
            return "Refreshed successfully";
        }
        return "Not an Amazon S3 file storage - refresh attempt ignored";
    }

    @ManagedOperation(description = "Refresh Amazon S3 file storage client by storage name")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "storageName", description = "Storage name"),
            @ManagedOperationParameter(name = "accessKey", description = "Amazon S3 access key"),
            @ManagedOperationParameter(name = "secretAccessKey", description = "Amazon S3 secret access key")})
    public String refreshS3Client(String storageName, String accessKey, String secretAccessKey) {
        FileStorage fileStorage = fileStorageLocator.getByName(storageName);
        if (fileStorage instanceof AwsFileStorage) {
            AwsFileStorage awsFileStorage = (AwsFileStorage) fileStorage;
            awsFileStorage.setAccessKey(accessKey);
            awsFileStorage.setSecretAccessKey(secretAccessKey);
            awsFileStorage.refreshS3Client();
            return "Refreshed successfully";
        }
        return "Not an Amazon S3 file storage - refresh attempt ignored";
    }

    @ManagedOperation(description = "Refresh Amazon S3 file storage client by storage name")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "storageName", description = "Storage name"),
            @ManagedOperationParameter(name = "accessKey", description = "Amazon S3 access key"),
            @ManagedOperationParameter(name = "secretAccessKey", description = "Amazon S3 secret access key"),
            @ManagedOperationParameter(name = "region", description = "Amazon S3 region"),
            @ManagedOperationParameter(name = "bucket", description = "Amazon S3 bucket name"),
            @ManagedOperationParameter(name = "chunkSize", description = "Amazon S3 chunk size (kB)"),
            @ManagedOperationParameter(name = "endpointUrl", description = "Optional custom S3 storage endpoint URL"),
            @ManagedOperationParameter(name = "usePathStyleBucketAddressing", description = "Whether to force the client to use path-style addressing for buckets")
    })
    public String refreshS3Client(String storageName, String accessKey, String secretAccessKey,
                                  String region, String bucket, int chunkSize, @Nullable String endpointUrl,
                                  boolean usePathStyleBucketAddressing) {
        FileStorage fileStorage = fileStorageLocator.getByName(storageName);
        if (fileStorage instanceof AwsFileStorage) {
            AwsFileStorage awsFileStorage = (AwsFileStorage) fileStorage;
            awsFileStorage.setAccessKey(accessKey);
            awsFileStorage.setSecretAccessKey(secretAccessKey);
            awsFileStorage.setRegion(region);
            awsFileStorage.setBucket(bucket);
            awsFileStorage.setChunkSize(chunkSize);
            awsFileStorage.setEndpointUrl(endpointUrl);
            awsFileStorage.setUsePathStyleBucketAddressing(usePathStyleBucketAddressing);
            awsFileStorage.refreshS3Client();
            return "Refreshed successfully";
        }
        return "Not an Amazon S3 file storage - refresh attempt ignored";
    }
}
