package smi.workitem;

import java.util.ArrayList;
import java.util.List;
import lppedd.j.exceptions.JInvalidWorkItemException;
import lppedd.j.interfaces.JMember;
import smi.workitem.abstracts.SMIWorkItem;

/**
 * @author Edoardo Luppi
 */
public class SMISqlWorkItem extends SMIWorkItem
{
   public SMISqlWorkItem(final JMember parent, final String line) throws JInvalidWorkItemException {
      super(parent, line);
   }

   public SMISqlWorkItem(
           final JMember parent,
           final int index,
           final int number,
           final String user,
           final long date,
           final boolean work) {
      super(parent, index, number, user, date, work);
   }

   @Override
   public List<String> getSource() {
      final StringBuilder builder = new StringBuilder(120);
      builder.append("--");
      builder.append(_index);
      builder.append(_work ? "§" : " ");
      builder.append("* ");
      builder.append(_user);
      builder.append(" ");
      builder.append(_date);
      builder.append(" WORKITEM ");
      builder.append(_number);

      final List<String> source = new ArrayList<>(1 + _text.size());
      source.add(builder.toString());

      builder.replace(2, 6, "    ");

      for (int i = 0; i < _text.size(); i++) {
         builder.replace(9, builder.length(), _text.get(i));
         source.add(builder.toString());
      }

      return source;
   }

   @Override
   public String[] getElements(final String line) {
      final String[] elements = new String[5];
      elements[INDEX] = line.substring(2, 6);
      elements[WORK] = line.substring(6, 7);
      elements[USER] = line.substring(9, 15);
      elements[DATE] = line.substring(16, 24);
      elements[NUMBER] = line.substring(34, 44).trim();

      return elements;
   }
}
