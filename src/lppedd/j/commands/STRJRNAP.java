package lppedd.j.commands;

import lppedd.j.JJournal;
import lppedd.j.JLogicalFile;
import lppedd.j.interfaces.JCommand;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public final class STRJRNAP implements JCommand<Boolean>
{
   public static final String LOGLVL_ALL = "*ALL";
   public static final String LOGLVL_ERRORS = "*ERRORS";

   private JLogicalFile _file;
   private JJournal _journal;
   private String _logLevel = LOGLVL_ALL;

   @Override
   public Boolean execute() {
      final StringBuilder builder = new StringBuilder(85);
      builder.append("STRJRNAP FILE(");
      builder.append(_file.getLibrary());
      builder.append("/");
      builder.append(_file.getName());
      builder.append(") JRN(");
      builder.append(_journal.getLibrary());
      builder.append("/");
      builder.append(_journal.getName());
      builder.append(") LOGLVL(");
      builder.append(_logLevel);
      builder.append(")");

      return checkForMessage("CPC7031", getInstance().executeCommand(builder.toString()));
   }

   public JLogicalFile getFile() {
      return _file;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public void setFile(final JLogicalFile file) {
      _file = file;
   }

   public void setLogLevel(final String logLevel) {
      _logLevel = logLevel;
   }
}
