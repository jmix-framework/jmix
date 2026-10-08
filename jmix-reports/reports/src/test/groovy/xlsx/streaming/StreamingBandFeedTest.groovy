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

package xlsx.streaming

import io.jmix.reports.yarg.formatters.impl.streaming.StreamingBandFeed
import io.jmix.reports.yarg.structure.BandData
import spock.lang.Specification

/**
 * The streaming band feed wraps cursor row maps into transient {@link BandData} instances that are
 * never attached to the band tree, and reproduces the extractor's empty-row semantics.
 */
class StreamingBandFeedTest extends Specification {

    def "wraps row maps into transient BandData without attaching them to the root"() {
        given:
        def root = new BandData(BandData.ROOT_BAND_NAME)
        def rows = [[a: 1], [a: 2]].iterator()
        def feed = new StreamingBandFeed("Data", rows, root)

        when:
        def iterator = feed.iterator()
        def first = iterator.next()
        def second = iterator.next()

        then:
        first.name == "Data"
        first.parentBand.is(root)
        first.data == [a: 1]
        second.data == [a: 2]
        !iterator.hasNext()
        root.childrenList.isEmpty()
    }

    def "first row is retained as a detached plain-map copy while the render sees the original map"() {
        given:
        def source = [a: 1, b: "x"] as LinkedHashMap
        def root = new BandData(BandData.ROOT_BAND_NAME)
        def feed = new StreamingBandFeed("Data", [source].iterator(), root, true)

        when:
        def rendered = []
        feed.iterator().each { rendered << it }

        then: "the rendered band keeps the original map (EntityMap path-alias semantics)"
        rendered[0].data.is(source)

        and: "the retained first row is an independent plain copy usable after the transaction closes"
        !feed.firstRow.data.is(source)
        feed.firstRow.data == [a: 1, b: "x"]
        feed.firstRow.data instanceof HashMap
    }

    def "first-row copy materializes a lazy EntityMap-like source whose size() is 0 until entrySet()"() {
        given: "a source that reports size()==0 until entrySet() lazily materializes it (mimics EntityMap)"
        def source = new LazyRow([a: 1, b: "x"])
        def root = new BandData(BandData.ROOT_BAND_NAME)
        def feed = new StreamingBandFeed("Data", [source].iterator(), root, true)

        when: "the feed is consumed (the render sees the lazy map; the first-row copy is taken eagerly)"
        feed.iterator().each { it }

        then: "the retained copy is fully materialized, not an empty map"
        feed.firstRow.data == [a: 1, b: "x"]
    }

    def "empty source yields exactly one empty row (putEmptyRowIfNoDataSelected semantics)"() {
        given:
        def feed = new StreamingBandFeed("Data", [].iterator(), new BandData(BandData.ROOT_BAND_NAME))

        when:
        def iterator = feed.iterator()

        then:
        iterator.hasNext()
        iterator.next().data.isEmpty()
        !iterator.hasNext()
    }

    def "empty source yields no rows when putEmptyRowIfNoDataSelected is off"() {
        given:
        def feed = new StreamingBandFeed("Data", [].iterator(), new BandData(BandData.ROOT_BAND_NAME), false)

        when:
        def iterator = feed.iterator()

        then:
        !iterator.hasNext()
    }

    def "iterator is one-shot"() {
        given:
        def feed = new StreamingBandFeed("Data", [[a: 1]].iterator(), new BandData(BandData.ROOT_BAND_NAME))
        feed.iterator()

        when:
        feed.iterator()

        then:
        thrown(IllegalStateException)
    }

    /**
     * Mimics {@code io.jmix.reports.app.EntityMap}: {@code size()} stays 0 until {@code entrySet()}
     * (or another materializing accessor) is called, and {@code isEmpty()} is hardcoded to false.
     * A plain {@code new HashMap<>(this)} therefore copies nothing, because {@code HashMap(Map)}
     * checks {@code size()} and skips {@code entrySet()} when it is 0.
     */
    static class LazyRow extends HashMap<String, Object> {
        private final Map<String, Object> pending
        private boolean loaded = false

        LazyRow(Map<String, Object> pending) {
            this.pending = pending
        }

        @Override
        int size() {
            return loaded ? super.size() : 0
        }

        @Override
        boolean isEmpty() {
            return false
        }

        @Override
        Set<Map.Entry<String, Object>> entrySet() {
            if (!loaded) {
                super.putAll(pending)
                loaded = true
            }
            return super.entrySet()
        }
    }
}
