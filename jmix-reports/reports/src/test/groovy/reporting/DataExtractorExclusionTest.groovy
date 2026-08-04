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

package reporting

import io.jmix.reports.yarg.loaders.ReportDataLoader
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory
import io.jmix.reports.yarg.reporting.DataExtractor
import io.jmix.reports.yarg.reporting.DataExtractorImpl
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.BandOrientation
import io.jmix.reports.yarg.structure.Report
import io.jmix.reports.yarg.structure.ReportBand
import io.jmix.reports.yarg.structure.ReportQuery
import spock.lang.Specification

/**
 * The extractor can skip named bands (their data comes from a streaming cursor) while still
 * registering them as first-level definitions and extracting all sibling bands normally.
 */
class DataExtractorExclusionTest extends Specification {

    def "excluded band is not extracted while sibling bands are"() {
        given: "a report with two first-level bands backed by a counting loader"
        def loadedQueries = []
        ReportDataLoader loader = { query, parentBand, params ->
            loadedQueries << query.getName()
            return [[v: 1]]
        }
        ReportLoaderFactory loaderFactory = { loaderType -> loader }
        def extractor = new DataExtractorImpl(loaderFactory)

        def hot = band("Hot", query("hotQuery"))
        def small = band("Small", query("smallQuery"))
        def rootDefinition = rootDefinition(hot, small)
        Report report = Mock(Report) {
            getRootBand() >> rootDefinition
        }
        def rootBand = new BandData(BandData.ROOT_BAND_NAME)
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>())

        when:
        extractor.extractData(report, [:], rootBand, ["Hot"] as Set)

        then:
        loadedQueries == ["smallQuery"]
        rootBand.getChildrenByName("Small").size() == 1
        rootBand.getChildrenByName("Hot").isEmpty()
        rootBand.getFirstLevelBandDefinitionNames().containsAll(["Hot", "Small"])
    }

    def "old 3-arg extractData keeps extracting everything"() {
        given:
        def loadedQueries = []
        ReportDataLoader loader = { query, parentBand, params ->
            loadedQueries << query.getName()
            return [[v: 1]]
        }
        ReportLoaderFactory loaderFactory = { loaderType -> loader }
        def extractor = new DataExtractorImpl(loaderFactory)
        def rootDefinition = rootDefinition(band("A", query("qa")), band("B", query("qb")))
        Report report = Mock(Report) {
            getRootBand() >> rootDefinition
        }
        def rootBand = new BandData(BandData.ROOT_BAND_NAME)
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>())

        when:
        extractor.extractData(report, [:], rootBand)

        then:
        loadedQueries == ["qa", "qb"]
    }

    def "default 4-arg extractData rejects exclusions it cannot honor"() {
        given: "a custom extractor implementing only the legacy 3-arg method"
        def extractor = { report, params, rootBand -> } as DataExtractor

        when: "the streaming path asks to exclude a band"
        extractor.extractData(Mock(Report), [:], new BandData(BandData.ROOT_BAND_NAME), ["Hot"] as Set)

        then: "it fails fast instead of silently materializing the streaming band"
        thrown(UnsupportedOperationException)
    }

    def "default 4-arg extractData with no exclusions delegates to the legacy method"() {
        given:
        def called = false
        def extractor = { report, params, rootBand -> called = true } as DataExtractor

        when:
        extractor.extractData(Mock(Report), [:], new BandData(BandData.ROOT_BAND_NAME), [] as Set)

        then:
        called
    }

    private ReportQuery query(String name) {
        return Mock(ReportQuery) {
            getName() >> name
            getLoaderType() >> "test"
            getScript() >> "s"
            getLinkParameterName() >> null
        }
    }

    private ReportBand band(String name, ReportQuery reportQuery) {
        return Mock(ReportBand) {
            getName() >> name
            getPosition() >> 0
            getChildren() >> []
            getReportQueries() >> [reportQuery]
            getBandOrientation() >> BandOrientation.HORIZONTAL
        }
    }

    private ReportBand rootDefinition(ReportBand... children) {
        return Mock(ReportBand) {
            getName() >> BandData.ROOT_BAND_NAME
            getPosition() >> 0
            getChildren() >> (children as List)
            getReportQueries() >> []
            getBandOrientation() >> BandOrientation.HORIZONTAL
        }
    }
}
