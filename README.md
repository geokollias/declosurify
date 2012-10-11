Declosurify
===========

It won't work without this:

  https://github.com/paulp/scala/tree/macro-minimum

I could not for the life of me convince sbt to actually use my local scala - it uses it for the compiler and library jars, but then downloads a snapshot of the reflect jar, which is useless when your local changes are in the reflect jar.  I resorted to copying my jars into the ivy cache.  Perhaps you will find this useful after building the macro-minimum branch:

```
#!/bin/sh

for j in library compiler reflect; do
  rm -f ~/.ivy2/cache/org.scala-lang/scala-$j/jars/*-2.10.0-SNAPSHOT.jar
  cp build/pack/lib/scala-$j.jar ~/.ivy2/cache/org.scala-lang/scala-$j/jars/scala-$j-2.10.0-SNAPSHOT.jar
  ls -l ~/.ivy2/cache/org.scala-lang/scala-$j/jars
done
```

Then you can

```
sbt test:run
```

And you should see [this output](https://raw.github.com/paulp/declosurify/master/test-output.txt).
