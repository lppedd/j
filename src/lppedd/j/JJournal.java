package lppedd.j;

import lppedd.j.abstracts.JAbstractObject;

import static lppedd.j.enums.JType.JRN;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi journal.
 *
 * @author Edoardo Luppi
 */
public class JJournal extends JAbstractObject
{
    public JJournal(final String name, final String library) {
        super(name, library, JRN);
    }

    @Override
    public boolean delete() {
        return checkForMessage("CPC2191", getConnection().executeCommand("DLTJRN JRN(" + _library + "/" + _name + ")"));
    }

    @Override
    protected boolean performSetText() {
        final StringBuilder builder = new StringBuilder(144);
        builder.append("CHGJRN JRN(");
        builder.append(_library);
        builder.append("/");
        builder.append(_name);
        builder.append(") TEXT('");
        builder.append(_text);
        builder.append("')");

        // FIXME: cambiare il codice del messaggio
        return checkForMessage("CPC0540", getConnection().executeCommand(builder.toString()));
    }
}
