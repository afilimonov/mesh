package com.gentics.mesh.core.data.model;

import static com.gentics.mesh.util.RoutingContextHelper.getUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.model.root.TagFamily;
import com.gentics.mesh.core.data.model.tinkerpop.Language;
import com.gentics.mesh.core.data.model.tinkerpop.MeshAuthUser;
import com.gentics.mesh.core.data.model.tinkerpop.MeshNode;
import com.gentics.mesh.core.data.model.tinkerpop.Tag;
import com.gentics.mesh.core.data.service.MeshNodeService;
import com.gentics.mesh.core.data.service.TagService;
import com.gentics.mesh.core.data.service.transformation.TransformationInfo;
import com.gentics.mesh.core.rest.tag.response.TagResponse;
import com.gentics.mesh.paging.PagingInfo;
import com.gentics.mesh.test.AbstractDBTest;
import com.gentics.mesh.util.InvalidArgumentException;
import com.gentics.mesh.util.JsonUtils;

public class TagTest extends AbstractDBTest {

	private static Logger log = LoggerFactory.getLogger(TagTest.class);

	public static final String GERMAN_NAME = "test german name";

	public static final String ENGLISH_NAME = "test english name";

	@Autowired
	private TagService tagService;

	@Autowired
	private MeshNodeService nodeService;

	@Before
	public void setup() throws Exception {
		setupData();
	}

	@Test
	public void testTagCreation() {
		TagFamily root = data().getTagFamily("basic");
		Tag tag = root.create(GERMAN_NAME);
		assertNotNull(tag.getId());
		tag = tagService.findOne(tag.getId());
		assertNotNull("The folder could not be found.", tag);
		String name = tag.getName();
		assertEquals("The loaded name of the folder did not match the expected one.", GERMAN_NAME, name);
	}

	@Test
	public void testSimpleTag() {
		TagFamily root = data().getTagFamily("basic");
		Tag tag = root.create("test");
		assertEquals("test", tag.getName());
		tag.setI18NProperty(data().getEnglish(), "name", "test");
	}

	@Test
	public void testNodes() {

		Language english = data().getEnglish();
		TagFamily root = data().getTagFamily("basic");
		Tag tag = root.create(ENGLISH_NAME);
		tag = tagService.findOne(tag.getId());
		assertNotNull(tag);

		final String GERMAN_TEST_FILENAME = "german.html";
		MeshNode parentNode = data().getFolder("2015");
		MeshNode node = parentNode.create();

		Language german = languageService.findByLanguageTag("de");

		node.setI18NProperty(german, "displayName", GERMAN_TEST_FILENAME);
		node.setI18NProperty(german, "name", "german node name");

		// Assign the tag to the node
		node.addTag(tag);

		// TODO: Reload the tag and check whether the content was set

		assertEquals("The tag should have exactly one node.", 1, tag.getNodes().size());
		MeshNode contentFromTag = tag.getNodes().iterator().next();
		assertNotNull(contentFromTag);
		assertEquals("We did not get the correct content.", node.getId(), contentFromTag.getId());
		String filename = contentFromTag.getI18nProperty(german, "displayName");
		assertEquals("The name of the file from the loaded tag did not match the expected one.", GERMAN_TEST_FILENAME, filename);

		// Remove the file/content and check whether the content was really removed
		tag.removeNode(contentFromTag);
		//TODO verify for removed node
		assertEquals("The tag should not have any file.", 0, tag.getNodes().size());

	}

	@Test
	public void testNodeTagging() {
		final String TEST_TAG_NAME = "testTag";
		TagFamily tagFamily = data().getTagFamily("basic");
		Tag tag = tagFamily.create(TEST_TAG_NAME);

		MeshNode node = data().getFolder("news");
		node.addTag(tag);

		MeshNode reloadedNode = nodeService.findByUUID(node.getUuid());
		boolean found = false;
		for (Tag currentTag : reloadedNode.getTags()) {
			if (currentTag.getUuid().equals(tag.getUuid())) {
				found = true;
			}
		}

		assertTrue("The tag {" + tag.getUuid() + "} was not found within the node tags.", found);
	}

	@Test
	public void testFindAll() throws InvalidArgumentException {
		List<String> languageTags = new ArrayList<>();
		languageTags.add("de");
		RoutingContext rc = getMockedRoutingContext("");
		MeshAuthUser requestUser = getUser(rc);

		Page<? extends Tag> tagPage = tagService.findProjectTags(requestUser, "dummy", languageTags, new PagingInfo(1, 10));
		assertEquals(8, tagPage.getTotalElements());
		assertEquals(10, tagPage.getSize());

		languageTags.add("en");
		tagPage = tagService.findProjectTags(requestUser, "dummy", languageTags, new PagingInfo(1, 14));
		assertEquals(data().getTags().size(), tagPage.getTotalElements());
		assertEquals(14, tagPage.getSize());
	}

	@Test
	public void testTransformToRest() {
		Tag tag = data().getTag("red");
		assertNotNull("The UUID of the tag must not be null.", tag.getUuid());
		List<String> languageTags = new ArrayList<>();
		languageTags.add("en");
		languageTags.add("de");
		int depth = 3;

		RoutingContext rc = getMockedRoutingContext("lang=de,en");
		MeshAuthUser requestUser = getUser(rc);
		for (int i = 0; i < 100; i++) {
			long start = System.currentTimeMillis();
			TransformationInfo info = new TransformationInfo(requestUser, languageTags, rc);
			TagResponse response = tag.transformToRest(info);
			assertNotNull(response);
			long dur = System.currentTimeMillis() - start;
			log.info("Transformation with depth {" + depth + "} took {" + dur + "} [ms]");
			System.out.println(JsonUtils.toJson(response));
		}
		// assertEquals(2, response.getChildTags().size());
		// assertEquals(4, response.getPerms().length);
	}

}
