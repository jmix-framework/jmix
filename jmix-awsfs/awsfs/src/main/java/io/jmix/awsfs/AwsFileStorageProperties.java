package io.jmix.awsfs;

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

    public AwsFileStorageProperties(
            String accessKey,
            String secretAccessKey,
            String region,
            String bucket,
            @DefaultValue("8192") int chunkSize,
            @DefaultValue("") String endpointUrl) {
        this.accessKey = accessKey;
        this.secretAccessKey = secretAccessKey;
        this.region = region;
        this.bucket = bucket;
        this.chunkSize = chunkSize;
        this.endpointUrl = endpointUrl;
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
}
