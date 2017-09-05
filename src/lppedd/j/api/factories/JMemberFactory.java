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
package lppedd.j.api.factories;

import java.io.IOException;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

import lppedd.j.api.JConnection;
import lppedd.j.api.ibm.IBMiDataTypes;
import lppedd.j.api.members.JDdsMember;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.members.JProgramMember;
import lppedd.j.api.members.JSqlMember;
import lppedd.j.api.misc.JUtil;

/**
 * @author Edoardo Luppi
 */
public final class JMemberFactory
{
   /**
    * Get a source member from the system.
    *
    * @param name
    *        The name of the source member
    * @param object
    *        The object of the source member
    * @param library
    *        The library of the source member
    */
   public static JMember get(final JConnection connection, final String name, final String object, final String library) {
      JMember member = JNullMember.getInstance();

      // I need only 58 bytes
      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(58),
            new ProgramParameter(IBMiDataTypes.BIN4.toBytes(58)),
            new ProgramParameter(IBMiDataTypes.CHAR10.toBytes("MBRD0100")),
            new ProgramParameter(IBMiDataTypes.CHAR20.toBytes(JUtil.getQualifiedPath(library, object))),
            new ProgramParameter(IBMiDataTypes.CHAR10.toBytes(name)),
            new ProgramParameter(IBMiDataTypes.CHAR1.toBytes("1")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QUSRMBRD.PGM", parameters);

      try {
         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            switch (IBMiDataTypes.CHAR10.toObject(parameters[0].getOutputData(), 48).toString().trim()) {
               case "SQL":
                  member = new JSqlMember(null, name, object, library);
                  break;
               case "PF":
               case "LF":
               case "PRTF":
                  member = new JDdsMember(null, name, object, library);
                  break;
               case "CLLE":
               case "RPGLE":
               case "SQLRPGLE":
                  member = new JProgramMember(null, name, object, library);
                  break;
               default:
                  break;
            }
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return member;
   }

   /**
    * Get a source member from the system using the user library list.
    *
    * @param name
    *        The name of the source member
    * @param objects
    *        An array of source physical files to scan
    */
   public static JMember get(final JConnection connection, final String name, final String... objects) {
      JMember member = JNullMember.getInstance();

      outer:
      for (final String library : connection.getJob().getLibraryList().getUserPortion()) {
         for (final String object : objects) {
            member = get(connection, name, object, library);

            if (!(member instanceof JNullMember)) {
               break outer;
            }
         }
      }

      return member;
   }
}
