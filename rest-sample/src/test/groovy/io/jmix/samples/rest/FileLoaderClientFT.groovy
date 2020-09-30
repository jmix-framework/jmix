/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest

import io.jmix.samples.rest.service.FileLoaderTestService
import org.junit.Ignore
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import spock.lang.Specification

//todo file storage
@Ignore
class FileLoaderClientFT extends Specification {

    def retrofit = new Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl('http://localhost:8080/app-portal/')
            .build()

    def "FileLoader loads text file"() {
        def testService = retrofit.create(FileLoaderTestService)

        when:
        def file = testService.loadTextFile().execute()

        then:
        file.code() == 200
        //file.body() == 'OK'
    }

    def "FileLoader throws FileStorageException if file descriptor removed"() {
        def testService = retrofit.create(FileLoaderTestService)

        when:
        def file = testService.loadRemovedTextFile().execute()

        then:
        file.code() == 200
        //file.body() == 'OK'
    }

    def "FileLoader throws FileStorageException FILE_NOT_FOUND if file not found"() {
        def testService = retrofit.create(FileLoaderTestService)

        when:
        def file = testService.loadNotFoundTextFile().execute()

        then:
        file.code() == 200
        //file.body() == 'OK'
    }

    def "FileLoader throws FileStorageException FILE_NOT_FOUND if file descriptor is not committed"() {
        def testService = retrofit.create(FileLoaderTestService)

        when:
        def file = testService.loadNonCommittedTextFile().execute()

        then:
        file.code() == 200
        //file.body() == 'OK'
    }
}
