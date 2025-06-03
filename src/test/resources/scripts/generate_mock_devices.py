import uuid
from opentelemetry.sdk.metrics.export import MetricsData, MetricExportResult
from requests.adapters import HTTPAdapter
from urllib3.util import Retry
import requests
import os
import pandas as pd
from concurrent.futures import ThreadPoolExecutor


payload_template = {
        "tags": {},
        "tagKeys": [],
        "tagValues": [],
        "uid": "7e498ae4-3373-4229-9b8d-d6fd4724c6c8",
        "name": "dummy_device_3",
        "namespace": "targets",
        "type": "devices",
        "version": 1,
        "createdDate": 1747299192655,
        "lastUpdatedDate": 1748604191039,
        "actionContext": None,
        "stateMachineContext": None,
        "stateDate": None,
        "status": "IDLE",
        "stateMachineDetails": None,
        "scheduledStateMachineEnabledMap": {},
        "pendingStatesQueue": [],
        "createdTenantUid": None,
        "credentials": None,
        "associatedDeviceUid": "c8dca160-91b5-4bd9-b0c8-dd0faa76af50",
        "sseEnabled": False,
        "deviceRole": None,
        "sseDeviceRegistrationToken": None,
        "sseDeviceSerialNumberRegistration": None,
        "sseDeviceData": None,
        "disks": [],
        "customLinks": [],
        "failoverDisks": [],
        "shouldInitCreds": None,
        "ipv4": None,
        "interfaces": None,
        "deviceActivity": None,
        "serial": None,
        "chassisSerial": None,
        "softwareVersion": "7.7.0",
        "onboardingState": None,
        "connectivityState": 1,
        "connectivityError": None,
        "ignoreCertificate": False,
        "healthStatus": "WARNING",
        "config": None,
        "mostRecentDeviceConfig": None,
        "deviceConfigOnDisk": None,
        "deviceConfig": None,
        "certificate": None,
        "certificateExpirationDate": None,
        "anyConnectCertExpDate": None,
        "mostRecentCertificate": None,
        "configHash": 0,
        "configState": "NOT_SYNCED",
        "configProcessingState": "INACTIVE",
        "enableOobDetection": False,
        "oobDetectionState": "IN_SYNC",
        "lastOobDetectionStartedAt": None,
        "lastOobDetectionSuspendedAt": None,
        "autoAcceptOobEnabled": False,
        "modelNumber": "Cisco Firepower Threat Defense for VMware",
        "model": False,
        "deviceType": "FTDC",
        "deviceSubType": None,
        "hasFirepower": False,
        "lastErrorMap": {},
        "logs": None,
        "notes": None,
        "metadata": {
            "deviceRecordUuid": "c5d308c8-3168-11f0-bd43-f2c0a0fdf6a8",
            "redundancyMode": "STANDALONE",
            "deviceWithActionItem": "true",
            "modelNumber": "75",
            "modelId": "A",
            "snortVersion": None,
            "isClusterBootstrapSupported": "true",
            "ftdMode": "ROUTED",
            "accessPolicyUuid": None,
            "accessPolicyName": None,
            "healthMessage": None,
            "license_caps": "ESSENTIALS",
            "performanceTier": None,
            "containerId": None,
            "containerName": None,
            "containerType": None,
            "peerDeviceRecordUuid": None
        },
        "customData": None,
        "configurationReference": None,
        "externalServiceMetadata": None,
        "oobCheckInterval": None,
        "larUid": None,
        "larType": "SDC",
        "lastDeployTimestamp": 0,
        "host": "",
        "port": "",
        "loggingEnabled": False,
        "liveAsaDevice": False,
        "state": None,
        "triggerState": None,
        "queueTriggerState": None
    }

ftd_payload = {
        "tags": {},
        "tagKeys": [],
        "tagValues": [],
        "uid": "9932ba5a-7b99-4708-b6d4-a0a1fa0e6298",
        "name": "dummy_device_3",
        "namespace": "firepower",
        "type": "ftds",
        "version": 1,
        "createdDate": 1747299192633,
        "lastUpdatedDate": 1748604193953,
        "actionContext": None,
        "stateMachineContext": None,
        "stateDate": None,
        "status": "IDLE",
        "stateMachineDetails": None,
        "scheduledStateMachineEnabledMap": {},
        "pendingStatesQueue": [],
        "createdTenantUid": None,
        "credentials": None,
        "model": False,
        "licenseRequirements": [],
        "smartLicense": None,
        "subscriptionLicenses": None,
        "exportCompliant": False,
        "healthStatus": None,
        "automaticSecurityDbUpdatesEnabled": False,
        "securityDbsSyncSchedule": None,
        "deviceRecordId": "c5d308c8-3168-11f0-bd43-f2c0a0fdf6a8",
        "deviceUid": "7e498ae4-3373-4229-9b8d-d6fd4724c6c8",
        "issues": {},
        "devicesToBeAutoSelectedForMigration": [],
        "s2SVpnTopologiesToBeAutoSelectedForMigration": [],
        "domainName": "Global",
        "domainUid": "e276abec-e0f2-11e3-8169-6d9ed49b625f",
        "cdoPolicyUid": None,
        "fmcPolicyUid": None,
        "showAccessSettingsTooltip": True,
        "certificate": None,
        "licenses": None,
        "info": {},
        "templateUid": None,
        "templateType": None,
        "policyVersion": None,
        "objectsMap": {
            "physical_interface": {},
            "ikev2Policy": {},
            "identityservicesengine": {},
            "mgmtaccesshttpsdataport": {},
            "specialrealms": {},
            "intrusionpolicy": {},
            "successnetworksettings": {},
            "securityzone": {},
            "mgmtaccessdataobject": {},
            "networkobject": {},
            "duoldapidentitysource": {},
            "ntpobject": {},
            "filepolicy": {},
            "applicationfilter": {},
            "cloudeventssettings": {},
            "serviceudpobject": {},
            "url_feed": {},
            "manualnatrulecontainer": {},
            "datadnssettings": {},
            "mgmtdnssettings": {},
            "anyconnectpackagefiles": {},
            "ravpnconnectionprofile": {},
            "securitygrouptag": {},
            "continent": {},
            "devlogobject": {},
            "ikev1Proposal": {},
            "ikev2Proposal": {},
            "specialuser": {},
            "devicesettings_cloudconfig": {},
            "sgtgroup": {},
            "hostnameobject": {},
            "geolocation": {},
            "network_feed": {},
            "urlrepuation": {},
            "networkobjectgroup": {},
            "localidentitysources": {},
            "radiusidentitysourcegroup": {},
            "application": {},
            "urlobjectgroup": {},
            "urlobject": {},
            "internalcacertificate": {},
            "ipsUpdateSchedule": {},
            "ravpngrouppolicy": {},
            "serviceicmpv4object": {},
            "staticrouteentry": {},
            "serviceicmpv6object": {},
            "samlserver": {},
            "mgmtaccesshttpobject": {},
            "devicesettings_managementaccess": {},
            "activedirectoryrealms": {},
            "dummyTypeWithUnsupportedVersion": {},
            "cloudconfig": {},
            "dhcpobject": {},
            "cloudservicesinfo": {},
            "externalcacertificate": {},
            "dnsobject": {},
            "applicationcategory": {},
            "intrusionsettingsobject": {},
            "ikev1Policy": {},
            "radiusidentitysource": {},
            "ravpn": {},
            "syslogserver": {},
            "anyconnectprofiles": {},
            "realmsequence": {},
            "servicetcpobject": {},
            "dnsgroupobject": {},
            "serviceobjectgroup": {},
            "country": {},
            "intrusion_policy": {},
            "serviceprotocolobject": {},
            "anyconnectclientprofiles": {},
            "mgmtaccesssshobject": {},
            "objectnatrulecontainer": {},
            "urlcategory": {},
            "webanalyticssettings": {},
            "applicationtag": {},
            "ipspolicy": {},
            "internalcertificate": {},
            "embeddedappfilter": {}
        },
        "currDeploymentUid": None,
        "ftdRecurringIpsRuleUpdateImports": {
            "name": None,
            "user": None,
            "description": None,
            "kind": "sruupdateschedule",
            "version": None,
            "scheduleType": "DAILY",
            "runTimes": None,
            "uuid": None
        },
        "metadata": {
            "changeMgmtEligibilityInfo": "{\"eligibilityCode\":\"ERR_DEPLOYMENT_PENDING\",\"isEligibleForManagerChange\":False}"
        },
        "interfaces": [
            {
                "name": "GigabitEthernet0/0",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970403",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/1",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970404",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/2",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970405",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/3",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970406",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/4",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970407",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/5",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970408",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/6",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970409",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "GigabitEthernet0/7",
                "ifname": None,
                "id": "02C9DFB5-97D7-0ed3-0000-004294970410",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            },
            {
                "name": "Management0/0",
                "ifname": "management",
                "id": "02C9DFB5-97D7-0ed3-0000-004294970411",
                "type": "PhysicalInterface",
                "tunnelType": None,
                "description": None,
                "ipv4": None,
                "ipv6": {
                    "linkLocalAddress": None,
                    "addresses": None,
                    "dhcp": None
                },
                "metadata": {
                    "tunnelSourceIfName": None,
                    "tunnelSourceIPv4Details": None,
                    "tunnelSourceIPv6Details": None
                }
            }
        ],
        "supportedFeatures": {},
        "lastSavedPolicyJson": None,
        "tempRegistrationField": None,
        "ftdHaMetadata": None,
        "primaryDeviceDetails": None,
        "secondaryDeviceDetails": None,
        "primaryFtdHaStatus": None,
        "secondaryFtdHaStatus": None,
        "ftdHaError": None,
        "clusterControlNodeDeviceDetails": None,
        "clusterDataNodesDeviceDetails": None,
        "ftdSmartLicenseStatus": None,
        "hybridZeroTrustSetting": None,
        "hybridZeroTrustSupported": False,
        "clusterCombinedDevice": False,
        "haCombinedDevice": False,
        "state": None,
        "triggerState": None,
        "queueTriggerState": None,
        "otherAssociatedPolicies": [
            {
                "id": "4897c8f4-e211-4661-b0a4-25b0826cded9",
                "name": "Default Prefilter Policy",
                "type": "PrefilterPolicy"
            },
            {
                "id": "02C9DFB5-97D7-0ed3-0000-004294970494",
                "name": None,
                "type": "FlexConfigPolicy"
            },
            {
                "id": "02C9DFB5-97D7-0ed3-0000-008589936426",
                "name": "ravpn_config",
                "type": "RAVpn"
            }
        ]
    }

def get_payload(device_uuid):
    payload_template["metadata"]["deviceRecordUuid"] = device_uuid
    payload_template["uid"] = str(uuid.uuid4())
    return payload_template


def post(endpoint,token, payload=None, expected_return_code=200):
    try:
        print(f"Sending POST request to {endpoint} with payload {payload} and token {token}")
        retry = Retry(
            total=3,
            backoff_factor=2,
            status_forcelist=[i for i in range(400, 600)],
        )
        adapter = HTTPAdapter(max_retries=retry)
        session = requests.Session()
        session.mount("https://", adapter)
        response = session.post(
            endpoint,
            json=payload,
            headers={
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token ,
            },
            timeout=180,
        )
        print("Response: ", response)
        assert (
            response.status_code == expected_return_code
        ), f"POST request to {endpoint} failed with status code {response.status_code}"
        return response
    except Exception as e:
        print(f"Failed to send POST request to {endpoint} with payload {payload}")
        raise e

def process_token(token):
    try:
        print(post("https://edge.scale.cdo.cisco.com/aegis/rest/v1/services/targets/devices", token=token,
                   payload=payload_template))
        print(post("https://edge.scale.cdo.cisco.com/aegis/rest/v1/services/firepower/ftds", token=token,
                   payload=ftd_payload))
    except Exception as e:
        print(f"Error processing token {token}: {e}")

if __name__=="__main__":
    device_data = pd.read_csv("../device_uuids.csv")
    data = pd.read_csv("../api_user_tokens.csv")

    tokens = data["API_USER_TOKEN"].tolist()

    with ThreadPoolExecutor(max_workers=10) as executor:
        executor.map(lambda token: process_token(token), tokens)