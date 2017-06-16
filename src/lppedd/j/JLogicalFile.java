package lppedd.j;

import lppedd.j.abstracts.JAbstractDatabaseFile;

import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Represents an IBMi logical file (LF and SQL).
 *
 * @author Edoardo Luppi
 */
public class JLogicalFile extends JAbstractDatabaseFile
{
   public JLogicalFile(final String name, final String library) {
      super(name, library);
   }

   /**
    * Returns if the logical file combines fields from two or more physical
    * files.
    */
   public boolean hasJoinMember() {
      return "1".equals(CHAR1.toObject(_MBRD0300, 266));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGLF FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
   }
}
