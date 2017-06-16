package lppedd.j;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.ObjectDoesNotExistException;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static lppedd.j.JConnection.getInstance;
import static lppedd.j.JLibraryList.ListPosition.FIRST;
import static lppedd.j.JLibraryList.ListPosition.LAST;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi library list.
 *
 * @author Edoardo Luppi
 */
public class JLibraryList
{
    public enum ListPosition
    {
        FIRST("*FIRST"),
        LAST("*LAST"),
        AFTER("*AFTER"),
        BEFORE("*BEFORE"),
        REPLACE("*REPLACE");

        private final String _value;

        private ListPosition(final String value) {
            _value = value;
        }

        public String getValue() {
            return _value;
        }
    }

    /**
     * The job to which the library list belong.
     */
    private final Job _job;

    /**
     * The system portion of the library list.
     */
    private String[] _systemPortion = new String[0];

    /**
     * The user portion of the library list.
     */
    private String[] _userPortion = new String[0];

    public JLibraryList(final Job job) {
        _job = job;
        retriveSystemPortion();
        retriveUserPortion();
    }

    public List<String> getUserPortion() {
        return asList(_userPortion);
    }

    public List<String> getSystemPortion() {
        return asList(_systemPortion);
    }

    public boolean addUserLibrary(final String library, final ListPosition position) {
        if (FIRST != position && LAST != position) {
            return false;
        }

        final StringBuilder builder = new StringBuilder(30);
        builder.append("ADDLIBLE LIB(");
        builder.append(library);
        builder.append(") POSITION(");
        builder.append(position.getValue());
        builder.append(")");

        if (checkForMessage("CPC2196", getInstance().executeCommand(builder.toString()))) {
            retriveUserPortion();
            return true;
        }

        return false;
    }

    public boolean addUserLibrary(final String library, final ListPosition position, final String targetLibrary) {
        if (FIRST == position || LAST == position) {
            return false;
        }

        final StringBuilder builder = new StringBuilder(30);
        builder.append("ADDLIBLE LIB(");
        builder.append(library);
        builder.append(") POSITION(");
        builder.append(position.getValue());
        builder.append(" ");
        builder.append(targetLibrary);
        builder.append(")");

        if (checkForMessage("CPC2196", getInstance().executeCommand(builder.toString()))) {
            retriveUserPortion();
            return true;
        }

        return false;
    }

    public boolean replaceUserLibrary(final String library, final String targetLibrary) {
        final StringBuilder builder = new StringBuilder(30);
        builder.append("ADDLIBLE LIB(");
        builder.append(library);
        builder.append(") POSITION(*REPLACE ");
        builder.append(targetLibrary);
        builder.append(")");

        if (checkForMessage("CPC2196", getInstance().executeCommand(builder.toString()))) {
            retriveUserPortion();
            return true;
        }

        return false;
    }

    public boolean removeUserLibrary(final String library) {
        if (checkForMessage("CPC2197", getInstance().executeCommand("RMVLIBLE (" + library + ")"))) {
            retriveUserPortion();
            return true;
        }

        return false;
    }

    public boolean addSystemLibrary(final String library) {
        if (checkForMessage("CPC2196", getInstance().executeCommand("CHGSYSLIBL LIB(" + library + ") OPTION(*ADD)"))) {
            retriveSystemPortion();
            return true;
        }

        return false;
    }

    public boolean removeSystemLibrary(final String library) {
        if (checkForMessage("CPC2197", getInstance().executeCommand("CHGSYSLIBL LIB(" + library + ") OPTION(*REMOVE)"))) {
            retriveSystemPortion();
            return true;
        }

        return false;
    }

    private void retriveUserPortion() {
        try {
            _job.loadInformation();
            _userPortion = _job.getUserLibraryList();
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    private void retriveSystemPortion() {
        try {
            _job.loadInformation();
            _systemPortion = _job.getSystemLibraryList();
        } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(_userPortion.length + _systemPortion.length * 10);

        for (final String library : _systemPortion) {
            builder.append(library);
            builder.append(", ");
        }

        for (final String library : _userPortion) {
            builder.append(library);
            builder.append(", ");
        }

        final int length = builder.length();
        builder.delete(length - 2, length);

        return builder.toString();
    }
}
