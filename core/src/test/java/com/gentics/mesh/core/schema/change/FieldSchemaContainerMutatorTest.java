package com.gentics.mesh.core.schema.change;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.core.data.schema.SchemaContainerVersion;
import com.gentics.mesh.core.data.schema.UpdateFieldChange;
import com.gentics.mesh.core.data.schema.handler.FieldSchemaContainerMutator;
import com.gentics.mesh.core.data.schema.impl.SchemaContainerVersionImpl;
import com.gentics.mesh.core.data.schema.impl.UpdateFieldChangeImpl;
import com.gentics.mesh.core.rest.schema.BinaryFieldSchema;
import com.gentics.mesh.core.rest.schema.BooleanFieldSchema;
import com.gentics.mesh.core.rest.schema.DateFieldSchema;
import com.gentics.mesh.core.rest.schema.HtmlFieldSchema;
import com.gentics.mesh.core.rest.schema.ListFieldSchema;
import com.gentics.mesh.core.rest.schema.MicronodeFieldSchema;
import com.gentics.mesh.core.rest.schema.NodeFieldSchema;
import com.gentics.mesh.core.rest.schema.NumberFieldSchema;
import com.gentics.mesh.core.rest.schema.Schema;
import com.gentics.mesh.core.rest.schema.StringFieldSchema;
import com.gentics.mesh.core.rest.schema.change.impl.SchemaChangeModel;
import com.gentics.mesh.core.rest.schema.impl.BinaryFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.BooleanFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.DateFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.HtmlFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.ListFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.MicronodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NodeFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.NumberFieldSchemaImpl;
import com.gentics.mesh.core.rest.schema.impl.SchemaModel;
import com.gentics.mesh.core.rest.schema.impl.StringFieldSchemaImpl;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.test.AbstractEmptyDBTest;

/**
 * Test for common mutator operations on a field containers.
 */
public class FieldSchemaContainerMutatorTest extends AbstractEmptyDBTest {

	@Autowired
	private FieldSchemaContainerMutator mutator;

	@Test
	public void testNullOperation() {
		SchemaContainerVersion version = Database.getThreadLocalGraph().addFramedVertex(SchemaContainerVersionImpl.class);
		Schema schema = new SchemaModel();
		version.setSchema(schema);
		Schema updatedSchema = mutator.apply(version);
		assertNotNull(updatedSchema);
		assertEquals("No changes were specified. No modification should happen.", schema, updatedSchema);
	}

	@Test
	public void testAUpdateFields() {

		SchemaContainerVersion version = Database.getThreadLocalGraph().addFramedVertex(SchemaContainerVersionImpl.class);

		// 1. Create schema
		Schema schema = new SchemaModel("testschema");

		BinaryFieldSchema binaryField = new BinaryFieldSchemaImpl();
		binaryField.setName("binaryField");
		binaryField.setAllowedMimeTypes("oldTypes");
		binaryField.setRequired(true);
		schema.addField(binaryField);

		StringFieldSchema stringField = new StringFieldSchemaImpl();
		stringField.setAllowedValues("blub");
		stringField.setName("stringField");
		stringField.setRequired(true);
		schema.addField(stringField);

		NodeFieldSchema nodeField = new NodeFieldSchemaImpl();
		nodeField.setAllowedSchemas("blub");
		nodeField.setName("nodeField");
		nodeField.setRequired(true);
		schema.addField(nodeField);

		MicronodeFieldSchema micronodeField = new MicronodeFieldSchemaImpl();
		micronodeField.setAllowedMicroSchemas("blub");
		micronodeField.setName("micronodeField");
		micronodeField.setRequired(true);
		schema.addField(micronodeField);

		NumberFieldSchema numberField = new NumberFieldSchemaImpl();
		numberField.setName("numberField");
		numberField.setRequired(true);
		schema.addField(numberField);

		HtmlFieldSchema htmlField = new HtmlFieldSchemaImpl();
		htmlField.setName("htmlField");
		htmlField.setRequired(true);
		schema.addField(htmlField);

		BooleanFieldSchema booleanField = new BooleanFieldSchemaImpl();
		booleanField.setName("booleanField");
		booleanField.setRequired(true);
		schema.addField(booleanField);

		DateFieldSchema dateField = new DateFieldSchemaImpl();
		dateField.setName("dateField");
		dateField.setRequired(true);
		schema.addField(dateField);

		ListFieldSchema listField = new ListFieldSchemaImpl();
		listField.setName("listField");
		listField.setRequired(true);
		schema.addField(listField);

		version.setSchema(schema);

		// 2. Create schema field update change
		UpdateFieldChange binaryFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		binaryFieldUpdate.setFieldName("binaryField");
		binaryFieldUpdate.setRestProperty("allowedMimeTypes", new String[] { "newTypes" });
		binaryFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		version.setNextChange(binaryFieldUpdate);

		UpdateFieldChange nodeFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		nodeFieldUpdate.setFieldName("nodeField");
		nodeFieldUpdate.setRestProperty("allowedSchemas", new String[] { "schemaA", "schemaB" });
		nodeFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		binaryFieldUpdate.setNextChange(nodeFieldUpdate);

		UpdateFieldChange stringFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		stringFieldUpdate.setRestProperty("allowedValues", new String[] { "valueA", "valueB" });
		stringFieldUpdate.setFieldName("stringField");
		stringFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		nodeFieldUpdate.setNextChange(stringFieldUpdate);

		UpdateFieldChange htmlFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		htmlFieldUpdate.setFieldName("htmlField");
		htmlFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		stringFieldUpdate.setNextChange(htmlFieldUpdate);

		UpdateFieldChange numberFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		numberFieldUpdate.setFieldName("numberField");
		numberFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		htmlFieldUpdate.setNextChange(numberFieldUpdate);

		UpdateFieldChange dateFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		dateFieldUpdate.setFieldName("dateField");
		dateFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		numberFieldUpdate.setNextChange(dateFieldUpdate);

		UpdateFieldChange booleanFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		booleanFieldUpdate.setFieldName("booleanField");
		booleanFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		dateFieldUpdate.setNextChange(booleanFieldUpdate);

		UpdateFieldChange micronodeFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		micronodeFieldUpdate.setFieldName("micronodeField");
		micronodeFieldUpdate.setRestProperty(SchemaChangeModel.ALLOW_KEY, new String[] { "A", "B", "C" });
		micronodeFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		booleanFieldUpdate.setNextChange(micronodeFieldUpdate);

		UpdateFieldChange listFieldUpdate = Database.getThreadLocalGraph().addFramedVertex(UpdateFieldChangeImpl.class);
		listFieldUpdate.setFieldName("listField");
		listFieldUpdate.setRestProperty(SchemaChangeModel.REQUIRED_KEY, false);
		micronodeFieldUpdate.setNextChange(listFieldUpdate);

		// 3. Apply the changes
		Schema updatedSchema = mutator.apply(version);

		// Binary 
		BinaryFieldSchema binaryFieldSchema = updatedSchema.getField("binaryField", BinaryFieldSchemaImpl.class);
		assertNotNull(binaryFieldSchema);
		assertArrayEquals(new String[] { "newTypes" }, binaryFieldSchema.getAllowedMimeTypes());
		assertFalse("The required flag should now be set to false.", binaryFieldSchema.isRequired());

		// Node
		NodeFieldSchema nodeFieldSchema = updatedSchema.getField("nodeField", NodeFieldSchemaImpl.class);
		assertNotNull(nodeFieldSchema);
		assertArrayEquals(new String[] { "schemaA", "schemaB" }, nodeFieldSchema.getAllowedSchemas());
		assertFalse("The required flag should now be set to false.", nodeFieldSchema.isRequired());

		// Microschema
		MicronodeFieldSchema micronodeFieldSchema = updatedSchema.getField("micronodeField", MicronodeFieldSchemaImpl.class);
		assertNotNull(micronodeFieldSchema);
		assertArrayEquals(new String[] { "A", "B", "C" }, micronodeFieldSchema.getAllowedMicroSchemas());
		assertFalse("The required flag should now be set to false.", micronodeFieldSchema.isRequired());

		// String
		StringFieldSchema stringFieldSchema = updatedSchema.getField("stringField", StringFieldSchemaImpl.class);
		assertNotNull(stringFieldSchema);
		assertArrayEquals(new String[] { "valueA", "valueB" }, stringFieldSchema.getAllowedValues());
		assertFalse("The required flag should now be set to false.", stringFieldSchema.isRequired());

		// Html
		HtmlFieldSchema htmlFieldSchema = updatedSchema.getField("htmlField", HtmlFieldSchemaImpl.class);
		assertNotNull(htmlFieldSchema);
		assertFalse("The required flag should now be set to false.", htmlFieldSchema.isRequired());

		// Boolean
		BooleanFieldSchema booleanFieldSchema = updatedSchema.getField("booleanField", BooleanFieldSchemaImpl.class);
		assertFalse("The required flag should now be set to false.", booleanFieldSchema.isRequired());

		// Date
		DateFieldSchema dateFieldSchema = updatedSchema.getField("dateField", DateFieldSchemaImpl.class);
		assertFalse("The required flag should now be set to false.", dateFieldSchema.isRequired());

		// Number
		NumberFieldSchema numberFieldSchema = updatedSchema.getField("numberField", NumberFieldSchemaImpl.class);
		assertFalse("The required flag should now be set to false.", numberFieldSchema.isRequired());

		// List
		ListFieldSchema listFieldSchema = updatedSchema.getField("listField", ListFieldSchemaImpl.class);
		assertFalse("The required flag should now be set to false.", listFieldSchema.isRequired());

	}

}