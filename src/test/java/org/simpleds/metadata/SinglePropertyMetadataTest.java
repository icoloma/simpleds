package org.simpleds.metadata;

import org.junit.Before;
import org.junit.Test;
import org.simpleds.AbstractEntityManagerTest;
import org.simpleds.testdb.Attrs;
import org.simpleds.testdb.Dummy1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SinglePropertyMetadataTest extends AbstractEntityManagerTest {

    @Test
    public void testSetValue() {
        ClassMetadata cm = repository.get(Dummy1.class);
        SinglePropertyMetadata<String, String> metadata = (SinglePropertyMetadata<String, String>) cm.<String, String>getProperty(Attrs.NAME);

        // ok
        Dummy1 d = new Dummy1();
        metadata.setValue(d, "foo");
        assertEquals("foo", d.getName());

        // CCE with setter
        try {
            metadata.setValue(d, 1);
            fail("CCE not detected");
        } catch (Exception e) {
            assertEquals("Cannot invoke Dummy1.setName((Integer) 1)", e.getMessage());
        }

        // CCE with field
        try {
            SinglePropertyMetadata<Dummy1.EnumValues, String> evalue = (SinglePropertyMetadata<Dummy1.EnumValues, String>) cm.<Dummy1.EnumValues, String>getProperty(Attrs.E_VALUE);
            evalue.setValue(d, 1);
            fail("CCE not detected");
        } catch (Exception e) {
            assertEquals("Cannot assign Dummy1.evalue = (Integer) 1", e.getMessage());
        }

    }

}
