package lppedd.j.exceptions;

/**
 *
 * @author Edoardo Luppi
 *
 */
public class JObjectNotFoundException extends Exception
{
    private static final long serialVersionUID = -4769363580549228399L;

    public JObjectNotFoundException() {
        super();
    }

    public JObjectNotFoundException(final String message) {
        super(message);
    }

    public JObjectNotFoundException(final Throwable cause) {
        super(cause);
    }

    public JObjectNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
