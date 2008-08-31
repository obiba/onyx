package org.obiba.onyx.quartz.core.domain.condition;

import java.io.Serializable;

public abstract class Condition implements Serializable {

	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
