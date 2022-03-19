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

package io.jmix.awsfs;

import com.google.common.base.Strings;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.TimeSource;
import io.jmix.core.UuidProvider;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Internal
@Component("awsfs_FileStorage")
public class AwsFileStorage implements FileStorage {

    private static final Logger log = LoggerFactory.getLogger(AwsFileStorage.class);
    private static final String DEFAULT_STORAGE_NAME = "s3";

    protected String storageName;

    @Autowired
    protected AwsFileStorageProperties properties;

    boolean useConfigurationProperties = true;

    protected String accessKey;
    protected String secretAccessKey;
    protected String region;
    protected String bucket;
    protected int chunkSize;
    protected String endpointUrl;

    @Autowired
    protected TimeSource timeSource;

    protected AtomicReference<S3Client> s3ClientReference = new AtomicReference<>();

    public AwsFileStorage() {
        this(DEFAULT_STORAGE_NAME);
    }

    public AwsFileStorage(String storageName) {
        this.storageName = storageName;
    }

    /**
     * Optional constructor that allows you to override {@link AwsFileStorageProperties}.
     */
    public AwsFileStorage(String storageName, String accessKey, String secretAccessKey,
                          String region, String bucket, int chunkSize, @Nullable String endpointUrl) {
        this.useConfigurationProperties = false;
        this.storageName = storageName;
        this.accessKey = accessKey;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
        this.bucket = bucket;
        this.chunkSize = chunkSize;
        this.endpointUrl = endpointUrl;
    }

    @EventListener
    protected void initS3Client(ApplicationStartedEvent event) {
        refreshS3Client();
    }

    protected void refreshProperties() {
        if (useConfigurationProperties) {
            this.accessKey = properties.getAccessKey();
            this.secretAccessKey = properties.getSecretAccessKey();
            this.region = properties.getRegion();
            this.bucket = properties.getBucket();
            this.chunkSize = properties.getChunkSize();
            this.endpointUrl = properties.getEndpointUrl();
        }
    }

    protected AwsCredentialsProvider getAwsCredentialsProvider() {
        if (accessKey != null && secretAccessKey != null) {
            AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretAccessKey);
            return StaticCredentialsProvider.create(awsCredentials);
        } else {
            return DefaultCredentialsProvider.builder().build();
        }
    }

    public void refreshS3Client() {
        refreshProperties();
        Preconditions.checkNotEmptyString(region, "region must not be empty");
        Preconditions.checkNotEmptyString(bucket, "bucket must not be empty");
        AwsCredentialsProvider awsCredentialsProvider = getAwsCredentialsProvider();
        if (Strings.isNullOrEmpty(endpointUrl)) {
            s3ClientReference.set(S3Client.builder()
                    .credentialsProvider(awsCredentialsProvider)
                    .region(Region.of(region))
                    .build());
        } else {
            s3ClientReference.set(S3Client.builder()
                    .credentialsProvider(awsCredentialsProvider)
                    .endpointOverride(URI.create(endpointUrl))
                    .region(Region.of(region))
                    .build());
        }
    }

    @Override
    public String getStorageName() {
        return storageName;
    }

    protected String createFileKey(String fileName) {
        return createDateDir() + "/" + createUuidFilename(fileName);
    }

    protected String createDateDir() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeSource.currentTimestamp());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return String.format("%d/%s/%s", year,
                StringUtils.leftPad(String.valueOf(month), 2, '0'),
                StringUtils.leftPad(String.valueOf(day), 2, '0'));
    }

    protected String createUuidFilename(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isNotEmpty(extension)) {
            return UuidProvider.createUuid().toString() + "." + extension;
        } else {
            return UuidProvider.createUuid().toString();
        }
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream) {
        String fileKey = createFileKey(fileName);
        int s3ChunkSizeBytes = this.chunkSize * 1024;
        try (BufferedInputStream bos = new BufferedInputStream(inputStream, s3ChunkSizeBytes)) {
            S3Client s3Client = s3ClientReference.get();
            int totalSizeBytes = bos.available();
            if (totalSizeBytes == 0) {
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build(), RequestBody.empty());
                return new FileRef(getStorageName(), fileKey, fileName);
            }

            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);

            List<CompletedPart> completedParts = new ArrayList<>();
            for (int partNumber = 1, readBytes = 0; readBytes != totalSizeBytes; partNumber++) {
                byte[] chunkBytes = new byte[Math.min(totalSizeBytes - readBytes, s3ChunkSizeBytes)];
                readBytes += bos.read(chunkBytes);
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .uploadId(response.uploadId())
                        .partNumber(partNumber)
                        .build();
                String eTag = s3Client.uploadPart(uploadPartRequest, RequestBody.fromBytes(chunkBytes)).eTag();
                CompletedPart part = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(eTag)
                        .build();
                completedParts.add(part);
            }

            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    CompleteMultipartUploadRequest.builder()
                            .bucket(bucket)
                            .key(fileKey)
                            .uploadId(response.uploadId())
                            .multipartUpload(completedMultipartUpload).build();
            s3Client.completeMultipartUpload(completeMultipartUploadRequest);
            return new FileRef(getStorageName(), fileKey, fileName);
        } catch (IOException | SdkException e) {
            log.error("Error saving file to S3 storage", e);
            String message = String.format("Could not save file %s.", fileName);
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, message);
        }
    }

    @Override
    public InputStream openStream(FileRef reference) {
        InputStream is;
        try {
            S3Client s3Client = s3ClientReference.get();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(reference.getPath())
                    .build();
            is = s3Client.getObject(getObjectRequest, ResponseTransformer.toInputStream());
        } catch (SdkException e) {
            log.error("Error loading file from S3 storage", e);
            String message = String.format("Could not load file %s.", reference.getFileName());
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, message);
        }
        return is;
    }

    @Override
    public void removeFile(FileRef reference) {
        try {
            S3Client s3Client = s3ClientReference.get();
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(reference.getPath())
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkException e) {
            log.error("Error removing file from S3 storage", e);
            String message = String.format("Could not delete file %s.", reference.getFileName());
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, message);
        }
    }

    @Override
    public boolean fileExists(FileRef reference) {
        S3Client s3Client = s3ClientReference.get();
        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(reference.getPath())
                .maxKeys(1)
                .build();
        ListObjectsV2Response listObjResponse = s3Client.listObjectsV2(listObjectsReqManual);
        return listObjResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList())
                .contains(reference.getPath());
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setEndpointUrl(@Nullable String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
}
