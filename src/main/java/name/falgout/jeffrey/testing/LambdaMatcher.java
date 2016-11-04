package name.falgout.jeffrey.testing;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * This class is basically {@link org.hamcrest.FeatureMatcher} with some syntactic sugar for method
 * references. If you're using a method reference, we can automatically generate the name and
 * description.
 */
public final class LambdaMatcher<T, R> extends TypeSafeDiagnosingMatcher<T> {
  public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}

  public static <T, R> Matcher<? super T> createMatcher(
      SerializableFunction<? super T, ? extends R> methodReference, Matcher<? super R> matcher) {
    try {
      if (!Lambdas.isMethodReference(methodReference)) {
        throw new IllegalArgumentException(methodReference + " is not a method reference.");
      }

      Class<?> parameterType = Lambdas.getParameterTypes(methodReference)[0];
      String methodName = Lambdas.getMethodName(methodReference);
      List<? extends Object> capturedArguments = Lambdas.getCapturedArguments(methodReference);
      if (capturedArguments.size() > 1) {
        throw new AssertionError("How can this happen?");
      }
      Optional<? extends Object> capturedArgument =
          capturedArguments.isEmpty() ? Optional.empty() : Optional.of(capturedArguments.get(0));

      String description = createDescription(capturedArgument, parameterType, methodName);

      return new LambdaMatcher<>(parameterType, description, methodReference, matcher);
    } catch (ObjectStreamException | ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  private static String createDescription(Optional<? extends Object> capturedArgument,
      Class<?> parameterType, String methodName) {
    return capturedArgument.map(arg -> {
      StringBuilder description = new StringBuilder();
      description.append(arg.getClass().getSimpleName());
      description.append(" instance<").append(arg).append(">");
      description.append(methodName).append("(").append(parameterType.getSimpleName()).append(")");
      return description.toString();
    }).orElseGet(() -> parameterType.getSimpleName() + " with " + methodName + "()");
  }

  public static <T, R> Matcher<? super T> createMatcher(
      SerializableFunction<? super T, ? extends R> lambda, String description,
      Matcher<? super R> matcher) {
    try {
      Class<?> parameterType = Lambdas.getParameterTypes(lambda)[0];
      return new LambdaMatcher<>(parameterType, description, lambda, matcher);
    } catch (ObjectStreamException | ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  private final String description;
  private final Function<? super T, ? extends R> mapper;
  private final Matcher<? super R> delegate;

  private LambdaMatcher(Class<?> expectedType, String description,
      Function<? super T, ? extends R> mapper, Matcher<? super R> delegate) {
    super(expectedType);
    this.description = description;
    this.mapper = mapper;
    this.delegate = delegate;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(this.description).appendText(" ").appendDescriptionOf(delegate);
  }

  @Override
  protected boolean matchesSafely(T item, Description mismatchDescription) {
    R value = mapper.apply(item);
    if (delegate.matches(value)) {
      return true;
    }

    delegate.describeMismatch(value, mismatchDescription);
    mismatchDescription.appendText(":\n").appendValue(item);
    return false;
  }
}
