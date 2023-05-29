/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.structure;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings({"UnusedDeclaration"})
public class BandData implements Serializable {
    public static final String ROOT_BAND_NAME = "Root";

    protected Map<String, Object> data = new HashMap<>();
    protected BandData parentBand;

    protected Map<String, List<BandData>> childrenBands = new LinkedHashMap<>();

    protected final String name;
    protected final BandOrientation orientation;
    protected Set<String> firstLevelBandDefinitionNames = null;
    protected int level;
    protected Map<String, ReportFieldFormat> reportFieldFormats = new HashMap<>();


    public BandData(String name) {
        this(name, null, BandOrientation.HORIZONTAL);
    }

    public BandData(String name, BandData parentBand) {
        this(name, parentBand, BandOrientation.HORIZONTAL);
    }

    public BandData(String name, BandData parentBand, BandOrientation orientation) {
        this.name = name;
        this.parentBand = parentBand;
        this.orientation = orientation;

        BandData currentBand = this;
        while (currentBand != null) {
            level++;
            currentBand = currentBand.parentBand;
        }
    }

    public Map<String, List<BandData>> getChildrenBands() {
        return childrenBands;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String name, Object value) {
        data.put(name, value);
    }

    public Object getParameterValue(String name) {
        return data.get(name);
    }

    //added for back compatibility
    public Object getParameter(String name) {
        return getParameterValue(name);
    }

    public void addAllParameters(Map<String, Object> parameters) {
        data.putAll(parameters);
    }

    public String getName() {
        return name;
    }

    public BandData getParentBand() {
        return parentBand;
    }

    public void setParentBand(BandData parentBand) {
        this.parentBand = parentBand;
    }

    public BandOrientation getOrientation() {
        return orientation;
    }

    public int getLevel() {
        return level;
    }

    public String getFullName() {
        String fullName = name;
        BandData upBand = parentBand;
        while ((upBand != null) && (upBand.level > 1)) {
            fullName = upBand.getName() + "." + fullName;
            upBand = upBand.parentBand;
        }
        return fullName;
    }

    public List<BandData> getChildrenList() {
        List<BandData> bandList = new ArrayList<>();
        for (List<BandData> bands : childrenBands.values()) {
            bandList.addAll(bands);
        }

        return bandList;
    }

    public List<BandData> getChildrenByName(String bandName) {
        if (bandName == null) {
            throw new NullPointerException("Parameter bandName can not be null.");
        }

        List<BandData> children = childrenBands.get(bandName);
        return children != null ? children : new ArrayList<>();
    }

    public BandData getChildByName(String bandName) {
        if (bandName == null) {
            throw new NullPointerException("Parameter bandName can not be null.");
        }

        List<BandData> childrenByName = getChildrenByName(bandName);
        return childrenByName.isEmpty() ? null : childrenByName.get(0);
    }

    public void addChild(BandData band) {
        if (!childrenBands.containsKey(band.getName())) {
            childrenBands.put(band.getName(), new ArrayList<>());
        }
        List<BandData> bands = childrenBands.get(band.getName());
        bands.add(band);
    }

    public void addChildren(List<BandData> bands) {
        for (BandData band : bands)
            addChild(band);
    }

    public boolean visit(BandVisitor bandVisitor) {
        if (bandVisitor.visit(this)) {
            return true;
        }

        for (BandData child : getChildrenList()) {
            if (child.visit(bandVisitor)) {
                return true;
            }
        }

        return false;
    }

    public BandData findBandRecursively(String name) {
        BandNameVisitor visitor = new BandNameVisitor(name);
        visit(visitor);

        return visitor.foundBand;
    }

    public List<BandData> findBandsRecursively(String name) {
        BandData firstBand = findBandRecursively(name);
        if (firstBand == null) {
            return Collections.emptyList();
        }

        List<BandData> allBands = firstBand.getParentBand() != null ?
                firstBand.getParentBand().getChildrenByName(name) :
                Collections.singletonList(firstBand);
        return allBands;
    }

    public Set<String> getFirstLevelBandDefinitionNames() {
        return firstLevelBandDefinitionNames;
    }

    public void setFirstLevelBandDefinitionNames(Set<String> firstLevelBandDefinitionNames) {
        this.firstLevelBandDefinitionNames = firstLevelBandDefinitionNames;
    }

    public void addReportFieldFormats(List<ReportFieldFormat> reportFieldFormats) {
        for (ReportFieldFormat reportFieldFormat : reportFieldFormats) {
            this.reportFieldFormats.put(reportFieldFormat.getName(), reportFieldFormat);
        }
    }

    public Map<String, ReportFieldFormat> getReportFieldFormats() {
        return reportFieldFormats;
    }

    @Override
    public String toString() {
        StringBuilder sbf = new StringBuilder();
        sbf.append(name).append(":").append(data.toString()).append("\n");
        for (BandData band : getChildrenList()) {
            for (int i = 0; i < level; i++)
                sbf.append("\t");
            sbf.append(band.toString());
        }
        return sbf.toString();
    }

    protected static class BandNameVisitor implements BandVisitor {
        protected String name;
        protected BandData foundBand;

        public BandNameVisitor(String name) {
            if (name == null) {
                throw new NullPointerException("Could not find band with name = null");
            }
            this.name = name;
        }

        @Override
        public boolean visit(BandData band) {
            boolean found = band.getName().equals(name);
            if (found) {
                foundBand = band;
            }

            return found;
        }
    }
}