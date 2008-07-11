package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
public class InstrumentType extends AbstractEntity {

	private static final long serialVersionUID = 23414234L;

	@Column(length = 200)
	@Index(name = "name_index")
	private String name;

	@Column(length = 200)
	@Index(name = "description_index")
	private String description;

	public InstrumentType() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
