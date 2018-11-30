/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.FieldTransformations;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
import org.neo4j.ogm.metadata.reflect.EntityFactory;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.session.EntityInstantiator;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple graph-to-entity mapper suitable for ad-hoc, one-off mappings.  This doesn't interact with a
 * mapping context or mandate graph IDs on the target types and is not designed for use in the OGM session.
 *
 * @author Adam George
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class SingleUseEntityMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleUseEntityMapper.class);

    private final EntityFactory entityFactory;
    private final MetaData metadata;

    /**
     * Compatibility constructor for SDN 5.0
     *
     * @param mappingMetaData The {@link MetaData} to use for performing mappings
     * @param entityFactory   The entity factory to use.
     */
    public SingleUseEntityMapper(MetaData mappingMetaData, EntityFactory entityFactory) {
        this.metadata = mappingMetaData;
        this.entityFactory = new EntityFactory(mappingMetaData);
    }

    /**
     * Constructs a new {@link SingleUseEntityMapper} based on the given mapping {@link MetaData}.
     *
     * @param mappingMetaData    The {@link MetaData} to use for performing mappings
     * @param entityInstantiator The entity factory to use.
     */
    public SingleUseEntityMapper(MetaData mappingMetaData, EntityInstantiator entityInstantiator) {
        this.metadata = mappingMetaData;
        this.entityFactory = new EntityFactory(mappingMetaData, entityInstantiator);
    }

    /**
     * Maps a row-based result onto a new instance of the specified type.
     *
     * @param <T>         The class of object to return
     * @param type        The {@link Class} denoting the type of object to create
     * @param columnNames The names of the columns in each row of the result
     * @param rowModel    The {@link org.neo4j.ogm.model.RowModel} containing the data to map
     * @return A new instance of <tt>T</tt> populated with the data in the specified row model
     */
    public <T> T map(Class<T> type, String[] columnNames, RowModel rowModel) {

        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < rowModel.getValues().length; i++) {
            properties.put(columnNames[i], rowModel.getValues()[i]);
        }

        T entity = this.entityFactory.newObject(type, properties);
        return populatePropertiesOfEntity(entity, properties);
    }

    public <T> T map(Class<T> type, Map<String, Object> row) {
        T entity = this.entityFactory.newObject(type, row);
        return populatePropertiesOfEntity(entity, row);
    }

    private <T> T populatePropertiesOfEntity(T entity, Map<String, Object> propertyMap) {

        Class entityClass = entity.getClass();
        if (this.entityFactory.needsFurtherPopulation(entityClass, entity)) {
            ClassInfo entityClassInfo = resolveClassInfoFor(entity.getClass());

            propertyMap.entrySet()
                .forEach(entry -> writeProperty(entityClassInfo, entity, entry.getKey(), entry.getValue()));
        }
        return entity;
    }

    private ClassInfo resolveClassInfoFor(Class<?> type) {
        ClassInfo classInfo = this.metadata.classInfo(type.getSimpleName());
        if (classInfo != null) {
            return classInfo;
        }
        throw new MappingException("Cannot map query result to a class not known by Neo4j-OGM.");
    }

    private void writeProperty(ClassInfo classInfo, Object instance, String propertyName, Object propertyValue) {

        FieldInfo targetFieldInfo = classInfo.getFieldInfo(propertyName);

        if (targetFieldInfo == null) {
            targetFieldInfo = classInfo.relationshipFieldByName(propertyName);
        }

        // When mapping query results to objects that are not domain entities, there's no concept of a GraphID
        if (targetFieldInfo == null && "id".equals(propertyName)) {
            targetFieldInfo = classInfo.identityField();
        }

        if (targetFieldInfo == null) {
            LOGGER.debug("Unable to find property: {} on class: {} for writing", propertyName, classInfo.name());
        } else {
            Object value = FieldTransformations.mergeAndCoercePossibleArray(targetFieldInfo, propertyValue);
            targetFieldInfo.write(instance, value);
        }
    }
}
