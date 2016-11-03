package name.falgout.jeffrey.testing;

import static org.hamcrest.CoreMatchers.containsString;

import org.hamcrest.Matcher;

public final class ThrowableMessageMatcher {
  private ThrowableMessageMatcher() {}

  public static Matcher<? super Throwable> hasMessage(String substring) {
    return hasMessage(containsString(substring));
  }

  public static Matcher<? super Throwable> hasMessage(Matcher<? super String> matcher) {
    return LambdaMatcher.createMatcher(Throwable::getMessage, matcher);
  }
}
