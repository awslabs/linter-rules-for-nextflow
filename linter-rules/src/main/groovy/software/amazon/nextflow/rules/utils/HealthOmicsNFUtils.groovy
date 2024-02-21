/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.utils

class HealthOmicsNFUtils {
    def static UNSUPPORTED_NF_PROCESS_DIRECTIVES = [
            'afterScript',
            'arch',
            'beforeScript',
            'cache',        // enable when we have call caching
            'clusterOptions',
            'conda',
            'containerOptions',
            'debug',
            'disk',
            'echo',
            'executor',
            'machineType',
            'maxForks',
            'module',
            'penv',
            'pod',
            'queue',
            'scratch',
            'shell',
            'spack',
            'stageInMode',
            'stageOutMode',
            'storeDir',     // possibly enable when we have call caching
    ]
}
