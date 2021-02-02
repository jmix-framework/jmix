package io.jmix.awsfs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages Amazon S3 file storage client", objectName = "jmix.awsfs:type=AwsFileStorage")
@Component("awsfs_AwsFileStorageManagementFacade")
public class AwsFileStorageManagementFacade {
    @Autowired
    private AwsFileStorage awsFileStorage;

    @ManagedOperation(description = "Refresh Amazon S3 file storage client")
    public String refreshS3Client() {
        awsFileStorage.refreshS3Client();
        return "Refreshed successfully";
    }
}
