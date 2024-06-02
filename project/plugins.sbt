addSbtPlugin("org.typelevel"    % "sbt-tpolecat" % "0.5.0")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.timushev.sbt" % "sbt-rewarn"   % "0.1.3")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("io.spray"     % "sbt-revolver" % "0.10.0")

addSbtPlugin("com.disneystreaming.smithy4s" % "smithy4s-sbt-codegen"       % "0.18.5")
addSbtPlugin("com.github.cornerman"         % "sbt-db-codegen"             % "0.4.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.16.0")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")

// sbt-db-codegen (with scalate) depends on parser-combinators v2, and scalablytyped depends on v1.
// https://github.com/ScalablyTyped/Converter/pull/624
// Not binary compatible, but works, so override it with v2:
dependencyOverrides += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
