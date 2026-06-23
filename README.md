> **Deprecated:** This repository is no longer actively maintained and has been archived.

JFR Equality Experiments
========================

Experiments to show why comparing objects from JFR recordings in Java is tricky.

Run with:

```shell
mvn clean package
java --add-opens jdk.jfr/jdk.jfr.consumer=ALL-UNNAMED -jar target/equality.jar
```

You can read all about it in the accompanying [blog post](https://mostlynerdless.de/blog/2025/10/10/jfr-and-equality-a-tale-of-many-objects/).

License
-------
MIT, Copyright 2025 SAP SE or an SAP affiliate company and contributors.