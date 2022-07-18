# Contributing to MORP

Welcome and thank you for investing your time in contributing to our project! Please take some time to read through this
document to make sure everyone has a great experience during the contribution process.

## Code of Conduct

We take our open-source community seriously and hold ourselves and other contributors to high standards of
communication. By participating and contributing to this project, you agree to uphold
our [Code of Conduct](https://github.com/jaconi-io/morp/blob/main/CODE_OF_CONDUCT.md).

## How to contribute

Contributions are made to this repo via Issues and Pull Requests (PRs). A few general guidelines that cover both:

- To report security vulnerabilities, please [email](mailto:security@jaconi.io) us directly.
- Search for existing Issues and PRs before creating your own.
- We work hard to makes sure issues are handled in a timely manner but, depending on the impact, it could take a while
  to investigate the root cause. A friendly ping in the comment thread to the submitter or a contributor can help draw
  attention if your issue is blocking.

### Issues

Issues should be used to report problems with MORP, request a new feature, or to discuss potential changes before a PR
is created. When you create a new Issue, a template will be loaded that will guide you through collecting and providing
the information we need to investigate.

If you find an Issue that addresses the problem you're having, please add your own reproduction information to the
existing issue rather than creating a new one. Adding a
[reaction](https://github.blog/2016-03-10-add-reactions-to-pull-requests-issues-and-comments/) can also help be
indicating to our maintainers that a particular problem is affecting more than just the reporter.

### Pull Requests

PRs to MORP are always welcome and can be a quick way to get your fix or improvement slated for the next release. In
general, PRs should:

- Only fix/add the functionality in question
- Add unit or integration tests for fixed or changed functionality
- Respect coverage and other code health metrics analyzed within th PR
- Include documentation in the repo
- Be accompanied by a complete Pull Request template (loaded automatically when a PR is created).

For changes that address core functionality or would require breaking changes (e.g. a major release), it's best to open
an Issue to discuss your proposal first. This is not required but can save time creating and reviewing changes.

In general, we follow the [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow) (with forks)
workflow:

1. Fork the repository to your own GitHub account
2. Clone the project to your machine
3. Create a branch locally with a short, descriptive name
4. Develop you fix/feature following any formatting and testing guidelines specific to this repo
5. Commit changes to the branch, following the [commit message conventions](#Commit message conventions) outlined below
7. Push changes to your fork
8. Open a PR in our repository and follow the PR template so that we can efficiently review the changes.

### Commit message conventions

MORP follows the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) specification.

Commit messages should follow the following scheme:

```text
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

where `type` must fall into one of the following categories:

* **build**: Changes that affect the build system or external dependencies (example scopes: gradle)
* **ci**: Changes to our CI configuration files and scripts (example scopes: gh-actions)
* **docs**: Documentation only changes
* **feat**: A new feature
* **fix**: A bug fix
* **perf**: A code change that improves performance
* **refactor**: A code change that neither fixes a bug nor adds a feature
* **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
* **test**: Adding missing tests or correcting existing tests

## License

By contributing to this project, you agree that your contributions will be licensed under
its [MIT License](https://github.com/jaconi-io/morp/blob/main/LICENSE.md).

## Seeking Help

Join us at [Github Discussions for MORP](https://github.com/jaconi-io/morp/discussions) and post your question there in
the correct category with a descriptive tag or leverage an existing issue to get help.