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
package lppedd.j.api.files.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Structure;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.SequentialFile;
import com.ibm.as400.access.UserSpace;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.files.JAbstractFile;
import lppedd.j.api.ibm.JApi;
import lppedd.j.api.ibm.JApiResult;
import lppedd.j.api.members.JAbstractMember;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.members.JSqlMember;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.misc.Util;
import lppedd.j.api.objects.JJournal;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;

/**
 * @author Edoardo Luppi
 */
public abstract class JAbstractDatabaseFile extends JAbstractFile implements JDatabaseFile
{
   // QDBRTVFD
   protected byte[] FILD0100;
   protected byte[] FILD0200;
   protected byte[] FILD0400;

   protected JJournal journal;

   protected JAbstractDatabaseFile(final JConnection connection, final String name, final String library) {
      super(connection, name, library);

      if (exists()) {
         retriveFileDescription();
      }
   }

   @Override
   public String[] getMembers() {
      final JConnection connection = getConnection();
      final String userSpaceName = JUtil.getRandomString(10);
      final String userSpaceAttribute = "USRSPC";
      final UserSpace userSpace = new UserSpace(connection.getAs400(), QSYSObjectPathName.toPath("QTEMP", userSpaceName, userSpaceAttribute));
      userSpace.setMustUseProgramCall(true);
      userSpace.setMustUseSockets(true);

      try {
         if (userSpace.exists()) {
            userSpace.delete();
         }

         userSpace.create(20000, true, userSpaceAttribute, (byte) 0x00, "Userspace per elenco membri", "*ALL");

         final ProgramParameter[] parameters = new ProgramParameter[] {
               new ProgramParameter(CHAR20.toBytes(JUtil.getQualifiedPath("QTEMP", userSpaceName))),
               new ProgramParameter(CHAR8.toBytes("MBRL0100")),
               new ProgramParameter(CHAR20.toBytes(getQualifiedPath())),
               new ProgramParameter(CHAR10.toBytes("*ALL")),
               new ProgramParameter(CHAR1.toBytes("1")),
               new ErrorCodeParameter()
         };

         final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QUSLMBR.PGM", parameters);

         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            return Util.splitString(userSpace.read(512, userSpace.getLength()).trim(), 10);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      } finally {
         try {
            if (userSpace != null) {
               userSpace.close();
               userSpace.delete();
            }
         } catch (final IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException e) {
            e.printStackTrace();
         }
      }

      return new String[0];
   }

   @Override
   public List<JObject> getDatabaseRelations() {
      if (!exists()) {
         return Collections.emptyList();
      }

      final JConnection connection = getConnection();
      final String userSpaceName = JUtil.getRandomString(10);
      final UserSpace userSpace = new UserSpace(connection.getAs400(), "/QSYS.LIB/" + "QTEMP" + ".LIB/" + userSpaceName + "." + "USRSPC");
      userSpace.setMustUseProgramCall(true);
      userSpace.setMustUseSockets(true);

      try {
         if (userSpace.exists()) {
            userSpace.delete();
         }

         userSpace.create(20000, true, "USRSPC", (byte) 0x00, "Userspace per DSPDBR", "*ALL");

         final ProgramParameter[] parameters = new ProgramParameter[] {
               new ProgramParameter(CHAR20.toBytes(JUtil.getQualifiedPath("QTEMP", userSpaceName))),
               new ProgramParameter(CHAR8.toBytes("DBRL0100")),
               new ProgramParameter(CHAR20.toBytes(JUtil.getQualifiedPath(library, name))),
               new ProgramParameter(CHAR10.toBytes("*FIRST")),
               new ProgramParameter(CHAR10.toBytes("")),
               new ErrorCodeParameter()
         };

         final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QDBLDBR.PGM", parameters);

         if (pgmCall.run() && pgmCall.getMessageList().length < 1) {
            final String[] dirtyData = Util.splitString(userSpace.read(260, userSpace.getLength()).trim(), 320);

            if (dirtyData.length > 0 && !dirtyData[0].contains("*NONE")) {
               final List<JObject> relationships = new ArrayList<>(dirtyData.length);

               for (final String element : dirtyData) {
                  final JObject file = JObjectFactory.get(
                        getConnection(),
                        element.substring(20, 30).trim(),
                        element.substring(30, 40).trim(),
                        JType.FILE);

                  if (!(file instanceof JNullObject) && !relationships.contains(file)) {
                     relationships.add(file);
                  }
               }

               return relationships;
            }
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      } finally {
         try {
            if (userSpace != null) {
               userSpace.close();
               userSpace.delete();
            }
         } catch (final IOException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | ObjectDoesNotExistException e) {
            e.printStackTrace();
         }
      }

      return Collections.emptyList();
   }

   @Override
   public Optional<JJournal> getJournal() {
      if (journal == null) {
         final int offset = BinaryConverter.byteArrayToInt(FILD0100, 378);

         if (offset > 0) {
            journal = new JJournal(
                  getConnection(),
                  (String) CHAR10.toObject(FILD0100, offset + 10),
                  (String) CHAR10.toObject(FILD0100, offset));
         }
      }

      return Optional.ofNullable(journal);
   }

   @Override
   public JMember toSql(final String library, final String object, final String member, final String type) {
      final JConnection connection = getConnection();

      if (!connection.exists(library, object, this.type, member)) {
         final AS400File file = new SequentialFile(connection.getAs400(), QSYSObjectPathName.toPath(library, object, "FILE"));

         try {
            file.addPhysicalFileMember(member, getText());
         } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
            e.printStackTrace();
         } finally {
            try {
               if (file != null) {
                  file.close();
               }
            } catch (AS400Exception | AS400SecurityException | InterruptedException | IOException e) {
               e.printStackTrace();
            }
         }
      }

      String attribute = getAttribute();

      switch (attribute) {
         case "PF":
            attribute = "TABLE";
            break;
         case "LF":
            if ("INDEX".equalsIgnoreCase(type)) {
               attribute = type;
            } else {
               attribute = "VIEW";
            }

            break;
         default:
            break;
      }

      // Costruisco il template SQLR0100
      final AS400DataType[] dataTypes = new AS400DataType[] {
            CHAR258,
            CHAR258,
            CHAR10,
            CHAR10,
            CHAR10,
            CHAR10,
            BIN4,
            CHAR1,
            CHAR1,
            CHAR3,
            CHAR1,
            CHAR3,
            CHAR1,
            CHAR3,
            CHAR1,
            CHAR1,
            CHAR1,
            BIN4,
            CHAR1,
            CHAR1,
            CHAR1
      };

      final Object[] javaStructure = new Object[] {
            name,
            library,
            attribute,
            object,
            library,
            member,
            30,
            "1",
            "0",
            "ISO",
            "",
            "ISO",
            "",
            "SYS",
            ".",
            "0",
            "0",
            30,
            "0",
            "1",
            "0"
      };

      final AS400Structure ASStructure = new AS400Structure(dataTypes);
      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(ASStructure.toBytes(javaStructure)),
            new ProgramParameter(BinaryConverter.intToByteArray(ASStructure.getByteLength())),
            new ProgramParameter(CHAR8.toBytes("SQLR0100")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QSQGNDDL.PGM", parameters);

      try {
         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            final JAbstractMember sqlMember = new JSqlMember(null, member, object, library);
            sqlMember.setSourceType("SQL");
            sqlMember.persist();
            return sqlMember;
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return JNullMember.getInstance();
   }

   private final void retriveFileDescription() {
      final JConnection connection = getConnection();
      final JApiResult output = JApi.QUSRMBRD(connection, "MBRD0300", this);

      if (output.getMessages().length != 0) {
         return;
      }

      MBRD0300 = output.getValue();

      // Eventuale attributo SQL
      final String attribute = CHAR10.toObject(MBRD0300, 268).toString().trim();

      if (!attribute.isEmpty()) {
         this.attribute = attribute;
      }

      FILD0100 = JApi.QDBRTVFD(connection, "FILD0100", this).getValue();
      FILD0200 = JApi.QDBRTVFD(connection, "FILD0200", this).getValue();
      FILD0400 = JApi.QDBRTVFD(connection, "FILD0400", this).getValue();
   }
}
