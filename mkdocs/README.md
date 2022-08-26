# Welcome to the MORP Documentation

The documentation is built by MkDocs. For full documentation visit [www.mkdocs.org](https://www.mkdocs.org/).
Documentation versioning is implemented using [Mike](https://github.com/jimporter/mike).

# Editing

You can simply edit the Markdown files and submit a Pull Request.
To test if the syntax is correct, you can run the site locally. See below.

# Running locally

## Bare-metal

Install `python` and `pip` on your machine and then execute the following:

```shell
pip install -r requirements.txt
mkdocs serve
```

## Docker

If you don't want to install Python on your machine, you can just run

```shell
docker-compose up -d
```