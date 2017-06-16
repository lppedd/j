package lppedd.j;

import lppedd.j.abstracts.JAbstractObject;

import static lppedd.j.enums.JType.MODULE;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi module.
 *
 * @author Edoardo Luppi
 */
public class JModule extends JAbstractObject
{
   public JModule(final String name, final String library) {
      super(name, library, MODULE);
   }

   @Override
   public boolean delete() {
      return checkForMessage("CPC2191", getConnection().executeCommand("DLTMOD MODULE(" + _library + "/" + _name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(148);
      builder.append("CHGMOD MODULE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC5D0D", getConnection().executeCommand(builder.toString()));
   }
}
