/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.studio.maven.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RestAPIExtensionArchetypeTest {

    @Test
    public void should_reference_rest_api_extension_archetype() throws Exception {
        var archetype = RestAPIExtensionArchetype.INSTANCE;

        assertThat(archetype.getGroupId()).isEqualTo("org.bonitasoft.archetypes");
        assertThat(archetype.getArtifactId()).isEqualTo("bonita-rest-api-extension-archetype");
    }

}
