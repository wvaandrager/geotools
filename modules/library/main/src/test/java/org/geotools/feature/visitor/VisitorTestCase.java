/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature.visitor;


import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sebastian Graca, ISPiK S.A.
 */
public abstract class VisitorTestCase<T, R> {
    private final Class<T> valueClass;
    private final List<T> values;
    private final R expectedValue;
    private SimpleFeatureType featureType;
    private SimpleFeatureBuilder featureBuilder;
    private MemoryDataStore dataStore;

    protected VisitorTestCase(Class<T> valueClass, List<T> values, R expectedValue) {
        this.valueClass = valueClass;
        this.values = values;
        this.expectedValue = expectedValue;
    }

    protected abstract FeatureCalc createVisitor(int attributeTypeIndex, SimpleFeatureType type);

    @Before
    public void setUp() throws Exception {
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        ftb.setName("test");
        ftb.add("val", valueClass);
        ftb.add("intVal", Integer.class);
        featureType = ftb.buildFeatureType();
        featureBuilder = new SimpleFeatureBuilder(featureType);
        dataStore = new MemoryDataStore(featureType);
    }

    @Test
    public void emptyCollection() throws Exception {
        FeatureCalc calc = createVisitor(0, featureType);
        getFeatureCollection().accepts(calc, null);
        assertNull(calc.getResult().getValue());
    }

    @Test
    public void onlyNulls() throws Exception {
        dataStore.addFeature(featureBuilder.buildFeature("f1", new Object[]{null, 1}));
        dataStore.addFeature(featureBuilder.buildFeature("f2", new Object[]{null, 2}));
        dataStore.addFeature(featureBuilder.buildFeature("f3", new Object[]{null, 3}));

        FeatureCalc calc = createVisitor(0, featureType);
        getFeatureCollection().accepts(calc, null);
        assertNull(calc.getResult().getValue());
    }

    @Test
    public void onlyNotNulls() throws Exception {
        int idx = 1;
        for (T value : values) {
            dataStore.addFeature(featureBuilder.buildFeature("f" + idx, new Object[]{value, idx}));
            ++idx;
        }

        FeatureCalc calc = createVisitor(0, featureType);
        getFeatureCollection().accepts(calc, null);
        Object value = calc.getResult().getValue();
        assertEquals(expectedValue, value);
        assertSame(expectedValue.getClass(), value.getClass());
    }

    @Test
    public void mixed() throws Exception {
        int idx = 1;
        for (T value : values) {
            dataStore.addFeature(featureBuilder.buildFeature("f" + idx, new Object[]{value, idx}));
            ++idx;
            dataStore.addFeature(featureBuilder.buildFeature("f" + idx, new Object[]{null, idx}));
            ++idx;
        }

        FeatureCalc calc = createVisitor(0, featureType);
        getFeatureCollection().accepts(calc, null);
        Object value = calc.getResult().getValue();
        assertEquals(expectedValue, value);
        assertSame(expectedValue.getClass(), value.getClass());
    }

    private SimpleFeatureCollection getFeatureCollection() throws IOException {
        return dataStore.getFeatureSource("test").getFeatures();
    }
}