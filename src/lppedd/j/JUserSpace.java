package lppedd.j;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.UserSpace;
import java.beans.PropertyVetoException;
import java.io.IOException;
import lppedd.j.abstracts.JAbstractObject;

import static com.ibm.as400.access.QSYSObjectPathName.toPath;
import static lppedd.j.JAPI.QUSDLTUS;
import static lppedd.j.enums.JType.USRSPC;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class JUserSpace extends JAbstractObject
{
    protected final UserSpace _userSpace;
    protected final int _length;
    protected final boolean _autoExtendible;
    protected final byte _initialValue;

    private JUserSpace(final JUserSpaceBuilder builder) {
        super(builder._name, builder._library, USRSPC);

        _length = builder._length;
        _autoExtendible = builder._autoExtendible;
        _initialValue = builder._initialValue;
        _userSpace = new UserSpace();

        // Setting these properties to true unsure everything is run on the same job,
        // so using QTEMP should be safe.
        _userSpace.setMustUseProgramCall(true);
        _userSpace.setMustUseSockets(true);

        try {
            _userSpace.setSystem(getConnection().getAS400());
            _userSpace.setPath(toPath(builder._library, builder._name, _type.getObjectType().substring(1)));
        } catch (final PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public boolean create() {
        try {
            _userSpace.create(_length, false, "", (byte) 0x00, _text, "*ALL");

            // Not always calling setAutoExtendible() to avoid a possibile exception.
            if (_autoExtendible) {
                _userSpace.setAutoExtendible(true);
            }

            return true;
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String read(final int start, final int length) {
        try {
            return _userSpace.read(start, length);
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }

        return "";
    }

    public int read(final byte[] buffer, final int bufferStart, final int start, final int length) {
        try {
            return _userSpace.read(buffer, start, bufferStart, length);
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int getLength() {
        try {
            return _userSpace.getLength();
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public UserSpace getHandle() {
        return _userSpace;
    }

    @Override
    public boolean delete() {
        return QUSDLTUS(this);
    }

    @Override
    protected boolean performSetText() {
        final StringBuilder builder = new StringBuilder(90);
        builder.append("CHGOBJD OBJ(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") OBJTYPE(");
        builder.append(_type.getObjectType());
        builder.append(") TEXT('");
        builder.append(_text);
        builder.append("')");

        return checkForMessage("CPC2103", getConnection().executeCommand(builder.toString()));
    }

    public static class JUserSpaceBuilder
    {
        private final String _name;
        private final String _library;
        private int _length;
        private boolean _autoExtendible;
        private byte _initialValue;

        public JUserSpaceBuilder(final String name, final String library) {
            _name = name;
            _library = library;
        }

        public JUserSpaceBuilder length(final int length) {
            _length = length;
            return this;
        }

        public JUserSpaceBuilder autoExtendible(final boolean autoExtendible) {
            _autoExtendible = autoExtendible;
            return this;
        }

        public JUserSpaceBuilder initialValue(final byte initialValue) {
            _initialValue = initialValue;
            return this;
        }

        public JUserSpace build() {
            return new JUserSpace(this);
        }
    }
}
