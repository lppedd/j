package lppedd.j;

import java.util.ArrayList;
import java.util.List;
import lppedd.j.abstracts.JAbstractDeviceFile;
import lppedd.j.enums.JType;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.JAPI.QSRLSAVF;
import static lppedd.j.enums.JType.FILE;
import static lppedd.j.enums.JType.valueOf;
import static lppedd.j.misc.JUtil.checkForMessage;

public class JSaveFile extends JAbstractDeviceFile
{
    public static class JSavedObject
    {
        private final String _name;
        private final String _library;
        private final JType _type;
        private final String _attribute;
        private final String _text;
        private final List<String> _members;

        private JSavedObject(final String name, final String library, final JType type, final String attribute, final String text, final List<String> members) {
            _name = name;
            _library = library;
            _type = type;
            _attribute = attribute;
            _text = text;
            _members = members;
        }

        public String getName() {
            return _name;
        }

        public String getLibrary() {
            return _library;
        }

        public JType getType() {
            return _type;
        }

        public String getAttribute() {
            return _attribute;
        }

        public String getText() {
            return _text;
        }

        public List<String> getMembers() {
            return _members;
        }
    }

    protected JSaveFile(final String name, final String library) {
        super(name, library);
    }

    public List<JSavedObject> getSavedObjects() {
        // Recupero gli oggetti.
        JAPIOutput output = QSRLSAVF("SAVF0200", "*ALL", "*ALL", this);

        if (output.getMessages().length != 0) {
            return EMPTY_LIST;
        }

        final byte[] objectsBytes = output.getValue();
        int objectsOffset = byteArrayToInt(objectsBytes, 124);
        int objectsCount = byteArrayToInt(objectsBytes, 132);
        final int objectEntrySize = byteArrayToInt(objectsBytes, 136);
        final List<JSavedObject> savedObjects = new ArrayList<>(objectsCount);

        for (; objectsCount > 0; --objectsCount, objectsOffset += objectEntrySize) {
            final String savedObjectName = ((String) CHAR10.toObject(objectsBytes, objectsOffset)).trim();
            final JType savedObjectType = valueOf(((String) CHAR10.toObject(objectsBytes, objectsOffset + 20)).trim().substring(1));

            // Lista vuota, dato che l'oggetto potrebbe non essere un file, e quindi contenere membri.
            List<String> savedObjectMembers = EMPTY_LIST;

            // Se l'oggetto e' un file, reperisco i suoi membri.
            if (FILE == savedObjectType) {
                output = QSRLSAVF("SAVF0300", savedObjectName, "*ALL", this);

                if (output.getMessages().length != 0) {
                    continue;
                }

                final byte[] membersBytes = output.getValue();
                int membersOffset = byteArrayToInt(membersBytes, 124);
                int membersCount = byteArrayToInt(membersBytes, 132);
                final int memberEntrySize = byteArrayToInt(membersBytes, 136);

                // Riassegno alla lista una nuova lista con dimensione iniziale uguale al numero di membri.
                savedObjectMembers = new ArrayList<>(membersCount);

                for (; membersCount > 0; --membersCount, membersOffset += memberEntrySize) {
                    savedObjectMembers.add(((String) CHAR10.toObject(membersBytes, membersOffset + 20)).trim());
                }
            }

            savedObjects.add(new JSavedObject(
                    savedObjectName,
                    ((String) CHAR10.toObject(objectsBytes, objectsOffset + 10)).trim(),
                    savedObjectType,
                    ((String) CHAR10.toObject(objectsBytes, objectsOffset + 30)).trim(),
                    ((String) CHAR50.toObject(objectsBytes, objectsOffset + 154)).trim(),
                    savedObjectMembers));
        }

        return savedObjects;
    }

    @Override
    protected boolean performSetText() {
        final StringBuilder builder = new StringBuilder(144);
        builder.append("CHGSAVF FILE(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") TEXT('");
        builder.append(_text);
        builder.append("')");

        return checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
    }
}
