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

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.awsfs")
public class AwsFileStorageProperties {

    /**
     * Amazon S3 access key.
     */
    String accessKey;

    /**
     * Amazon S3 secret access key.
     */
    String secretAccessKey;

    /**
     * Amazon S3 region.
     */
    String region;

    /**
     * Amazon S3 bucket name.
     */
    String bucket;

    /**
     * Amazon S3 chunk size (kB).
     */
    int chunkSize;

    /**
     * Custom S3 storage endpoint URL.
     */
    String endpointUrl;

    /**
     * Whether to force the client to use path-style addressing for buckets.
     */
    boolean usePathStyleBucketAddressing;

    /**
     * Whether to send the SHA-256 checksum of the uploaded files to S3. If not set, the checksum is sent when the
     * custom endpoint URL is not set (Amazon S3) and is not sent otherwise, as an S3-compatible storage may reject
     * the checksum headers.
     */
    @Nullable Boolean checksumEnabled;

    public AwsFileStorageProperties(
            String accessKey,
            String secretAccessKey,
            String region,
            String bucket,
            @DefaultValue("8192") int chunkSize,
            @DefaultValue("") String endpointUrl,
            @DefaultValue("false") boolean usePathStyleBucketAddressing,
            @Nullable Boolean checksumEnabled) {
        this.accessKey = accessKey;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
        this.bucket = bucket;
        this.chunkSize = chunkSize;
        this.endpointUrl = endpointUrl;
        this.usePathStyleBucketAddressing = usePathStyleBucketAddressing;
        this.checksumEnabled = checksumEnabled;
    }

    /**
     * @see #accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * @see #secretAccessKey
     */
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    /**
     * @see #region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @see #bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * @see #chunkSize
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * @see #endpointUrl
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * @see #usePathStyleBucketAddressing
     */
    public boolean getUsePathStyleBucketAddressing() {
        return usePathStyleBucketAddressing;
    }

    /**
     * @see #checksumEnabled
     */
    public @Nullable Boolean getChecksumEnabled() {
        return checksumEnabled;
    }
}
