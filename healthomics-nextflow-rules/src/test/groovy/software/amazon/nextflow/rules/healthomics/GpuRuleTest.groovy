package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class GpuRuleTest extends AbstractRuleTestCase<GpuRule> {
    @Override
    protected GpuRule createRule() {
        new GpuRule()
    }

    @Test
    void ruleProperties(){
        assert rule.name == "GpuRule"
        assert rule.priority == 2
    }

    @Test
    void gpuRule_ArgumentsNoViolation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator 1, type: 'nvidia-tesla-t4-a10g'
        }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void gpuRule_ArgumentsViolation(){
        final SOURCE= '''
            process MY_PROCESS{
                accelerator 1
            }
            '''
        assertSingleViolation(SOURCE, 3, 'accelerator 1', 'the accelerator directive requires 2 arguments')
    }

    @Test
    void gpuRule_GpuTypeViolation(){
        final SOURCE = '''
        process MY_PROCESS {
            accelerator 1, type: 'not-a-real-gpu'
        }
        '''
        assertSingleViolation(
                SOURCE, 3, 'accelerator 1, type: \'not-a-real-gpu\'',
                'the GPU type specified: not-a-real-gpu is not a supported type [nvidia-tesla-t4, nvidia-tesla-a10g, nvidia-tesla-t4-a10g]')
    }

    @Test
    void gpuRule_GpuCountViolation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator 10, type: 'nvidia-tesla-t4-a10g'
        }
        '''
        assertSingleViolation(SOURCE, 3, 'accelerator 10, type: \'nvidia-tesla-t4-a10g\'', 'The GPU count: 10 exceeds the maximum: 4')
    }

    @Test
    void gpuRule_GpuMinCountVioloation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator 0.5, type: 'nvidia-tesla-t4-a10g'
        }
        '''
        assertSingleViolation(SOURCE, 3, 'accelerator 0.5, type: \'nvidia-tesla-t4-a10g\'', "The GPU count: 0.5 is below the minimum: 1")
    }

    @Test
    void gpuRule_ParamsNoViolation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator params.num_gpu, type: params.type_gpu
        }
        '''

        assertNoViolations(SOURCE)
    }

    @Test
    void gpuRule_ParamsTypeViolation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator 1, type: params.type_gpu
        }
        '''

        assertNoViolations(SOURCE)
    }

    @Test
    void gpuRule_ParamsCountViolation(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator params.num_gpu, type: 'nvidia-tesla-t4-a10g'
        }
        '''

        assertNoViolations(SOURCE)
    }

}