package lppedd.j.commands;

import lppedd.j.JJournal;
import lppedd.j.JPhysicalFile;
import lppedd.j.interfaces.JCommand;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class STRJRNPF implements JCommand<Boolean>
{
    public static final String IMAGES_AFTER = "*AFTER";
    public static final String IMAGES_BOTH = "*BOTH";

    public static final String OMTJRNE_NONE = "*NONE";
    public static final String OMTJRNE_OPNCLO = "*OPNCLO";

    public static final String LOGLVL_ALL = "*ALL";
    public static final String LOGLVL_ERRORS = "*ERRORS";

    private JPhysicalFile _file;
    private JJournal _journal;
    private String _images = IMAGES_AFTER;
    private String _omittedEntires = OMTJRNE_NONE;
    private String _logLevel = LOGLVL_ALL;

    @Override
    public Boolean execute() {
        final StringBuilder builder = new StringBuilder(85);
        builder.append("STRJRNPF FILE(");
        builder.append(_file.getLibrary());
        builder.append("/");
        builder.append(_file.getName());
        builder.append(") JRN(");
        builder.append(_journal.getLibrary());
        builder.append("/");
        builder.append(_journal.getName());
        builder.append(") IMAGES(");
        builder.append(_images);
        builder.append(") OMTJRNE(");
        builder.append(_omittedEntires);
        builder.append(") LOGLVL(");
        builder.append(_logLevel);
        builder.append(")");

        return checkForMessage("CPC7031", getInstance().executeCommand(builder.toString()));
    }

    public JPhysicalFile getFile() {
        return _file;
    }

    public JJournal getJournal() {
        return _journal;
    }

    public String getImages() {
        return _images;
    }

    public String getOmittedEntries() {
        return _omittedEntires;
    }

    public String getLogLevel() {
        return _logLevel;
    }

    public void setFile(final JPhysicalFile file) {
        _file = file;
    }

    public void setJournal(final JJournal jrn) {
        _journal = jrn;
    }

    public void setImages(final String images) {
        _images = images;
    }

    public void setOmittedEntries(final String omtjrne) {
        _omittedEntires = omtjrne;
    }

    public void setLogLevel(final String loglvl) {
        _logLevel = loglvl;
    }
}
