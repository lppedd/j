package lppedd.j;

import java.util.List;
import lppedd.j.interfaces.JFile;

/**
 * @author Edoardo Luppi
 */
public class JRecordFormat
{
    private final JFile _parent;
    private final String _name;
    private final String _description;
    private final List<JField> _fields;

    public JRecordFormat(final JFile parent, final String name, final String description, final List<JField> fields) {
        _parent = parent;
        _name = name;
        _description = description;
        _fields = fields;
    }

    public JFile getParent() {
        return _parent;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public List<JField> getFields() {
        return _fields;
    }

    @Override
    public String toString() {
        return _name;
    }
}
