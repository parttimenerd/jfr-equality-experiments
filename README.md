JFR Equality Experiments
========================

Experiments to show why comparing objects from JFR recordings in Java is tricky.

Run with:

```shell
mvn clean package
java --add-opens jdk.jfr/jdk.jfr.consumer=ALL-UNNAMED -jar target/equality.jar
```


License
-------
MIT, Copyright 2025 SAP SE or an SAP affiliate company and contributors.