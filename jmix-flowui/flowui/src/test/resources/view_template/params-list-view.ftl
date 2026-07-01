<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      title="${viewTitle} ${titleSuffix}">
    <data>
        <collection id="entityDc"
                    class="${entityMetaClass.javaClass.name}">
            <fetchPlan extends="_base"/>
            <loader id="entityDl" readOnly="true">
                <query>
                    <![CDATA[select e from ${entityMetaClass.name} e]]>
                </query>
            </loader>
        </collection>
    </data>
    <layout>
        <span id="marker" text="${markerText}"/>
        <dataGrid id="dataGrid"
                  width="100%"
                  dataContainer="entityDc">
            <columns>
                <column property="name"/>
            </columns>
        </dataGrid>
    </layout>
</view>
