# Scala Native name demangler

[![sn-demangler Scala version support](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler/latest-by-scala-version.svg?targetType=Native)](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler) [![sn-demangler-core Scala version support](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler-core/latest-by-scala-version.svg)](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler-core)

Status: sort of works.

# Why?

Scala Native mangles names in the binaries according to a particular scheme:

https://scala-native.readthedocs.io/en/latest/contrib/mangling.html

This project parses the mangled name back into a more readable format. It is
distributed as both a runnable application, and an embeddable library.

## Application

Launch the JVM version using coursier:

```
$ cs launch com.indoorvivants::sn-demangler:latest.release -- -s '_SM36scala.scalanative.runtime.BoxedUnit$G8instance' '_SM34scala.scalanative.runtime.package$D16throwNullPointernEO'

// scala.scalanative.runtime.BoxedUnit$.<generated> instance
// scala.scalanative.runtime.package$.throwNullPointer(): Nothing
```

* `-s id1 id2 id3 ...` accepts any number of identifiers
* `-f <filename>` - processes a file, assuming a single identifier per line
* `-i` - accepts input from stdin
