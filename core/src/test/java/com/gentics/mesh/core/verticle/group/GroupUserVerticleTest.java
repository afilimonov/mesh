package com.gentics.mesh.core.verticle.group;

import static com.gentics.mesh.core.data.model.relationship.Permission.DELETE_PERM;
import static com.gentics.mesh.core.data.model.relationship.Permission.READ_PERM;
import static com.gentics.mesh.core.data.model.relationship.Permission.UPDATE_PERM;
import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.POST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.core.AbstractRestVerticle;
import com.gentics.mesh.core.data.model.root.UserRoot;
import com.gentics.mesh.core.data.model.tinkerpop.Group;
import com.gentics.mesh.core.data.model.tinkerpop.MeshUser;
import com.gentics.mesh.core.data.service.GroupService;
import com.gentics.mesh.core.data.service.MeshUserService;
import com.gentics.mesh.core.rest.group.response.GroupResponse;
import com.gentics.mesh.core.rest.user.response.UserListResponse;
import com.gentics.mesh.core.rest.user.response.UserResponse;
import com.gentics.mesh.core.verticle.GroupVerticle;
import com.gentics.mesh.test.AbstractRestVerticleTest;
import com.gentics.mesh.util.DataHelper;
import com.gentics.mesh.util.JsonUtils;

public class GroupUserVerticleTest

extends AbstractRestVerticleTest {

	@Autowired
	private GroupVerticle groupsVerticle;

	@Autowired
	private GroupService groupService;

	@Autowired
	private MeshUserService userService;

	@Autowired
	private DataHelper helper;

	@Override
	public AbstractRestVerticle getVerticle() {
		return groupsVerticle;
	}

	// Group User Testcases - PUT / Add

	@Test
	public void testGetUsersByGroup() throws Exception {
		UserRoot userRoot = data().getMeshRoot().getUserRoot();
		MeshUser extraUser = userRoot.create("extraUser");
		info.getGroup().addUser(extraUser);
		info.getRole().addPermissions(extraUser, READ_PERM);

		String uuid = info.getGroup().getUuid();
		String response = request(info, GET, "/api/v1/groups/" + uuid + "/users", 200, "OK");
		UserListResponse userList = JsonUtils.readValue(response, UserListResponse.class);
		assertEquals(2, userList.getMetainfo().getTotalCount());
		assertEquals(2, userList.getData().size());
		Iterator<UserResponse> userIt = userList.getData().iterator();
		UserResponse userB = userIt.next();
		UserResponse userA = userIt.next();
		assertEquals(info.getUser().getUuid(), userA.getUuid());
		assertEquals(extraUser.getUuid(), userB.getUuid());
	}

	@Test
	public void testAddUserToGroupWithBogusGroupId() throws Exception {
		UserRoot userRoot = data().getMeshRoot().getUserRoot();
		MeshUser extraUser = helper.addUser(userRoot, "extraUser", info.getRole(), READ_PERM);
		String response = request(info, POST, "/api/v1/groups/bogus/users/" + extraUser.getUuid(), 404, "Not Found");
		expectMessageResponse("object_not_found_for_uuid", response, "bogus");

	}

	@Test
	public void testAddUserToGroupWithPerm() throws Exception {
		Group group = info.getGroup();
		UserRoot userRoot = data().getMeshRoot().getUserRoot();
		MeshUser extraUser = helper.addUser(userRoot, "extraUser", info.getRole(), READ_PERM);
		String response = request(info, POST, "/api/v1/groups/" + group.getUuid() + "/users/" + extraUser.getUuid(), 200, "OK");
		GroupResponse restGroup = JsonUtils.readValue(response, GroupResponse.class);
		test.assertGroup(group, restGroup);

		assertTrue("User should be member of the group.", group.hasUser(extraUser));
	}

	@Test
	public void testAddUserToGroupWithoutPermOnGroup() throws Exception {
		Group group = info.getGroup();
		UserRoot userRoot = data().getMeshRoot().getUserRoot();
		MeshUser extraUser = userRoot.create("extraUser");
		info.getRole().addPermissions(extraUser, READ_PERM);
		info.getRole().revokePermissions(group, UPDATE_PERM);

		String response = request(info, POST, "/api/v1/groups/" + group.getUuid() + "/users/" + extraUser.getUuid(), 403, "Forbidden");
		expectMessageResponse("error_missing_perm", response, group.getUuid());

		assertFalse("User should not be member of the group.", group.hasUser(extraUser));
	}

	@Test
	public void testAddUserToGroupWithoutPermOnUser() throws Exception {
		Group group = info.getGroup();
		UserRoot userRoot = data().getMeshRoot().getUserRoot();
		MeshUser extraUser = userRoot.create("extraUser");
		info.getRole().addPermissions(extraUser, DELETE_PERM);

		String response = request(info, POST, "/api/v1/groups/" + group.getUuid() + "/users/" + extraUser.getUuid(), 403, "Forbidden");
		expectMessageResponse("error_missing_perm", response, extraUser.getUuid());

		assertFalse("User should not be member of the group.", group.hasUser(extraUser));
	}

	// Group User Testcases - DELETE / Remove
	@Test
	public void testRemoveUserFromGroupWithoutPerm() throws Exception {
		MeshUser user = info.getUser();
		Group group = info.getGroup();

		info.getRole().revokePermissions(group, UPDATE_PERM);

		String response = request(info, DELETE, "/api/v1/groups/" + group.getUuid() + "/users/" + user.getUuid(), 403, "Forbidden");
		expectMessageResponse("error_missing_perm", response, group.getUuid());

		assertTrue("User should still be a member of the group.", group.hasUser(user));
	}

	@Test
	public void testRemoveUserFromGroupWithPerm() throws Exception {
		MeshUser user = info.getUser();
		Group group = info.getGroup();

		String response = request(info, DELETE, "/api/v1/groups/" + group.getUuid() + "/users/" + user.getUuid(), 200, "OK");
		GroupResponse restGroup = JsonUtils.readValue(response, GroupResponse.class);
		test.assertGroup(group, restGroup);

		assertFalse("User should not be member of the group.", group.hasUser(user));
	}

	@Test
	@Ignore("Not yet implemented")
	public void testRemoveSameUserFromGroupWithPerm() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveUserFromLastGroupWithPerm() throws Exception {
		MeshUser user = info.getUser();
		Group group = info.getGroup();

		String response = request(info, DELETE, "/api/v1/groups/" + group.getUuid() + "/users/" + user.getUuid(), 400, "Bad Request");
		String json = "error-user-last-group";
		assertEqualsSanitizedJson("Response json does not match the expected one.", json, response);
		assertTrue("User should still be member of the group.", group.hasUser(user));
	}

	@Test
	public void testRemoveUserFromGroupWithBogusUserUuid() throws Exception {
		MeshUser user = info.getUser();
		Group group = info.getGroup();

		String response = request(info, DELETE, "/api/v1/groups/" + group.getUuid() + "/users/bogus", 404, "Not Found");
		expectMessageResponse("object_not_found_for_uuid", response, "bogus");

		assertTrue("User should still be member of the group.", group.hasUser(user));
	}
}
