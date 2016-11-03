package name.falgout.jeffrey.testing;

import static com.google.common.truth.Truth.assertThat;
import static name.falgout.jeffrey.testing.Lambdas.getParameterTypes;
import static name.falgout.jeffrey.testing.Lambdas.isLambda;
import static name.falgout.jeffrey.testing.Lambdas.isMethodReference;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

import org.junit.Test;

public class LambdasTest {
  @Test
  public void isLambda_trueForMethodReference() throws ObjectStreamException {
    assertTrue(isLambda((Function<String, String> & Serializable) String::toString));
  }

  @Test
  public void isLambda_trueForLambda() throws ObjectStreamException {
    assertTrue(isLambda((Function<String, String> & Serializable) s -> s));
  }

  @Test
  public void isLambda_falseForGarbage() throws ObjectStreamException {
    assertFalse(isLambda("garbage"));
  }

  @Test
  public void isMethodReference_trueForMethodReference() throws ObjectStreamException {
    assertTrue(isMethodReference((Function<String, String> & Serializable) String::toString));
  }

  @Test
  public void isMethodReference_falseForLambda() throws ObjectStreamException {
    assertFalse(isMethodReference((Function<String, String> & Serializable) s -> s));
  }

  @Test
  public void isMethodReference_falseForGarbage() throws ObjectStreamException {
    assertFalse(isMethodReference("garbage"));
  }

  @Test
  public void getParameterTypes_singleClass() throws ObjectStreamException, ClassNotFoundException {
    assertThat(getParameterTypes((Function<String, String> & Serializable) String::toString))
        .asList().containsExactly(String.class);
  }

  @Test
  public void getParameterTypes_singlePrimitive()
      throws ObjectStreamException, ClassNotFoundException {
    assertThat(getParameterTypes((IntFunction<String> & Serializable) String::valueOf)).asList()
        .containsExactly(int.class);
  }

  @Test
  public void getParameterTypes_singleClassArray()
      throws ObjectStreamException, ClassNotFoundException {
    assertThat(getParameterTypes((Function<String[], String> & Serializable) s -> s[0])).asList()
        .containsExactly(String[].class);
  }

  @Test
  public void getParameterTypes_singlePrimitiveArray()
      throws ObjectStreamException, ClassNotFoundException {
    assertThat(getParameterTypes((Function<int[], String> & Serializable) s -> "")).asList()
        .containsExactly(int[].class);
  }

  interface LongFunction<A, B, C, D, E, R> extends Serializable {
    R apply(A a, B b, C c, D d, E e, long l);
  }

  @Test
  public void getParameterTypes_everything() throws ObjectStreamException, ClassNotFoundException {
    assertThat(getParameterTypes(
        (LongFunction<String, int[], List<Integer>, Pattern, Number[], Object>) (a, b, c, d, e,
            l) -> null)).asList()
                .containsExactly(String.class, int[].class, List.class, Pattern.class,
                    Number[].class, long.class)
                .inOrder();
  }
}
