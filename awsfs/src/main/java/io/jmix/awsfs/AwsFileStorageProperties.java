package io.jmix.awsfs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.filestorage.aws")
@ConstructorBinding
public class AwsFileStorageProperties {
    String accessKey;
    String secretAccessKey;
    String region;
    String bucket;
    int chunkSize;
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
     * Amazon S3 access key.
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Amazon S3 secret access key.
     */
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    /**
     * Amazon S3 region.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Amazon S3 bucket name.
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Amazon S3 chunk size (kB).
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Return custom S3 storage endpoint URL.
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
