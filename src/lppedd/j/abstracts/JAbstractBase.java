package lppedd.j.abstracts;

import lppedd.j.JConnection;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.AS400DataTypes;
import lppedd.j.interfaces.JBase;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.enums.JType.NONE;

/**
 * Represents every IBMi object.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractBase implements JBase, AS400DataTypes
{
    // Connessione AS400 al quale l'oggetto appartiene
    private static final JConnection _CONNECTION = getInstance();

    // Path dell'oggetto
    protected String _name = "";
    protected String _library = "";

    // Attributi dell'oggetto
    protected String _originalName = "";
    protected JType _type = NONE;
    protected String _attribute = "";
    protected String _text = "";
    protected String _creator = "";
    protected long _creationDateTime;

    // Indicatori di stato dell'oggetto
    protected boolean _changedName;
    protected boolean _changedAttribute;
    protected boolean _changedText;
    protected boolean _changedSource;

    // Formato MBRD0300 -> QUSRMBRD
    protected byte[] _MBRD0300;

    protected JAbstractBase(final String name, final String library, final JType type) {
        _name = name.trim().toUpperCase();
        _library = library.trim().toUpperCase();
        _type = type;
        _originalName = _name;
    }

    @Override
    public boolean commitChanges() {
        if (!exists()) {
            return false;
        }

        if (_changedName) {
            if (!performSetName()) {
                return false;
            }

            _originalName = getName();
            _changedName = false;
        }

        if (_changedText) {
            if (!performSetText()) {
                return false;
            }

            _changedText = false;
        }

        return true;
    }

    @Override
    public String getLibrary() {
        return _library;
    }

    @Override
    public JType getType() {
        return _type;
    }

    @Override
    public String getAttribute() {
        return _attribute;
    }

    @Override
    public String getText() {
        return _text;
    }

    @Override
    public String getCreator() {
        return _creator;
    }

    @Override
    public long getCreationDateTime() {
        return _creationDateTime;
    }

    @Override
    public JConnection getConnection() {
        return _CONNECTION;
    }

    @Override
    public void setName(String name) {
        name = name.trim().toUpperCase();

        if (!_name.equalsIgnoreCase(name)) {
            _name = name;
            _changedName = true;
        }
    }

    @Override
    public void setText(final String text) {
        if (!_text.equalsIgnoreCase(text)) {
            _text = text;
            _changedText = true;
        }
    }

    /**
     * Lancia il comando AS400 per rendere effettiva la modifica del nome
     * dell'oggetto
     */
    protected abstract boolean performSetName();

    /**
     * Lancia il comando AS400 per rendere effettiva la modifica del testo
     * descrittivo dell'oggetto
     */
    protected abstract boolean performSetText();

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof JAbstractBase)) {
            return false;
        }

        return getPath().equals(((JAbstractBase) object).getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }
}
