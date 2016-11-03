# lambda-matcher
Create Hamcrest Matchers with Java 8 method references and lambdas.

An alternative to [`FeatureMatcher`](http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/FeatureMatcher.html). Instead of implementing a new class, just call one of the static factory methods in [`LambdaMatcher`](src/main/java/name/falgout/jeffrey/testing/LambdaMatcher.java). For instance, if you wanted to create a matcher on a `Throwable`'s message: `LambdaMatcher.createMatcher(Throwable::getMessage, yourStringMatcher)`.
