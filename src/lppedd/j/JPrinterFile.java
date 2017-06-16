package lppedd.j;

import lppedd.j.abstracts.JAbstractDeviceFile;

import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * Rapresents an IBMi printer file object.
 *
 * @author Edoardo Luppi
 */
public class JPrinterFile extends JAbstractDeviceFile
{
   protected JPrinterFile(final String name, final String library) {
      super(name, library);
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGPRTF FILE(");
      builder.append(_library);
      builder.append("/");
      builder.append(_name);
      builder.append(") TEXT('");
      builder.append(_text);
      builder.append("')");

      return checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
   }
}
