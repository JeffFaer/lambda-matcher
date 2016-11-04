# lambda-matcher
Create Hamcrest Matchers with Java 8 method references and lambdas.

[![Maven Central][mvn-img]][mvn-link]

An alternative to [`FeatureMatcher`](http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/FeatureMatcher.html). Instead of implementing a new class, just call one of the static factory methods in [`LambdaMatcher`](src/main/java/name/falgout/jeffrey/testing/LambdaMatcher.java). For instance, if you wanted to create a matcher on a `Throwable`'s message:
```java
  Throwable t = new Throwable("foobar");

  @Test
  public void test() {
    assertThat(t, hasMessage(containsString("foo")));
  }
  
  public static Matcher<? super Throwable> hasMessage(Matcher<? super String> matcher) {
  	return LambdaMatcher.createMatcher(Throwable::getMessage, matcher);
  }
```

[mvn-img]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/lambda-matcher/badge.svg
[mvn-link]: https://maven-badges.herokuapp.com/maven-central/name.falgout.jeffrey.testing/lambda-matcher
