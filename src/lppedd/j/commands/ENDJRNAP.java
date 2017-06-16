package lppedd.j.commands;

import lppedd.j.JConnection;
import lppedd.j.JJournal;
import lppedd.j.JLogicalFile;
import lppedd.j.interfaces.JCommand;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class ENDJRNAP implements JCommand<Boolean>
{
    public static final String LOGLVL_ALL = "*ALL";
    public static final String LOGLVL_ERRORS = "*ERRORS";

    private JLogicalFile[] _files;
    private JJournal _journal;
    private String _logLevel = LOGLVL_ERRORS;

    @Override
    public Boolean execute() {
        final JConnection connection = getInstance();
        final StringBuilder builder = new StringBuilder(85);
        builder.append("ENDJRNAP FILE(");

        for (final JLogicalFile file : _files) {
            builder.append(file.getLibrary());
            builder.append("/");
            builder.append(file.getName());
            builder.append(" ");
        }

        builder.deleteCharAt(builder.length());
        builder.append(") JRN(");
        builder.append(_journal.getLibrary());
        builder.append("/");
        builder.append(_journal.getName());
        builder.append(") LOGLVL(");
        builder.append(_logLevel);
        builder.append(")");

        return checkForMessage("CPC7031", connection.executeCommand(builder.toString()));
    }

    public JLogicalFile[] getFiles() {
        return _files;
    }

    public JJournal getJournal() {
        return _journal;
    }

    public String getLogLevel() {
        return _logLevel;
    }

    public void setFiles(final JLogicalFile... files) {
        _files = files;
    }

    public void setJournal(final JJournal journal) {
        _journal = journal;
    }

    public void setLogLevel(final String logLevel) {
        _logLevel = logLevel;
    }
}
