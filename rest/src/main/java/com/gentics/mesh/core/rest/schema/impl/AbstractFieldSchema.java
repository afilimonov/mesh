package com.gentics.mesh.core.rest.schema.impl;

import com.gentics.mesh.core.rest.common.response.FieldTypes;
import com.gentics.mesh.core.rest.schema.FieldSchema;

public abstract class AbstractFieldSchema implements FieldSchema {

	private String name;

	private String label;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
