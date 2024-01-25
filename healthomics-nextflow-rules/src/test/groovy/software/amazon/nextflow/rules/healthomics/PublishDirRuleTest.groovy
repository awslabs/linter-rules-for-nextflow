/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codenarc.rule.AbstractRuleTestCase
import org.junit.jupiter.api.Test

class PublishDirRuleTest extends AbstractRuleTestCase<PublishDirRule>{
    @Override
    protected PublishDirRule createRule() {
        new PublishDirRule()
    }

    @Test
    void testRuleProperties(){
        assert rule.priority == 1
        assert rule.name == 'PublishDirRule'
    }

    @Test
    void publishDirRule_NoViolations(){
        final String SOURCE =
'''
process foo {
    publishDir '/mnt/workflow/pubdir'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDirRule_NoViolationsMultipleArgs(){
        final String SOURCE =
                '''
process foo {
    publishDir '/mnt/workflow/pubdir', mode: 'link'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDirRule_NoViolationsMultipleNamedArgs(){

        // this results in a MethodCallExpression with a NamedArgumentListExpression
        final String SOURCE =
                '''
process foo {
    publishDir path: '/mnt/workflow/pubdir', mode: 'link'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDirRule_NoViolationsTrailingSlash(){
        final String SOURCE =
'''
process foo {
    publishDir '/mnt/workflow/pubdir/'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDirRule_NoViolationsTrailingDirs(){
        final String SOURCE =
                '''
process foo {
    publishDir '/mnt/workflow/pubdir/dir1/dir2/'
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDirRule_NoViolationsParameterized(){
        final String SOURCE =
'''
process foo {
    publishDir params.pubdir
}
'''
        assertNoViolations(SOURCE)
    }

    @Test
    void publishDir_OneViolation(){
        final String SOURCE =
'''
process foo {
    publishDir '/foo/baa/'
}
'''
        assertSingleViolation(SOURCE, 3, "publishDir '/foo/baa/",
                        "AWS HealthOmics requires the value of publishDir to be '/mnt/workflow/pubdir'. Please replace the current value")
    }
}
