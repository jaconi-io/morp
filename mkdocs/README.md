# Welcome to the MORP Documentation

The documentation is built using [MkDocs](https://www.mkdocs.org/). Versioning is implemented using
[mike](https://github.com/jimporter/mike).

You can simply edit the Markdown files and submit a pull request. To test if the syntax is correct, you can run the site
locally:

```shell
# Bare metal (make sure python and pip are installed)
pip install -r requirements.txt
mkdocs serve

# Docker
docker compose up -d
```

The documentation can be viewed at [http://localhost:8000](http://localhost:8000) with either method.
