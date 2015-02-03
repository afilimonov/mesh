package com.gentics.cailun.core.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.gentics.cailun.core.rest.model.GenericNode;
import com.gentics.cailun.core.rest.model.auth.AbstractPermissionRelationship;
import com.gentics.cailun.core.rest.model.auth.User;

public interface UserRepository extends GraphRepository<User> {

	User findByFirstnameEquals(String firstname);

	User findByUsername(String username);

	@Query("MATCH (u:_User) WHERE u.username + '%' + u.emailAddress + '%' +  u.passwordHash = {0} return u")
	User findByPrincipalId(String principalId);

	@Query("start generic_node=node({1}) MATCH (generic_node)<-[p:HAS_PERMISSIONSET]-(role:Role)-[:HAS_ROLE]->(group:Group)<-[:MEMBER_OF]-(user:_User) where user.username + '%' + user.emailAddress + '%' +  user.passwordHash = {0} return p")
	List<AbstractPermissionRelationship> findPermissionSetsForUserAndContent(String principalId, GenericNode node);
}
