package name.falgout.jeffrey.testing;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Lambdas {
  private static final String PRIMITIVE = "[ZBCSIJFD]";
  private static final String CLASS = "L[^;]+;";
  private static final Pattern SINGLE_PARAMETER_PATTERN = Pattern.compile(or(PRIMITIVE, CLASS));
  private static final Pattern PARAMETER_LIST_PATTERN = Pattern.compile("\\((?<parameters>.*)\\)");

  private static String or(String... patterns) {
    StringJoiner joiner = new StringJoiner(")|(?:", "(?:", ")");
    for (String pattern : patterns) {
      joiner.add(pattern);
    }

    return joiner.toString();
  }

  private static final Map<String, Class<?>> PRIMITIVE_CLASSES;
  static {
    Map<String, Class<?>> classes = new LinkedHashMap<>();
    classes.put("Z", boolean.class);
    classes.put("B", byte.class);
    classes.put("C", char.class);
    classes.put("S", short.class);
    classes.put("I", int.class);
    classes.put("J", long.class);
    classes.put("F", float.class);
    classes.put("D", double.class);

    PRIMITIVE_CLASSES = Collections.unmodifiableMap(classes);
  }

  private Lambdas() {}

  public static boolean isLambda(Serializable lambda) throws ObjectStreamException {
    return getLambda(lambda).isPresent();
  }

  public static boolean isMethodReference(Serializable methodReference)
      throws ObjectStreamException {
    Optional<SerializedLambda> lambda = getLambda(methodReference);
    if (!lambda.isPresent()) {
      return false;
    }

    SerializedLambda actualLambda = lambda.get();
    return !actualLambda.getImplMethodName().contains("lambda$");
  }

  public static Class<?>[] getParameterTypes(Serializable lambda)
      throws ObjectStreamException, ClassNotFoundException {
    return getParameterTypes(lambda, Thread.currentThread().getContextClassLoader());
  }

  public static Class<?>[] getParameterTypes(Serializable lambda, ClassLoader classLoader)
      throws ObjectStreamException, ClassNotFoundException {
    SerializedLambda actualLambda = getLambdaOrThrow(lambda);
    String methodSignature = actualLambda.getInstantiatedMethodType();

    Matcher matcher = PARAMETER_LIST_PATTERN.matcher(methodSignature);
    matcher.find();

    String parameters = matcher.group("parameters");
    List<String> parameterClassNames = new ArrayList<>();
    matcher = SINGLE_PARAMETER_PATTERN.matcher(parameters);
    int findIndex = 0;
    while (matcher.find(findIndex)) {
      parameterClassNames.add(parameters.substring(findIndex, matcher.end()).replace('/', '.'));

      findIndex = matcher.end();
    }

    Class<?>[] parameterTypes = new Class<?>[parameterClassNames.size()];
    for (int i = 0; i < parameterTypes.length; i++) {
      String parameterClassName = parameterClassNames.get(i);
      if (parameterClassName.startsWith("[")) {
        parameterTypes[i] = Class.forName(parameterClassName, true, classLoader);
      } else if (!parameterClassName.startsWith("L")) {
        if (!PRIMITIVE_CLASSES.containsKey(parameterClassName)) {
          throw new Error("Unknown class: " + parameterClassName);
        }

        parameterTypes[i] = PRIMITIVE_CLASSES.get(parameterClassName);
      } else /* parameterClassName.startsWith("L") */ {
        // Remove L and ;
        parameterClassName = parameterClassName.substring(1, parameterClassName.length() - 1);
        parameterTypes[i] = Class.forName(parameterClassName, true, classLoader);
      }
    }

    return parameterTypes;
  }

  public static String getMethodName(Serializable lambda) throws ObjectStreamException {
    return getLambdaOrThrow(lambda).getImplMethodName();
  }

  public static List<? extends Object> getCapturedArguments(Serializable lambda)
      throws ObjectStreamException {
    SerializedLambda actualLambda = getLambdaOrThrow(lambda);
    return new AbstractList<Object>() {
      @Override
      public Object get(int index) {
        return actualLambda.getCapturedArg(index);
      }

      @Override
      public int size() {
        return actualLambda.getCapturedArgCount();
      }
    };
  }

  private static SerializedLambda getLambdaOrThrow(Serializable lambda)
      throws ObjectStreamException {
    return getLambda(lambda).orElseThrow(() -> new NotALambdaException(lambda));
  }

  private static Optional<SerializedLambda> getLambda(Serializable lambda)
      throws ObjectStreamException {
    for (Class<?> clazz = lambda.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
      try {
        Method method = clazz.getDeclaredMethod("writeReplace");
        if (!isSerializableWriteReplace(method)) {
          continue;
        }

        method.setAccessible(true);
        Object replacement = method.invoke(lambda);

        if (!(replacement instanceof SerializedLambda)) {
          return Optional.empty();
        }

        return Optional.of((SerializedLambda) replacement);
      } catch (NoSuchMethodException e) {
        // Try a superclass.
      } catch (IllegalAccessException e) {
        throw new AssertionError("We made it accessible", e);
      } catch (IllegalArgumentException e) {
        throw new AssertionError("writeReplace doesn't take any arguments", e);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ObjectStreamException) {
          throw (ObjectStreamException) cause;
        } else if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
          throw (Error) cause;
        } else {
          throw new AssertionError("writeReplace can only throw ObjectStreamException", e);
        }
      }
    }

    return Optional.empty();
  }

  /**
   * Checks the given method to see if it conforms to Serializable's requirements on writeReplace.
   */
  private static boolean isSerializableWriteReplace(Method method) {
    if (method.getParameterCount() > 0) {
      return false;
    }
    for (Class<?> exception : method.getExceptionTypes()) {
      if (RuntimeException.class.isAssignableFrom(exception)
          || Error.class.isAssignableFrom(exception)
          || ObjectStreamException.class.isAssignableFrom(exception)) {
        continue;
      } else {
        return false;
      }
    }

    return true;
  }
}
