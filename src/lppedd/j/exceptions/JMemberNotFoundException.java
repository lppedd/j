package lppedd.j.exceptions;

/**
 *
 * @author Edoardo Luppi
 *
 */
public class JMemberNotFoundException extends Exception
{
    private static final long serialVersionUID = -1032240693611174352L;

    public JMemberNotFoundException() {
        super();
    }

    public JMemberNotFoundException(final String message) {
        super(message);
    }

    public JMemberNotFoundException(final Throwable cause) {
        super(cause);
    }

    public JMemberNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
