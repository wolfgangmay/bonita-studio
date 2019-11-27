/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.asciidoc.templating.model.bdm

import groovy.transform.Canonical
import groovy.transform.builder.Builder

/**
 * Business Data Model of a Bonita project.
 */
@Canonical
@Builder
class BusinessObject {

    /**
     * The name of the Business Object
     */
    String name

    /**
     * The package of the Business Object
     */
    String packageName

    /**
     * The description of the Business Object
     */
    String description

    /**
     * The list of attributes of the Business Object
     */
    Attribute[] attributes

    /**
     * The list of relations of the Business Object
     */
    Relation[] relations

    /**
     * The list of custom queries of the Business Object
     */
    Query[] customQueries

    /**
     * The list of default queries of the Business Object
     */
    Query[] defaultQueries
}