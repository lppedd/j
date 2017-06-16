package lppedd.j;

import lppedd.j.abstracts.JAbstractObject;

import static lppedd.j.enums.JType.SRVPGM;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi service program.
 *
 * @author Edoardo Luppi
 */
public class JServiceProgram extends JAbstractObject
{
   public JServiceProgram(final String name, final String library) {
      super(name, library, SRVPGM);
   }

   @Override
   public boolean delete() {
      return checkForMessage("CPC2191", getConnection().executeCommand("DLTSRVPGM SRVPGM(" + _library + "/" + _name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(152);
      builder.append("CHGSRVPGM SRVPGM(");
      builder.append(_library);
      builder.append("/");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC5D11", getConnection().executeCommand(builder.toString()));
   }
}
