package org.simpleds.testdb;

import javax.persistence.Embedded;

public class Embedded1 {

	private Integer int1;
	
	@Embedded
	private Embedded2 embedded2;

	public Integer getInt1() {
		return int1;
	}

	public void setInt1(Integer int1) {
		this.int1 = int1;
	}

	public Embedded2 getEmbedded2() {
		return embedded2;
	}
	
}
