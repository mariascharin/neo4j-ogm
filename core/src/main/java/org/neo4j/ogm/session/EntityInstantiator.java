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

package org.neo4j.ogm.session;

import java.util.Map;

/**
 * Interface to be implemented to override entity instances creation.
 * This is mainly designed for SDN, Spring data commons having some infrastructure code to do fancy
 * object instantiation using persistence constructors and ASM low level bytecode generation.
 *
 * @author Nicolas Mervaillie
 * @author Michael J. Simons
 * @since 3.1
 */
public interface EntityInstantiator {

    /**
     * Creates an instance of a given class.
     *
     * @param clazz          The class to materialize.
     * @param propertyValues Properties of the object (needed for constructors with args)
     * @param <T>            Type to create
     * @return The created instance.
     */
    <T> T createInstance(Class<T> clazz, Map<String, Object> propertyValues);

    /**
     * This methods shall return true if an instance of a class instantiated by this instantiator needs
     * further population after instantiation.
     *
     * @param clazz The class that is checked whether it requires further population or not after being instantiated
     * @return true by default
     * @since 3.1.5
     */
    default <T> boolean needsFurtherPopulation(Class<T> clazz, T instance) {
        return true;
    }
}
