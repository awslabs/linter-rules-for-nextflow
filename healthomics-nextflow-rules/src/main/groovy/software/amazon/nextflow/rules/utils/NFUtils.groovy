package software.amazon.nextflow.rules.utils

class NFUtils {
    def static ALLOWED_NF_PROCESS_DIRECTIVES = [
            'accelerator', 'afterScript', 'arch', 'beforeScript', 'cache', 'clusterOptions', 'conda', 'container',
            'containerOptions', 'cpus', 'debug', 'disk', 'echo', 'errorStrategy', 'executor', /*'ext'*/ 'fair', 'label',
            'machineType', 'maxSubmitAwait', 'maxErrors', 'maxForks', 'maxRetries', 'memory', 'module', 'penv', 'pod',
            'publishDir', 'queue', 'resourceLabels', 'scratch', 'shell', 'spack', 'stageInMode', 'stageOutMode', 'storeDir',
            'tag', 'time'
    ]

    def static ALLOWED_NF_INPUTS_DIRECTIVES = [
            'val', 'file', 'path', 'env', 'stdin', 'set', 'tuple', 'each'
    ]

    def static ALLOWED_NF_OUTPUTS_DIRECTIVES = [
            'val', 'file', 'path', 'env', 'stdout', 'set', 'tuple'
    ]
}
