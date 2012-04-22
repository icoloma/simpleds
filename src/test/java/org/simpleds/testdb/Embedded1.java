package org.simpleds.testdb;

import org.simpleds.annotations.Embedded;

public class Embedded1 {

	private String embeddedName;
	
	private int int1;
	
	@Embedded
	private Embedded2 embedded2;

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	public Embedded2 getEmbedded2() {
		return embedded2;
	}
	
}
