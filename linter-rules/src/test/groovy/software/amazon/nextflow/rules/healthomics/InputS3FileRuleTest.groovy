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
            process foo {
                input:
                  path 's3://my-input-bucket/data/bar.txt'
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePath_LocalPathViolation() {
       final SOURCE = '''
            process foo {
                input:
                  path '/home/bar/baz.txt'
            }
        '''
        assertSingleViolation(SOURCE, 4, "path '/home/bar/baz.txt'","The file path '/home/bar/baz.txt' does not match the pattern 's3://.*'. Replace with an S3 object URI.")
    }

    @Test
    void filePath_HttpPathViolation() {
       final SOURCE = '''
            process foo {
                input:
                  path 'http://example.com/bar.txt'
            }
        '''
        assertSingleViolation(SOURCE, 4, "path 'http://example.com/bar.txt'","The file path 'http://example.com/bar.txt' does not match the pattern 's3://.*'. Replace with an S3 object URI.")
    }

    @Test
    void filePath_FtpPathViolation() {
       final SOURCE = '''
            process foo {
                input:
                  path 'ftp://example.com/bar.txt'
            }
        '''
        assertSingleViolation(SOURCE, 4, "path 'ftp://example.com/bar.txt'","The file path 'ftp://example.com/bar.txt' does not match the pattern 's3://.*'. Replace with an S3 object URI.")
    }

    @Test
    void filePathParam_NoViolations() {
       final SOURCE = '''
            process foo {
                input:
                  path bar.baz
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePathVariable_NoViolations() {
       final SOURCE = '''
            process foo {
                input:
                  path bar
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePathExpression1_NoViolations() {
       final SOURCE = '''
            process foo {
                input:
                    path '${bar}.txt'
            }
        '''
        assertNoViolations(SOURCE)
    }

    @Test
    void filePathExpression2_NoViolations() {
       final SOURCE = '''
            process foo {
                input:
                    path '${params.bar}/${baz}.txt'
            }
        '''
        assertNoViolations(SOURCE)
    }

}
