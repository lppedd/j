package lppedd.j.api.exceptions;

public class JConnectionException extends Exception
{
   private static final long serialVersionUID = 1L;
   
   public JConnectionException(final String message) {
      super(message);
   }
   
   public JConnectionException(final Throwable throwable) {
      super(throwable);
   }

   public JConnectionException(final String message, final Throwable throwable) {
      super(message, throwable);
   }
}
