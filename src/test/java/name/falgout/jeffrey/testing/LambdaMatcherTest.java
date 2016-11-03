package name.falgout.jeffrey.testing;

import static com.google.common.truth.Truth.assertThat;
import static name.falgout.jeffrey.testing.ThrowableMessageMatcher.hasMessage;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

public class LambdaMatcherTest {
  Throwable t = new Throwable("foobar");

  @Test
  public void testMatch() {
    assertThat(t, hasMessage("foo"));
  }

  @Test
  public void testMismatch() {
    Description mismatch = new StringDescription();
    hasMessage("abcd").describeMismatch(t, mismatch);

    assertThat(mismatch.toString()).contains("getMessage");
  }

  @Test
  public void testDescription() {
    String message = "abcd";
    Description description = new StringDescription();
    hasMessage(message).describeTo(description);

    assertThat(description.toString()).contains("Throwable");
    assertThat(description.toString()).contains("getMessage");
    assertThat(description.toString()).contains(message);
  }

  @Test
  public void testCapturedMethodReference() {
    List<String> strings = Arrays.asList("0", "1", "2", "3");
    Matcher<? super Integer> indexOfMatcher =
        LambdaMatcher.createMatcher(strings::get, CoreMatchers.equalTo("3"));

    assertThat(3, indexOfMatcher);
    assertFalse(indexOfMatcher.matches(2));
  }
}
