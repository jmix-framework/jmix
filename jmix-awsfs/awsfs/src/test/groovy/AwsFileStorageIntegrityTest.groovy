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
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse
import software.amazon.awssdk.services.s3.model.UploadPartResponse
import spock.lang.Specification
import test_support.AwsFileStorageTestConfiguration
import test_support.TestContextInititalizer

import java.security.MessageDigest

@ContextConfiguration(
        classes = [CoreConfiguration, AwsFileStorageConfiguration, AwsFileStorageTestConfiguration],
        initializers = [TestContextInititalizer]
)
class AwsFileStorageIntegrityTest extends Specification {

    static final String DIGEST_PARAMETER = 'jmix-sha-256'
    static final String ABC_SHA_256 = 'ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad'
    static final String EMPTY_SHA_256 = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'

    // The storage is created with the chunk size of 8 kB.
    static final int CHUNK_SIZE_BYTES = 8 * 1024

    @Autowired
    ApplicationContext applicationContext

    S3Client s3Client = Mock()

    def "single-part upload sends the SHA-256 checksum and adds the digest"() {
        def storage = storage(null)

        when:
        def fileRef = storage.saveStream('a.txt', stream('abc'), [:])

        then:
        1 * s3Client.putObject({ it.checksumAlgorithm() == ChecksumAlgorithm.SHA256 }, _)
        fileRef.parameters[DIGEST_PARAMETER] == ABC_SHA_256
    }

    def "empty file is uploaded in one part"() {
        def storage = storage(null)

        when:
        def fileRef = storage.saveStream('empty.txt', stream(''), [:])

        then:
        1 * s3Client.putObject(_, _)
        0 * s3Client.createMultipartUpload(_)
        fileRef.parameters[DIGEST_PARAMETER] == EMPTY_SHA_256
    }

    def "exactly chunkSize bytes go through a multipart upload"() {
        def storage = storage(null)
        byte[] content = content(CHUNK_SIZE_BYTES)

        when:
        def fileRef = storage.saveStream('chunk.bin', new ByteArrayInputStream(content), [:])

        then:
        1 * s3Client.createMultipartUpload(_) >> CreateMultipartUploadResponse.builder().uploadId('u').build()
        1 * s3Client.uploadPart(_, _) >> UploadPartResponse.builder().eTag('e1').checksumSHA256('c1').build()
        1 * s3Client.completeMultipartUpload(_)
        0 * s3Client.putObject(_, _)
        fileRef.parameters[DIGEST_PARAMETER] == sha256Hex(content)
    }

    def "multipart upload sends part checksums and completes with them"() {
        def storage = storage(null)
        byte[] content = content(CHUNK_SIZE_BYTES * 2 + 1)

        when:
        def fileRef = storage.saveStream('big.bin', new ByteArrayInputStream(content), [:])

        then:
        1 * s3Client.createMultipartUpload({ it.checksumAlgorithm() == ChecksumAlgorithm.SHA256 }) >>
                CreateMultipartUploadResponse.builder().uploadId('u').build()
        3 * s3Client.uploadPart({ it.checksumAlgorithm() == ChecksumAlgorithm.SHA256 && it.uploadId() == 'u' }, _) >>>
                ['c1', 'c2', 'c3'].collect { UploadPartResponse.builder().eTag('e' + it).checksumSHA256(it).build() }
        1 * s3Client.completeMultipartUpload({
            it.uploadId() == 'u' && it.multipartUpload().parts()*.checksumSHA256() == ['c1', 'c2', 'c3']
        })
        fileRef.parameters[DIGEST_PARAMETER] == sha256Hex(content)
    }

    def "custom endpoint sends no checksums by default"() {
        def storage = storage('http://localhost:9000')

        when:
        def fileRef = storage.saveStream('a.txt', stream('abc'), [:])

        then:
        1 * s3Client.putObject({ it.checksumAlgorithm() == null }, _)
        fileRef.parameters[DIGEST_PARAMETER] == ABC_SHA_256
    }

    def "custom endpoint sends no checksums in a multipart upload"() {
        def storage = storage('http://localhost:9000')
        byte[] content = content(CHUNK_SIZE_BYTES + 1)

        when:
        storage.saveStream('big.bin', new ByteArrayInputStream(content), [:])

        then:
        1 * s3Client.createMultipartUpload({ it.checksumAlgorithm() == null }) >>
                CreateMultipartUploadResponse.builder().uploadId('u').build()
        2 * s3Client.uploadPart({ it.checksumAlgorithm() == null }, _) >> UploadPartResponse.builder().eTag('e').build()
        1 * s3Client.completeMultipartUpload({
            it.multipartUpload().parts().size() == 2 && it.multipartUpload().parts().every { part -> part.checksumSHA256() == null }
        })
    }

    def "the switch overrides the endpoint default"() {
        def storage = storage(endpointUrl)
        storage.setChecksumEnabled(checksumEnabled)

        when:
        storage.saveStream('a.txt', stream('abc'), [:])

        then:
        1 * s3Client.putObject({ it.checksumAlgorithm() == expectedAlgorithm }, _)

        where:
        endpointUrl             | checksumEnabled || expectedAlgorithm
        'http://localhost:9000' | true            || ChecksumAlgorithm.SHA256
        null                    | false           || null
        ''                      | null            || ChecksumAlgorithm.SHA256
    }

    def "a reserved parameter is rejected"() {
        def storage = storage(null)

        when:
        storage.saveStream('a.txt', stream('abc'), [(DIGEST_PARAMETER): 'x'])

        then:
        thrown(IllegalArgumentException)
        0 * s3Client._
    }

    def "application parameters are kept next to the digest"() {
        def storage = storage(null)

        when:
        def fileRef = storage.saveStream('a.txt', stream('abc'), ['sha-256': 'mine', 'owner': 'me'])

        then:
        1 * s3Client.putObject(_, _)
        fileRef.parameters['sha-256'] == 'mine'
        fileRef.parameters['owner'] == 'me'
        fileRef.parameters[DIGEST_PARAMETER] == ABC_SHA_256
    }

    AwsFileStorage storage(String endpointUrl) {
        def storage = new AwsFileStorage('s3test', 'key', 'secret', 'eu-west-1', 'bucket', 8, endpointUrl)
        applicationContext.autowireCapableBeanFactory.autowireBean(storage)
        storage.s3ClientReference.set(s3Client)
        return storage
    }

    static InputStream stream(String text) {
        return new ByteArrayInputStream(text.bytes)
    }

    static byte[] content(int size) {
        byte[] content = new byte[size]
        for (int i = 0; i < size; i++) {
            content[i] = (byte) (i % 251)
        }
        return content
    }

    static String sha256Hex(byte[] content) {
        return HexFormat.of().formatHex(MessageDigest.getInstance('SHA-256').digest(content))
    }
}
