package lppedd.j;

import lppedd.j.abstracts.JAbstractObject;

import static lppedd.j.enums.JType.PGM;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi program.
 *
 * @author Edoardo Luppi
 */
public class JProgram extends JAbstractObject
{
   public JProgram(final String name, final String library) {
      super(name, library, PGM);
   }

   @Override
   public boolean delete() {
      return checkForMessage("CPC2191", getConnection().executeCommand("DLTPGM PGM(" + _library + "/" + _name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGPGM PGM(");
      builder.append(_library);
      builder.append("/");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC0540", getConnection().executeCommand(builder.toString()));
   }
}
