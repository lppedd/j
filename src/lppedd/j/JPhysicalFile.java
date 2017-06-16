package lppedd.j;

import com.ibm.as400.access.AS400Message;
import java.util.ArrayList;
import java.util.List;
import lppedd.j.abstracts.JAbstractDatabaseFile;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static com.ibm.as400.access.BinaryConverter.byteArrayToUnsignedShort;
import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.JObjectFactory.get;
import static lppedd.j.enums.JType.PGM;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi physical file (PF and SQL).
 *
 * @author Edoardo Luppi
 */
public class JPhysicalFile extends JAbstractDatabaseFile
{
    /**
     * Physical file maximum number of records.
     */
    protected static final int MAX_DIMENSION = 2147483646;

    public JPhysicalFile(final String name, final String library) {
        super(name, library);
    }

    /**
     * Copy all the records to another physical file.<br>
     * By default it uses <code>FMTOPT(*MAP *DROP)</code>
     *
     * @param physicalFile The target physical file
     */
    public boolean copyFile(final JPhysicalFile physicalFile) {
        final StringBuilder builder = new StringBuilder(136);
        builder.append("CPYF FROMFILE(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") TOFILE(");
        builder.append(physicalFile._library);
        builder.append("/");
        builder.append(physicalFile._name);
        builder.append(") FROMMBR(*ALL) TOMBR(*FROMMBR) MBROPT(*ADD) FMTOPT(*MAP *DROP)");

        final AS400Message[] messages = getConnection().executeCommand(builder.toString());
        return checkForMessage("CPC2955", messages) || checkForMessage("CPC2957", messages);
    }

    /**
     * Changes the physical file size.
     *
     * @param newSize The new size.
     */
    public boolean changeSize(final int newSize) {
        if (newSize < 1 || newSize > MAX_DIMENSION) {
            return false;
        }

        final StringBuilder builder = new StringBuilder(100);
        builder.append("CHGPF FILE(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") SIZE(");
        builder.append(newSize);
        builder.append(")");

        return checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
    }

    /**
     * Add a trigger to the physical file.
     *
     * @param trigger The trigger to be added
     */
    public boolean addTrigger(final JTrigger trigger) {
        return checkForMessage("CPC3203", getConnection().executeCommand("ADDPFTRG " + trigger.buildString()));
    }

    /**
     * Returns the triggers attached to the physical file.
     */
    public List<JTrigger> getTriggers() {
        // Find Qdb_Qdbfphys offset
        int offset = byteArrayToInt(_FILD0100, 364);

        if (offset > 0) {
            // Find the number of triggers
            final int triggerCount = byteArrayToUnsignedShort(_FILD0100, offset + 28);

            if (triggerCount > 0) {
                final List<JTrigger> triggers = new ArrayList<>(triggerCount);

                // Find Qdb_Qdbftrg offset
                offset = byteArrayToInt(_FILD0100, offset + 24);

                for (int i = 0; i < triggerCount; i++) {
                    if ((_FILD0100[offset + 23] & 0x02) != 0) {
                        // Analyze the FILD0400 format for more accurate information about the trigger
                        // TODO
                    }

                    final JProgram program = get((String) CHAR10.toObject(_FILD0100, offset + 2), (String) CHAR10.toObject(_FILD0100, offset + 12), PGM);

                    final String time = getTimeFromAPI((String) CHAR1.toObject(_FILD0100, offset));
                    final String event = getEventFromAPI((String) CHAR1.toObject(_FILD0100, offset + 1));

                    triggers.add(new JTrigger(this, time, event, program));

                    // Move to the next trigger
                    offset = offset + 48;
                }

                return triggers;
            }
        }

        return EMPTY_LIST;
    }

    /**
     * Returns if the physical file is a source file.
     */
    public boolean isSourceFile() {
        return (_FILD0100[8] & 0x08) != 0;
    }

    /**
     * Returns if the physical file has multiple members.
     */
    public boolean isMultiMember() {
        return byteArrayToUnsignedShort(_FILD0100, 41) != 1;
    }

    @Override
    protected boolean performSetText() {
        final StringBuilder builder = new StringBuilder(144);
        builder.append("CHGPF FILE(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") TEXT('");
        builder.append(_text);
        builder.append("')");

        return checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
    }

    protected final String getTimeFromAPI(final String string) {
        String time = "";

        switch (string) {
            case "1":
                time = "*AFTER";
                break;
            case "2":
                time = "*BEFORE";
                break;
            case "3":
                time = "*INSTEAD";
                break;
            default:
                break;
        }

        return time;
    }

    protected final String getEventFromAPI(final String string) {
        String event = "";

        switch (string) {
            case "1":
                event = "*INSERT";
                break;
            case "2":
                event = "*DELETE";
                break;
            case "3":
                event = "*UPDATE";
                break;
            case "4":
                event = "*READ";
                break;
            default:
                break;
        }

        return event;
    }
}
