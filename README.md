# Scala Native name demangler

[![sn-demangler Scala version support](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler/latest-by-scala-version.svg?targetType=Native)](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler) [![sn-demangler-core Scala version support](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler-core/latest-by-scala-version.svg)](https://index.scala-lang.org/indoorvivants/sn-demangler/sn-demangler-core)


# Why?

Scala Native mangles names in the binaries according to a particular scheme:

https://scala-native.readthedocs.io/en/latest/contrib/mangling.html

This project parses the mangled name back into a more readable format. It is
distributed as both a runnable application, and an embeddable library.

Available as a native CLI, as a 

## Installation

- Download the native `sn-demangler` binary for your platform from [Github Releases](https://github.com/indoorvivants/sn-demangler/releases/latest)
- Use the [Web version](https://indoorvivants.github.io/sn-demangler/)
- Launch the JVM version directly from Coursier:
  ```
  cs launch com.indoorvivants::sn-demangler:latest.release -- <args>
  ```

## Usage

Demangler will attempt to find all SN identifiers, demangle them, and display the 
symbols inline with the rest of the text.

Let's say you have text like this:

```
hello _SM36scala.scalanative.runtime.BoxedUnit$G8instance world
```

The demangled symbols will be highlighted in the console (disable that with `--plain` or set `NO_COLOR=true` env variable).

<img width="2388" height="978" alt="CleanShot 2026-04-09 at 13 23 54@2x" src="https://github.com/user-attachments/assets/26181b29-17d4-48d2-ac3d-21e544804307" />


1. Demangle a single string:

    ```
    $ sn-demangler 'hello _SM36scala.scalanative.runtime.BoxedUnit$G8instance world'
    hello scala.scalanative.runtime.BoxedUnit$.<generated> instance world
    ```


2. Demangle a file:

    ```
    $ cat myfile
    hello scala.scalanative.runtime.BoxedUnit$.<generated> instance world
    
    $ sn-demangler -f myfile
    hello scala.scalanative.runtime.BoxedUnit$.<generated> instance world
    ```
  
3. Demangle from STDIN:

    ```
    $ cat myfile
    hello scala.scalanative.runtime.BoxedUnit$.<generated> instance world
    
    $ cat myfile | sn-demangler -f -
    hello scala.scalanative.runtime.BoxedUnit$.<generated> instance world
    ```
## Webapp

A webapp is available: https://indoorvivants.github.io/sn-demangler/

<img width="3746" height="1100" alt="CleanShot 2026-04-09 at 13 22 21@2x" src="https://github.com/user-attachments/assets/362217cb-ad4d-41c6-a19a-b557a673a906" />
