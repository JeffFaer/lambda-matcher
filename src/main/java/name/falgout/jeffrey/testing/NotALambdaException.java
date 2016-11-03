package name.falgout.jeffrey.testing;

import java.io.Serializable;

public final class NotALambdaException extends IllegalArgumentException {
  private static final long serialVersionUID = 7659856359072887880L;
  private final Serializable notALambda;

  public NotALambdaException(Serializable notALambda) {
    super(notALambda + " is not a lambda.");
    this.notALambda = notALambda;
  }

  public Serializable getNotALambda() {
    return notALambda;
  }
}
