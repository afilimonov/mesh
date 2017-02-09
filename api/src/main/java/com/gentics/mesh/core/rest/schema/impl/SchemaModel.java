package com.gentics.mesh.core.rest.schema.impl;

import static com.gentics.mesh.core.rest.error.Errors.error;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.gentics.mesh.core.rest.schema.BinaryFieldSchema;
import com.gentics.mesh.core.rest.schema.FieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.SchemaReference;
import com.gentics.mesh.core.rest.schema.StringFieldSchema;

/**
 * @see Schema
 */
public class SchemaModel extends AbstractFieldSchemaContainer implements Schema {

	@JsonPropertyDescription("Name of the display field.")
	private String displayField;

	@JsonPropertyDescription("Name of the segment field. This field is used to construct the webroot path to the node.")
	private String segmentField;

	@JsonPropertyDescription("Flag which indicates whether nodes which use this schema store additional child nodes.")
	private boolean container = false;

	/**
	 * Create a new schema with the given name.
	 * 
	 * @param name
	 */
	public SchemaModel(String name) {
		super(name);
	}

	public SchemaModel() {
	}

	@Override
	public String getDisplayField() {
		return displayField;
	}

	@Override
	public void setDisplayField(String displayField) {
		this.displayField = displayField;
	}

	@Override
	public String getSegmentField() {
		return segmentField;
	}

	@Override
	public void setSegmentField(String segmentField) {
		this.segmentField = segmentField;
	}

	@Override
	public boolean isContainer() {
		return container;
	}

	@Override
	public void setContainer(boolean flag) {
		this.container = flag;
	}

	@Override
	public void validate() {
		super.validate();
		// TODO make sure that the display name field only maps to string fields since NodeImpl can currently only deal with string field values for
		// displayNames
		if (!StringUtils.isEmpty(getDisplayField())) {
			if (!getFields().stream().map(FieldSchema::getName).anyMatch(e -> e.equals(getDisplayField()))) {
				throw error(BAD_REQUEST, "schema_error_displayfield_invalid", getDisplayField());
			}

			// TODO maybe we should also allow other field types
			if (!(getField(getDisplayField()) instanceof StringFieldSchema)) {
				throw error(BAD_REQUEST, "schema_error_displayfield_type_invalid", getDisplayField());
			}
		}

		FieldSchema segmentFieldSchema = getField(getSegmentField());
		if (segmentFieldSchema != null
				&& (!((segmentFieldSchema instanceof StringFieldSchema) || (segmentFieldSchema instanceof BinaryFieldSchema)))) {
			throw error(BAD_REQUEST, "schema_error_segmentfield_type_invalid", segmentFieldSchema.getType());
		}

		if (getSegmentField() != null && !getFields().stream().map(FieldSchema::getName).anyMatch(e -> e.equals(getSegmentField()))) {
			throw error(BAD_REQUEST, "schema_error_segmentfield_invalid", getSegmentField());
		}
	}

	@Override
	public SchemaReference toReference() {
		SchemaReference reference = new SchemaReference();
		reference.setUuid(getUuid());
		reference.setVersion(getVersion());
		reference.setName(getName());
		return reference;
	}

}
