package software.amazon.nextflow.rules.healthomics

class HealthOmicsNFUtils {
    def static UNSUPPORTED_NF_PROCESS_DIRECTIVES = [
            'afterScript',
            'arch',
            'beforeScript',
            'cache',        //will be allowed when we have call caching
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
            'storeDir',     //might be allowed when we have call caching
    ]
}
