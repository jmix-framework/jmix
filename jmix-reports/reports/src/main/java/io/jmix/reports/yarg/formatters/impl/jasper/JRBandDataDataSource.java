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

package io.jmix.reports.yarg.formatters.impl.jasper;

import io.jmix.reports.yarg.structure.BandData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.*;

/**
 * Provides bypass of BandData tree.
 */
public class JRBandDataDataSource implements JRDataSource {
    protected BandData root;
    protected BandData currentBand;
    protected Iterator<BandData> currentIterator;
    protected Map<BandData, Iterator<BandData>> visitedBands = new HashMap<>();
    protected Set<BandData> readBands = new HashSet<>();

    /**
     * Accepts root element.
     * Goes down one level because root must not have elements.
     *
     * @param root of the tree
     */
    public JRBandDataDataSource(BandData root) {
        this.root = root;
        currentIterator = root.getChildrenList().iterator();
        visitedBands.put(root, currentIterator);
        currentBand = root;
    }

    /**
     * Maintains visitedBands to continue bypass on the same
     * level after return from deeper level of hierarchy.
     * Creates iterator for each level.
     */
    @Override
    public boolean next() throws JRException {
        List<BandData> children = currentBand.getChildrenList();

        if (children != null && !children.isEmpty() && !visitedBands.containsKey(currentBand)) {
            currentIterator = children.iterator();
            visitedBands.put(currentBand, currentIterator);
        } else if (currentIterator == null) {
            currentIterator = Collections.singletonList(currentBand).iterator();
        }

        if (currentIterator.hasNext()) {
            currentBand = currentIterator.next();
            if (readBands.contains(currentBand) || currentBand.getData().isEmpty())
                return next();

            return true;
        } else {
            BandData parentBand = currentBand.getParentBand();
            currentBand = parentBand;
            currentIterator = visitedBands.get(parentBand);

            if (parentBand == null || parentBand.equals(root))
                return false;

            return next();
        }
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        Object value = null;

        if (currentBand != null && currentBand.getData() != null) {
            value = currentBand.getData().get(jrField.getName());
        }

        readBands.add(currentBand);
        return value;
    }

    /**
     * Search for first level children band with specified name
     * and return new datasource with this band as root element.
     */
    public JRBandDataDataSource subDataSource(String bandName) {
        if (containsVisitedBand(bandName))
            return null;

        BandData newParentBand = createNewBand(bandName);

        currentBand = root;
        currentIterator = root.getChildrenList().iterator();
        visitedBands.put(root, currentIterator);

        return new JRBandDataDataSource(newParentBand);
    }

    protected BandData createNewBand(String bandName) {
        BandData newParentBand = new BandData(bandName);
        List<BandData> childrenList = root.getChildrenByName(bandName);
        root.getChildrenBands().remove(bandName);
        newParentBand.addChildren(childrenList);
        childrenList.forEach(childBand -> childBand.setParentBand(newParentBand));

        visitedBands.put(newParentBand, newParentBand.getChildrenList().iterator());
        return newParentBand;
    }

    protected boolean containsVisitedBand(String bandName) {
        for (Map.Entry<BandData, Iterator<BandData>> entry : visitedBands.entrySet()) {
            if (entry.getKey().getName().equals(bandName))
                return true;
        }

        return false;
    }
}
