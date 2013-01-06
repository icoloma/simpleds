package org.simpleds.testdb;

import org.simpleds.annotations.Embedded;
import org.simpleds.annotations.Property;

public class Embedded1 {

    @Property(Attrs.EMBEDDED_NAME)
	private String embeddedName;

	private int i1;
	
	@Embedded
	private Embedded2 embedded2;

	public int getI1() {
		return i1;
	}

	public void setI1(int i1) {
		this.i1 = i1;
	}

	public Embedded2 getEmbedded2() {
		return embedded2;
	}
	
}
