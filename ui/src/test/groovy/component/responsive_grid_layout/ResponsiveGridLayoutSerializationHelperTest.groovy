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

package component.responsive_grid_layout

import io.jmix.ui.widget.JmixResponsiveGridLayout
import spock.lang.Specification

import static io.jmix.ui.widget.JmixResponsiveGridLayout.*
import static io.jmix.ui.widget.responsivegridlayout.ResponsiveGridLayoutSerializationHelper.serialize
import static io.jmix.ui.widget.responsivegridlayout.ResponsiveGridLayoutSerializationHelper.toStyle

class ResponsiveGridLayoutSerializationHelperTest extends Specification {

    protected static final String STYLE_PROPERTY = "style"
    protected static final String STYLE_SEPARATOR = " "

    def "convert container type to style"() {
        expect:
        toStyle(type) == className

        where:
        type                | className
        ContainerType.FIXED | "container"
        ContainerType.FLUID | "container-fluid"
    }

    def "convert row columns to style"() {
        expect:
        toStyle(breakpoint, columns) == className

        where:
        breakpoint    | columns                    || className
        Breakpoint.XS | RowColumnsValue.columns(3) || "row-cols-3"
        Breakpoint.SM | RowColumnsValue.columns(3) || "row-cols-sm-3"
        Breakpoint.MD | RowColumnsValue.columns(3) || "row-cols-md-3"
        Breakpoint.LG | RowColumnsValue.columns(3) || "row-cols-lg-3"
        Breakpoint.XL | RowColumnsValue.columns(3) || "row-cols-xl-3"
    }

    def "convert align items to style"() {
        expect:
        toStyle(breakpoint, alignItems) == className

        where:
        breakpoint    | alignItems          || className
        Breakpoint.XS | AlignItems.START    || "align-items-start"
        Breakpoint.XS | AlignItems.CENTER   || "align-items-center"
        Breakpoint.XS | AlignItems.END      || "align-items-end"
        Breakpoint.XS | AlignItems.BASELINE || "align-items-baseline"
        Breakpoint.XS | AlignItems.STRETCH  || "align-items-stretch"

        Breakpoint.SM | AlignItems.START    || "align-items-sm-start"
        Breakpoint.SM | AlignItems.CENTER   || "align-items-sm-center"
        Breakpoint.SM | AlignItems.END      || "align-items-sm-end"
        Breakpoint.SM | AlignItems.BASELINE || "align-items-sm-baseline"
        Breakpoint.SM | AlignItems.STRETCH  || "align-items-sm-stretch"

        Breakpoint.MD | AlignItems.START    || "align-items-md-start"
        Breakpoint.MD | AlignItems.CENTER   || "align-items-md-center"
        Breakpoint.MD | AlignItems.END      || "align-items-md-end"
        Breakpoint.MD | AlignItems.BASELINE || "align-items-md-baseline"
        Breakpoint.MD | AlignItems.STRETCH  || "align-items-md-stretch"

        Breakpoint.LG | AlignItems.START    || "align-items-lg-start"
        Breakpoint.LG | AlignItems.CENTER   || "align-items-lg-center"
        Breakpoint.LG | AlignItems.END      || "align-items-lg-end"
        Breakpoint.LG | AlignItems.BASELINE || "align-items-lg-baseline"
        Breakpoint.LG | AlignItems.STRETCH  || "align-items-lg-stretch"

        Breakpoint.XL | AlignItems.START    || "align-items-xl-start"
        Breakpoint.XL | AlignItems.CENTER   || "align-items-xl-center"
        Breakpoint.XL | AlignItems.END      || "align-items-xl-end"
        Breakpoint.XL | AlignItems.BASELINE || "align-items-xl-baseline"
        Breakpoint.XL | AlignItems.STRETCH  || "align-items-xl-stretch"
    }

    def "convert justify content to style"() {
        expect:
        toStyle(breakpoint, justifyContent) == className

        where:
        breakpoint    | justifyContent         || className
        Breakpoint.XS | JustifyContent.START   || "justify-content-start"
        Breakpoint.XS | JustifyContent.CENTER  || "justify-content-center"
        Breakpoint.XS | JustifyContent.END     || "justify-content-end"
        Breakpoint.XS | JustifyContent.AROUND  || "justify-content-around"
        Breakpoint.XS | JustifyContent.BETWEEN || "justify-content-between"

        Breakpoint.SM | JustifyContent.START   || "justify-content-sm-start"
        Breakpoint.SM | JustifyContent.CENTER  || "justify-content-sm-center"
        Breakpoint.SM | JustifyContent.END     || "justify-content-sm-end"
        Breakpoint.SM | JustifyContent.AROUND  || "justify-content-sm-around"
        Breakpoint.SM | JustifyContent.BETWEEN || "justify-content-sm-between"

        Breakpoint.MD | JustifyContent.START   || "justify-content-md-start"
        Breakpoint.MD | JustifyContent.CENTER  || "justify-content-md-center"
        Breakpoint.MD | JustifyContent.END     || "justify-content-md-end"
        Breakpoint.MD | JustifyContent.AROUND  || "justify-content-md-around"
        Breakpoint.MD | JustifyContent.BETWEEN || "justify-content-md-between"

        Breakpoint.LG | JustifyContent.START   || "justify-content-lg-start"
        Breakpoint.LG | JustifyContent.CENTER  || "justify-content-lg-center"
        Breakpoint.LG | JustifyContent.END     || "justify-content-lg-end"
        Breakpoint.LG | JustifyContent.AROUND  || "justify-content-lg-around"
        Breakpoint.LG | JustifyContent.BETWEEN || "justify-content-lg-between"

        Breakpoint.XL | JustifyContent.START   || "justify-content-xl-start"
        Breakpoint.XL | JustifyContent.CENTER  || "justify-content-xl-center"
        Breakpoint.XL | JustifyContent.END     || "justify-content-xl-end"
        Breakpoint.XL | JustifyContent.AROUND  || "justify-content-xl-around"
        Breakpoint.XL | JustifyContent.BETWEEN || "justify-content-xl-between"
    }

    def "convert columns to style"() {
        expect:
        toStyle(breakpoint, columns) == className

        where:
        breakpoint    | columns                    || className
        Breakpoint.XS | ColumnsValue.DEFAULT       || "col"
        Breakpoint.XS | ColumnsValue.AUTO          || "col-auto"
        Breakpoint.XS | ColumnsValue.columns(3)    || "col-3"
        Breakpoint.XS | ColumnsValue.columns(null) || "col"

        Breakpoint.SM | ColumnsValue.DEFAULT       || "col-sm"
        Breakpoint.SM | ColumnsValue.AUTO          || "col-sm-auto"
        Breakpoint.SM | ColumnsValue.columns(3)    || "col-sm-3"
        Breakpoint.SM | ColumnsValue.columns(null) || "col-sm"

        Breakpoint.MD | ColumnsValue.DEFAULT       || "col-md"
        Breakpoint.MD | ColumnsValue.AUTO          || "col-md-auto"
        Breakpoint.MD | ColumnsValue.columns(3)    || "col-md-3"
        Breakpoint.MD | ColumnsValue.columns(null) || "col-md"

        Breakpoint.LG | ColumnsValue.DEFAULT       || "col-lg"
        Breakpoint.LG | ColumnsValue.AUTO          || "col-lg-auto"
        Breakpoint.LG | ColumnsValue.columns(3)    || "col-lg-3"
        Breakpoint.LG | ColumnsValue.columns(null) || "col-lg"

        Breakpoint.XL | ColumnsValue.DEFAULT       || "col-xl"
        Breakpoint.XL | ColumnsValue.AUTO          || "col-xl-auto"
        Breakpoint.XL | ColumnsValue.columns(3)    || "col-xl-3"
        Breakpoint.XL | ColumnsValue.columns(null) || "col-xl"
    }

    def "convert align self to style"() {
        expect:
        toStyle(breakpoint, alignSelf) == className

        where:
        breakpoint    | alignSelf          || className
        Breakpoint.XS | AlignSelf.AUTO     || "align-self-auto"
        Breakpoint.XS | AlignSelf.START    || "align-self-start"
        Breakpoint.XS | AlignSelf.CENTER   || "align-self-center"
        Breakpoint.XS | AlignSelf.END      || "align-self-end"
        Breakpoint.XS | AlignSelf.BASELINE || "align-self-baseline"
        Breakpoint.XS | AlignSelf.STRETCH  || "align-self-stretch"

        Breakpoint.SM | AlignSelf.AUTO     || "align-self-sm-auto"
        Breakpoint.SM | AlignSelf.START    || "align-self-sm-start"
        Breakpoint.SM | AlignSelf.CENTER   || "align-self-sm-center"
        Breakpoint.SM | AlignSelf.END      || "align-self-sm-end"
        Breakpoint.SM | AlignSelf.BASELINE || "align-self-sm-baseline"
        Breakpoint.SM | AlignSelf.STRETCH  || "align-self-sm-stretch"

        Breakpoint.MD | AlignSelf.AUTO     || "align-self-md-auto"
        Breakpoint.MD | AlignSelf.START    || "align-self-md-start"
        Breakpoint.MD | AlignSelf.CENTER   || "align-self-md-center"
        Breakpoint.MD | AlignSelf.END      || "align-self-md-end"
        Breakpoint.MD | AlignSelf.BASELINE || "align-self-md-baseline"
        Breakpoint.MD | AlignSelf.STRETCH  || "align-self-md-stretch"

        Breakpoint.LG | AlignSelf.AUTO     || "align-self-lg-auto"
        Breakpoint.LG | AlignSelf.START    || "align-self-lg-start"
        Breakpoint.LG | AlignSelf.CENTER   || "align-self-lg-center"
        Breakpoint.LG | AlignSelf.END      || "align-self-lg-end"
        Breakpoint.LG | AlignSelf.BASELINE || "align-self-lg-baseline"
        Breakpoint.LG | AlignSelf.STRETCH  || "align-self-lg-stretch"

        Breakpoint.XL | AlignSelf.AUTO     || "align-self-xl-auto"
        Breakpoint.XL | AlignSelf.START    || "align-self-xl-start"
        Breakpoint.XL | AlignSelf.CENTER   || "align-self-xl-center"
        Breakpoint.XL | AlignSelf.END      || "align-self-xl-end"
        Breakpoint.XL | AlignSelf.BASELINE || "align-self-xl-baseline"
        Breakpoint.XL | AlignSelf.STRETCH  || "align-self-xl-stretch"
    }

    def "convert order to style"() {
        expect:
        toStyle(breakpoint, order) == className

        where:
        breakpoint    | order                 || className
        Breakpoint.XS | OrderValue.columns(1) || "order-1"
        Breakpoint.XS | OrderValue.FIRST      || "order-first"
        Breakpoint.XS | OrderValue.LAST       || "order-last"

        Breakpoint.SM | OrderValue.columns(1) || "order-sm-1"
        Breakpoint.SM | OrderValue.FIRST      || "order-sm-first"
        Breakpoint.SM | OrderValue.LAST       || "order-sm-last"

        Breakpoint.MD | OrderValue.columns(1) || "order-md-1"
        Breakpoint.MD | OrderValue.FIRST      || "order-md-first"
        Breakpoint.MD | OrderValue.LAST       || "order-md-last"

        Breakpoint.LG | OrderValue.columns(1) || "order-lg-1"
        Breakpoint.LG | OrderValue.FIRST      || "order-lg-first"
        Breakpoint.LG | OrderValue.LAST       || "order-lg-last"

        Breakpoint.XL | OrderValue.columns(1) || "order-xl-1"
        Breakpoint.XL | OrderValue.FIRST      || "order-xl-first"
        Breakpoint.XL | OrderValue.LAST       || "order-xl-last"
    }

    def "convert offset to style"() {
        expect:
        toStyle(breakpoint, offset) == className

        where:
        breakpoint    | offset                 || className
        Breakpoint.XS | OffsetValue.columns(1) || "offset-1"
        Breakpoint.SM | OffsetValue.columns(1) || "offset-sm-1"
        Breakpoint.MD | OffsetValue.columns(1) || "offset-md-1"
        Breakpoint.LG | OffsetValue.columns(1) || "offset-lg-1"
        Breakpoint.XL | OffsetValue.columns(1) || "offset-xl-1"
    }

    def "serialize grid column"() {
        JmixResponsiveGridLayout grid = new JmixResponsiveGridLayout()
        Column column = new Column(grid)

        column.setColumns(Breakpoint.SM, ColumnsValue.DEFAULT)
        column.setOrder(Breakpoint.MD, OrderValue.LAST)
        column.setOffset(Breakpoint.LG, OffsetValue.columns(1))
        column.setAlignSelf(AlignSelf.CENTER)

        def customStyleName = "custom-stylename"
        column.setStyleName(customStyleName)

        when:
        def jsonObject = serialize(column)
        def styles = jsonObject.get(STYLE_PROPERTY)
                .asString
                .split(STYLE_SEPARATOR)

        then:
        verifyAll(styles) {
            size() == 5
            contains(customStyleName)
            contains("col-sm")
            contains("order-md-last")
            contains("offset-lg-1")
            contains("align-self-center")
        }
    }

    def "serialize grid column with no columns value"() {
        JmixResponsiveGridLayout grid = new JmixResponsiveGridLayout()
        Column column = new Column(grid)

        when:
        def jsonObject = serialize(column)

        then:
        jsonObject.get(STYLE_PROPERTY).asString == "col"
    }

    def "serialize grid row"() {
        JmixResponsiveGridLayout grid = new JmixResponsiveGridLayout()
        Row row = new Row(grid)
        row.setHeight("100px")

        row.setRowColumns(Breakpoint.SM, RowColumnsValue.columns(2))
        row.setAlignItems(Breakpoint.MD, AlignItems.BASELINE)
        row.setJustifyContent(JustifyContent.CENTER)

        def customStyleName = "custom-stylename"
        row.setStyleName(customStyleName)

        row.addColumn()
        row.addColumn()

        when:
        def jsonObject = serialize(row)
        def styles = jsonObject.get(STYLE_PROPERTY)
                .asString
                .split(STYLE_SEPARATOR)

        then:
        verifyAll(styles) {
            size() == 5
            contains(customStyleName)
            contains("row")
            contains("row-cols-sm-2")
            contains("align-items-md-baseline")
            contains("justify-content-center")
        }

        and:
        jsonObject.get("height").asString == "100.0px"

        and:
        jsonObject.get("cols").asJsonArray.size() == 2
    }

    def "serialize grid"() {
        JmixResponsiveGridLayout grid = new JmixResponsiveGridLayout()

        grid.setContainerType(ContainerType.FIXED)

        grid.addRow()
        grid.addRow()
        grid.addRow()

        when:
        def jsonObject = serialize(grid)

        then:
        jsonObject.get(STYLE_PROPERTY).asString == "container"

        and:
        jsonObject.get("rows").asJsonArray.size() == 3
    }
}
