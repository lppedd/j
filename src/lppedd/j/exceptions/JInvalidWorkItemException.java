package lppedd.j.exceptions;

/**
 * @author Edoardo Luppi
 */
public class JInvalidWorkItemException extends Exception
{
   private static final long serialVersionUID = -1412545237788334159L;

   public JInvalidWorkItemException() {
      super();
   }

   public JInvalidWorkItemException(final String message) {
      super(message);
   }

   public JInvalidWorkItemException(final Throwable cause) {
      super(cause);
   }

   public JInvalidWorkItemException(final String message, final Throwable cause) {
      super(message, cause);
   }
}
