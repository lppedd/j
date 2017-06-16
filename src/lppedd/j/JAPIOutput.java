package lppedd.j;

import com.ibm.as400.access.AS400Message;

import static lppedd.misc.EmptyArrays.EMPTY_BYTE;

/**
 * Represents the output value of an IBMi API call.
 *
 * @author Edoardo Luppi
 */
public class JAPIOutput
{
    private final byte[] _value;
    private final AS400Message[] _messages;

    public JAPIOutput(final byte[] value, final AS400Message[] messages) {
        _value = value;
        _messages = messages;
    }

    public JAPIOutput(final byte[] value) {
        _value = value;
        _messages = new AS400Message[0];
    }

    public JAPIOutput(final byte[] value, final AS400Message message) {
        this(value, new AS400Message[]{
            message
        });
    }

    public JAPIOutput(final AS400Message[] messages) {
        this(new byte[0], messages);
    }

    public JAPIOutput(final AS400Message message) {
        this(EMPTY_BYTE, message);
    }

    public byte[] getValue() {
        return _value;
    }

    public AS400Message[] getMessages() {
        return _messages;
    }
}
