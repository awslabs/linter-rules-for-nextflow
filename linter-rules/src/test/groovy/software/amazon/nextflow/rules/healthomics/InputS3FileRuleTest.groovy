/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics


import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

/*
 * Tests for InputFileIsS3Rule
 *
 * @author Jesse Marks
 */

class InputS3FileRuleTest extends AbstractRuleTestCase<InputS3FileRule> {

    @Override                                                                                                                           
    protected InputS3FileRule createRule() {                                                                                                    
        new InputS3FileRule()                                                                                                                   
    }                                                                                                                                   
           
    @Test
    void RuleProperties() {
        assert rule.priority == 1
        assert rule.name == 'InputFileIsS3'
    }

    @Test
    void filePath_NoViolations() {
       final SOURCE = '''
            params {
                publishDir = "s3://my-output-bucket/results"
            }

            process processData {
                publishDir params.publishDir
                cpus 1

                input:
                path "s3://my-input-bucket/data/input1.txt"
                path "s3://my-input-bucket/data/input2.txt"


                """
                echo "All input files are valid S3 paths."
                """
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePath_OneViolation() {
       final SOURCE = '''

            process foo {
                input:
                path '/home/data/input1.txt'
            }
        '''
        assertSingleViolation(SOURCE, 5, "path '/home/data/input1.txt'", "The file path URI '/home/data/input1.txt' does not match the pattern 's3://.*'. Replace with a file located on s3.")
    }
}
