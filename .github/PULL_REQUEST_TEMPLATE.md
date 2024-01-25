<!-- Title format: type(scope): Short description (72 chars max for line) -->
<!-- `(scope)` is optional and `type` is one of: build, ci, chore, docs, feat, fix, perf, refactor, revert, style, test -->
<!-- Available types:
- feat: A new feature
- fix: A bug fix
- docs: Documentation only changes
- style: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- refactor: A code change that neither fixes a bug nor adds a feature
- perf: A code change that improves performance
- test: Adding missing tests or correcting existing tests
- build: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- ci: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- chore: Other changes that don't modify src or test files
- revert: Reverts a previous commit -->

Issue #, if available:

**Description of Changes**

[//]: #  (A description of the change that you made and the new user experience that it creates)

**Description of how you validated changes**

[//]: #  (A description of you validated your changes and any relevant logs that show your change works)


**Checklist**

- [ ] If I have added a new rule, that rule has also been added to `healthomics-nextflow-rules/src/main/resources/rulesets/healthomics.xml`
- [ ] If this change would make any existing documentation invalid, I have included those updates within this PR
- [ ] I have added unit tests that prove my fix is effective or that my feature works
- [ ] I have confirmed that I am able to locally build the project with no errors (`./gradlew clean build`)
- [ ] I have not made extensive or unnecessary formatting changes unless this PR is specifically and only about reformatting
- [ ] Title of this Pull Request follows Conventional Commits standard: https://www.conventionalcommits.org/en/v1.0.0/

----

*By submitting this pull request, I confirm that my contribution is made under the terms of the Apache-2.0 license*