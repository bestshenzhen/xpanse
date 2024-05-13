/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.monitor.exceptions.ClientApiCallFailedException;
import org.eclipse.xpanse.modules.orchestrator.servicestate.ServiceStateManageRequest;
import org.eclipse.xpanse.plugins.openstack.common.keystone.KeystoneManager;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.compute.Action;
import org.openstack4j.model.compute.RebootType;
import org.openstack4j.model.compute.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Class to manage state of VMs for Openstack plugin.
 */
@Slf4j
@Component
public class OpenstackServersManager {

    private final KeystoneManager keystoneManager;

    private final CredentialCenter credentialCenter;


    /**
     * Constructor for the MetricsManager bean.
     *
     * @param keystoneManager  KeystoneManager bean.
     * @param credentialCenter credentialCenter bean.
     */
    @Autowired
    public OpenstackServersManager(KeystoneManager keystoneManager,
                                   CredentialCenter credentialCenter) {
        this.keystoneManager = keystoneManager;
        this.credentialCenter = credentialCenter;
    }


    /**
     * Start the OpenStack Nova VM.
     */
    public boolean startService(ServiceStateManageRequest request) {
        OSClient.OSClientV3 osClient = getOsClient(request.getUserId(), request.getServiceId());
        List<String> errorMessages = new ArrayList<>();
        for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
            Server serverBeforeStart =
                    osClient.compute().servers().get(resource.getResourceId());
            if (serverBeforeStart.getStatus().equals(Server.Status.ACTIVE)) {
                log.info("Resource with id {} is already started.", resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse =
                    osClient.compute().servers().action(resource.getResourceId(), Action.START);
            if (actionResponse.isSuccess()) {
                log.info("Start resource with id {} successfully.", resource.getResourceId());
            } else {
                String errorMsg = String.format("Start resource %s failed, error: %s",
                        resource.getResourceId(), actionResponse.getFault());
                log.error(errorMsg);
                errorMessages.add(errorMsg);
            }
        }
        if (!CollectionUtils.isEmpty(errorMessages)) {
            throw new ClientApiCallFailedException(String.join("\n", errorMessages));
        }
        return true;
    }


    /**
     * Stop the OpenStack Nova VM.
     */
    public boolean stopService(ServiceStateManageRequest request) {
        OSClient.OSClientV3 osClient = getOsClient(request.getUserId(), request.getServiceId());
        List<String> errorMessages = new ArrayList<>();
        for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
            Server serverBeforeStop =
                    osClient.compute().servers().get(resource.getResourceId());
            if (serverBeforeStop.getStatus().equals(Server.Status.SHUTOFF)) {
                log.info("Resource with id {} is already stopped.", resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse =
                    osClient.compute().servers().action(resource.getResourceId(), Action.STOP);
            if (actionResponse.isSuccess()) {
                log.info("Stop resource {} successfully.", resource.getResourceId());
            } else {
                String errorMsg = String.format("Stop resource %s failed, error: %s",
                        resource.getResourceId(), actionResponse.getFault());
                log.error(errorMsg);
                errorMessages.add(errorMsg);
            }
        }
        if (!CollectionUtils.isEmpty(errorMessages)) {
            throw new ClientApiCallFailedException(String.join("\n", errorMessages));
        }
        return true;
    }

    /**
     * Restart the OpenStack Nova VM.
     */
    public boolean restartService(ServiceStateManageRequest request) {
        OSClient.OSClientV3 osClient = getOsClient(request.getUserId(), request.getServiceId());
        List<String> errorMessages = new ArrayList<>();
        for (DeployResourceEntity resource : request.getDeployResourceEntityList()) {
            Server serverBeforeRestart =
                    osClient.compute().servers().get(resource.getResourceId());
            if (!serverBeforeRestart.getStatus().equals(Server.Status.ACTIVE)) {
                log.error("Resource with id {} could not be restarted when it is inactive.",
                        resource.getResourceId());
                continue;
            }
            ActionResponse actionResponse = osClient.compute().servers()
                    .reboot(resource.getResourceId(), RebootType.SOFT);
            if (actionResponse.isSuccess()) {
                log.info("Restart resource with id {} successfully.", resource.getResourceId());
            } else {
                String errorMsg = String.format("Restart resource %s failed, error: %s",
                        resource.getResourceId(), actionResponse.getFault());
                log.error(errorMsg);
                errorMessages.add(errorMsg);
            }
        }
        if (!CollectionUtils.isEmpty(errorMessages)) {
            throw new ClientApiCallFailedException(String.join("\n", errorMessages));
        }
        return true;
    }

    private OSClient.OSClientV3 getOsClient(String userId, UUID serviceId) {
        AbstractCredentialInfo credentialInfo =
                credentialCenter.getCredential(Csp.OPENSTACK, CredentialType.VARIABLES, userId);
        return keystoneManager.getAuthenticatedClient(serviceId, credentialInfo);
    }
}



