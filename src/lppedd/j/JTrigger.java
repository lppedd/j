package lppedd.j;

import lppedd.j.interfaces.JDatabaseFile;
import lppedd.j.interfaces.JVirtual;

/**
 * Contains an IBMi trigger attributes.
 *
 * @author Edoardo Luppi
 * @see JVirtual
 */
public class JTrigger implements JVirtual
{
   private final JDatabaseFile _parent;
   private final String _time;
   private final String _event;
   private final JProgram _program;

   // Optional values
   private String _replace = "*NO";
   private String _name = "*GEN";
   private String _library = "*FILE";

   public JTrigger(final JDatabaseFile parent, final String time, final String event, final JProgram program) {
      _parent = parent;
      _time = time;
      _event = event;
      _program = program;
   }

   public JDatabaseFile getParent() {
      return _parent;
   }

   public JProgram getProgram() {
      return _program;
   }

   public String getTime() {
      return _time;
   }

   public String getEvent() {
      return _event;
   }

   public String getReplace() {
      return _replace;
   }

   public String getName() {
      return _name;
   }

   public String getLibrary() {
      return _library;
   }

   public void setReplace(final String replace) {
      _replace = replace;
   }

   public void setName(final String name) {
      _name = name;
   }

   public void setLibrary(final String library) {
      _library = library;
   }

   public String buildString() {
      final StringBuilder builder = new StringBuilder(100);
      builder.append("FILE(");
      builder.append(_parent.getLibrary());
      builder.append("/");
      builder.append(_parent.getName());
      builder.append(") TRGTIME(");
      builder.append(_time);
      builder.append(") TRGEVENT(");
      builder.append(_event);
      builder.append(") PGM(");
      builder.append(_program.getLibrary());
      builder.append("/");
      builder.append(_program.getName());
      builder.append(") RPLTRG(");
      builder.append(_replace);
      builder.append(") TRG(");
      builder.append(_name);
      builder.append(") TRGLIB(");
      builder.append(_library);
      builder.append(")");

      return builder.toString();
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof JTrigger)) {
         return false;
      }

      return buildString().equals(((JTrigger) object).buildString());
   }

   @Override
   public int hashCode() {
      return buildString().hashCode();
   }
}
