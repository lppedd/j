package smi.workitem;

import java.util.ArrayList;
import java.util.List;
import lppedd.j.exceptions.JInvalidWorkItemException;
import lppedd.j.interfaces.JMember;
import smi.workitem.abstracts.SMIWorkItem;

/**
 * @author Edoardo Luppi
 */
public class SMIPgmWorkItem extends SMIWorkItem
{
    public SMIPgmWorkItem(final JMember parent, final String line) throws JInvalidWorkItemException {
        super(parent, line);
    }

    public SMIPgmWorkItem(
            final JMember parent,
            final int index,
            final int number,
            final String username,
            final long date,
            final boolean work) {
        super(parent, index, number, username, date, work);
    }

    @Override
    public List<String> getSource() {
        final StringBuilder builder = new StringBuilder(120);
        builder.append(_index);
        builder.append(_work ? "§" : " ");
        builder.append(" * ");
        builder.append(_user);
        builder.append(" ");
        builder.append(_date);
        builder.append(" WORKITEM ");
        builder.append(_number);

        final List<String> source = new ArrayList<>(1 + _text.size());
        source.add(builder.toString());

        builder.replace(0, 4, "    ");

        for (int i = 0; i < _text.size(); i++) {
            builder.replace(8, builder.length(), _text.get(i));
            source.add(builder.toString());
        }

        return source;
    }

    @Override
    public String[] getElements(final String line) {
        final String[] elements = new String[5];
        elements[INDEX] = line.substring(0, 4);
        elements[WORK] = line.substring(4, 5);
        elements[USER] = line.substring(8, 14);
        elements[DATE] = line.substring(15, 23);
        elements[NUMBER] = line.substring(33, 43).trim();

        return elements;
    }
}
