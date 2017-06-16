/* 
 * The MIT License
 *
 * Copyright (c) 2017 Edoardo Luppi <lp.edoardo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package j;

import com.ibm.as400.access.*;
import java.io.IOException;
import j.interfaces.JMember;

import static j.interfaces.AS400DataTypes.BIN4;
import static j.interfaces.AS400DataTypes.CHAR1;
import static j.interfaces.AS400DataTypes.CHAR10;
import static j.interfaces.AS400DataTypes.CHAR20;
import static j.misc.JUtil.getQualifiedPath;

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
