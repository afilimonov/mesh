package com.gentics.mesh.core.data;

import com.gentics.mesh.core.rest.schema.MicroschemaResponse;

public interface MicroschemaContainer extends GenericVertex<MicroschemaResponse>, NamedVertex {

	public static final String TYPE = "microschema";

}