package org.neo4j.ogm.domain.autoindex;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Frantisek Hartman
 */
@NodeEntity(label = "EntityWithMultipleCompositeIndexes")
@CompositeIndex({ "firstName", "age" })
@CompositeIndex({ "firstName", "email" })
public class MultipleCompositeIndexEntity {

    Long id;

    String firstName;

    int age;

    String email;
}
