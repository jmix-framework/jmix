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

import io.jmix.awsfs.AwsFileStorage
import io.jmix.awsfs.AwsFileStorageConfiguration
import io.jmix.core.CoreConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification
import test_support.AwsFileStorageTestConfiguration
import test_support.TestContextInititalizer

@ContextConfiguration(
        classes = [CoreConfiguration, AwsFileStorageConfiguration, AwsFileStorageTestConfiguration],
        initializers = [TestContextInititalizer]
)
@TestPropertySource(properties = 'jmix.core.file-digest-on-save-enabled=false')
class AwsFileStorageDigestDisabledTest extends Specification {

    @Autowired
    ApplicationContext applicationContext

    S3Client s3Client = Mock()

    def "saveStream adds no digest when switched off"() {
        def storage = new AwsFileStorage('s3test', 'key', 'secret', 'eu-west-1', 'bucket', 8, null)
        applicationContext.autowireCapableBeanFactory.autowireBean(storage)
        storage.s3ClientReference.set(s3Client)

        when:
        def fileRef = storage.saveStream('a.txt', new ByteArrayInputStream('abc'.bytes), [:])

        then:
        1 * s3Client.putObject(_, _)
        !fileRef.parameters.containsKey('jmix-sha-256')
    }

    def "a passed digest is kept without a check when switched off"() {
        def storage = new AwsFileStorage('s3test', 'key', 'secret', 'eu-west-1', 'bucket', 8, null)
        applicationContext.autowireCapableBeanFactory.autowireBean(storage)
        storage.s3ClientReference.set(s3Client)
        // The digest of empty content, not of 'abc'
        def passedDigest = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'

        when:
        def fileRef = storage.saveStream('a.txt', new ByteArrayInputStream('abc'.bytes), ['jmix-sha-256': passedDigest])

        then:
        1 * s3Client.putObject(_, _)
        fileRef.parameters == ['jmix-sha-256': passedDigest]
    }
}
