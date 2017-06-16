package lppedd.j;

import com.ibm.as400.access.*;
import java.io.IOException;
import lppedd.j.interfaces.JMember;

import static lppedd.j.interfaces.AS400DataTypes.BIN4;
import static lppedd.j.interfaces.AS400DataTypes.CHAR1;
import static lppedd.j.interfaces.AS400DataTypes.CHAR10;
import static lppedd.j.interfaces.AS400DataTypes.CHAR20;
import static lppedd.j.misc.JUtil.getQualifiedPath;

/**
 * @author Edoardo Luppi
 */
public final class JMemberFactory
{
   /**
    * Get a source member from the system.
    *
    * @param name    The name of the source member
    * @param object  The object of the source member
    * @param library The library of the source member
    */
   public static <T extends JMember> T get(final String name, final String object, final String library) {
      JMember member = JNullMember.getInstance();

      // I need only 58 bytes
      final ProgramParameter[] parameters = new ProgramParameter[] {
         new ProgramParameter(58),
         new ProgramParameter(BIN4.toBytes(58)),
         new ProgramParameter(CHAR10.toBytes("MBRD0100")),
         new ProgramParameter(CHAR20.toBytes(getQualifiedPath(library, object))),
         new ProgramParameter(CHAR10.toBytes(name)),
         new ProgramParameter(CHAR1.toBytes("1")),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(JConnection.getInstance().getAS400(), "/QSYS.LIB/QUSRMBRD.PGM", parameters);

      try {
         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            switch (CHAR10.toObject(parameters[0].getOutputData(), 48).toString().trim()) {
               case "SQL":
                  member = new JSQLMember(name, object, library);
                  break;
               case "PF":
               case "LF":
               case "PRTF":
                  member = new JDDSMember(name, object, library);
                  break;
               case "CLLE":
               case "RPGLE":
               case "SQLRPGLE":
                  member = new JProgramMember(name, object, library);
                  break;
               default:
                  break;
            }
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return (T) member;
   }

   /**
    * Get a source member from the system using the user library list.
    *
    * @param name    The name of the source member
    * @param objects An array of source physical files to scan
    */
   public static <T extends JMember> T get(final String name, final String... objects) {
      JMember member = JNullMember.getInstance();

      outer:
      for (final String library : JConnection.getInstance().getLibraryList().getUserPortion()) {
         for (final String object : objects) {
            member = get(name, object, library);

            if (!(member instanceof JNullMember)) {
               break outer;
            }
         }
      }

      return (T) member;
   }
}
