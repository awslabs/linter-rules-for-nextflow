/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

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
                'the GPU type specified: not-a-real-gpu is not a supported type [nvidia-tesla-t4, nvidia-tesla-t4-a10g, nvidia-tesla-a10g, nvidia-l4-a10g, nvidia-l4, nvidia-l40s]')
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

    @Test
    void gpuRule_ParameterOrderIndependent(){
        // Test that both parameter orders work (though Groovy parses them the same way)
        final SOURCE1 = '''
        process MY_PROCESS{
            accelerator 2, type: 'nvidia-tesla-t4'
        }
        '''
        
        final SOURCE2 = '''
        process MY_PROCESS{
            accelerator type: 'nvidia-tesla-t4', 2
        }
        '''

        assertNoViolations(SOURCE1)
        assertNoViolations(SOURCE2)
    }

    @Test
    void gpuRule_MissingCountParameter(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator type: 'nvidia-tesla-t4'
        }
        '''
        assertSingleViolation(SOURCE, 3, 'accelerator type: \'nvidia-tesla-t4\'', 
                'the accelerator directive requires 2 arguments')
    }

    @Test
    void gpuRule_MissingTypeParameter(){
        final SOURCE = '''
        process MY_PROCESS{
            accelerator 2
        }
        '''
        assertSingleViolation(SOURCE, 3, 'accelerator 2', 
                'the accelerator directive requires 2 arguments')
    }

    @Test
    void gpuRule_NewGpuTypesNoViolation(){
        // Test all the new GPU types are accepted
        final SOURCE_L4 = '''
        process MY_PROCESS{
            accelerator 1, type: 'nvidia-l4'
        }
        '''
        
        final SOURCE_L4_A10G = '''
        process MY_PROCESS{
            accelerator 1, type: 'nvidia-l4-a10g'
        }
        '''
        
        final SOURCE_L40S = '''
        process MY_PROCESS{
            accelerator 1, type: 'nvidia-l40s'
        }
        '''

        assertNoViolations(SOURCE_L4)
        assertNoViolations(SOURCE_L4_A10G)
        assertNoViolations(SOURCE_L40S)
    }

}