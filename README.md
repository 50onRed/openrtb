Google OpenRTB Libraries
----------------------------------------------------------------------

This library supports the OpenRTB specification, providing
bindings for all protobuf-supported languages, and additional
support for Java such as JSON serialization and validation.


BUILDING NOTES
----------------------------------------------------------------------

You need: JDK 7, Maven 3.2, Protocol buffers (protoc) 2.5.0.
Building is supported from the command line with Maven and
from any IDE that can load Maven projects (on Eclipse, use m2e).

Recommended to run 'mvn clean install' after checkout, this is
important for the code generation steps that may not be performed
by some IDEs (Eclipse/m2e in particular).

Building via Maven will NOT work with JDK 8, because the projects
use error-prone which is not yet JDK 8-compatible.  You can work
around this by defining the property m2e.version to any value
(error-prone doesn't play well with m2e either, and we cannot use
a proper profile rule for <jdk>!1.8</jdk> because, you guessed,
this also breaks m2e). JDK 8 support is coming soon for error-prone
so this hack for non-Eclipse builds should be temporary.


RELEASE NOTES
----------------------------------------------------------------------

## Version 0.6.1, Jul 2014

* Build system improvements (Maven, Eclipse, NetBeans).

## Version 0.6, Jul 2014

* Initial Open Source release.
