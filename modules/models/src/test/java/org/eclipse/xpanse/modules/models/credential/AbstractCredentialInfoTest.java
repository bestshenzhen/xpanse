/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Test;

/**
 * Test of AbstractCredentialInfo.
 */
class AbstractCredentialInfoTest {

    @Test
    public void testConstructorAndGetters() {
        Csp csp = Csp.HUAWEI;
        String xpanseUser = "user";
        String name = "credential";
        String description = "Test credential";
        CredentialType type = CredentialType.VARIABLES;
        Integer timeToLive = 100000;

        AbstractCredentialInfo credentialInfo =
                new AbstractCredentialInfoImpl(csp, xpanseUser, name, description, type);

        credentialInfo.setTimeToLive(timeToLive);
        credentialInfo.setCsp(Csp.FLEXIBLE_ENGINE);
        credentialInfo.setXpanseUser("admin");

        assertEquals(Csp.FLEXIBLE_ENGINE, credentialInfo.getCsp());
        assertEquals("admin", credentialInfo.getXpanseUser());
        assertEquals(name, credentialInfo.getName());
        assertEquals(description, credentialInfo.getDescription());
        assertEquals(type, credentialInfo.getType());
        assertEquals(timeToLive, credentialInfo.getTimeToLive());
    }

    private static class AbstractCredentialInfoImpl extends AbstractCredentialInfo {
        AbstractCredentialInfoImpl(Csp csp, String xpanseUser, String name, String description,
                                   CredentialType type) {
            super(csp, xpanseUser, name, description, type);
        }
    }

}
