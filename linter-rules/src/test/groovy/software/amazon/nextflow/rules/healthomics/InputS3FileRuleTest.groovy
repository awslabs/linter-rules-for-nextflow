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
            process bar {
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
    void filePath_TooManyArgsViolations() {
       final SOURCE = '''
            process bar {
                input:
                  path 's3://my-input-bucket/data/input1.txt', 's3://my-input-bucket/data/input2.txt'
            }
        '''
        assertSingleViolation(SOURCE, 4, "path 's3://my-input-bucket/data/input1.txt', 's3://my-input-bucket/data/input2.txt'","The path directive must have exactly one argument.")
    }

    @Test
    void filePath_LocalPathViolation() {
       final SOURCE = '''
            process foo {
                input:
                  path '/home/data/input1.txt'
            }
        '''
        assertSingleViolation(SOURCE, 4, "path '/home/data/input1.txt'", "The file path '/home/data/input1.txt' does not match the pattern 's3://.*'. Replace with an S3 object URI.")
    }

    @Test
    void filePathParam_NoViolations() {
       final SOURCE = '''

            process baz {
                input:
                  path params.infile
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePathParam_OneViolations() {
       final SOURCE = '''
            process baz {
                input:
                  path par.infile
            }
        '''
        assertSingleViolation(SOURCE, 4, "path par.infile", "The file path must be either a string or params.")
    }
}
