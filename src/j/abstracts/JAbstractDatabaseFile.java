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
package j.abstracts;

import com.ibm.as400.access.AS400DataType;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Structure;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.SequentialFile;
import com.ibm.as400.access.UserSpace;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import j.JAPIOutput;
import j.JConnection;
import j.JJournal;
import j.JNullObject;
import j.JSQLMember;
import j.interfaces.JDatabaseFile;
import j.interfaces.JMember;
import j.interfaces.JObject;
import j.misc.JUtil;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static com.ibm.as400.access.BinaryConverter.intToByteArray;
import static com.ibm.as400.access.QSYSObjectPathName.toPath;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static j.JAPI.QDBRTVFD;
import static j.JAPI.QUSRMBRD;
import static j.JNullMember.getInstance;
import static j.JObjectFactory.get;
import static j.enums.JType.FILE;
import static j.misc.JUtil.getRandomString;
import static j.misc.Util.splitString;

/**
 * @author Edoardo Luppi
 */
public abstract class JAbstractDatabaseFile extends JAbstractFile implements JDatabaseFile
{
   // QDBRTVFD
   protected byte[] _FILD0100;
   protected byte[] _FILD0200;
   protected byte[] _FILD0400;

   /**
    * Il giornale al quale e' iscritto il file fisico.
    */
   protected JJournal _journal;

   protected JAbstractDatabaseFile(final String name, final String library) {
      super(name, library);

      if (exists()) {
         retriveFileDescription();
      }
   }

   @Override
   public String[] getMembers() {
      final JConnection connection = getConnection();
      final String userSpaceName = getRandomString(10);
      final String userSpaceAttribute = "USRSPC";
      final UserSpace userSpace = new UserSpace(connection.getAS400(), toPath("QTEMP", userSpaceName, userSpaceAttribute));
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

         final ProgramCall pgmCall = new ProgramCall(connection.getAS400(), "/QSYS.LIB/QUSLMBR.PGM", parameters);

         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            return splitString(userSpace.read(512, userSpace.getLength()).trim(), 10);
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
         return EMPTY_LIST;
      }

      final JConnection connection = getConnection();
      final String userSpaceName = getRandomString(10);
      final UserSpace userSpace = new UserSpace(connection.getAS400(), "/QSYS.LIB/" + "QTEMP" + ".LIB/" + userSpaceName + "." + "USRSPC");
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
            new ProgramParameter(CHAR20.toBytes(JUtil.getQualifiedPath(_library, _name))),
            new ProgramParameter(CHAR10.toBytes("*FIRST")),
            new ProgramParameter(CHAR10.toBytes("")),
            new ErrorCodeParameter()
         };

         final ProgramCall pgmCall = new ProgramCall(connection.getAS400(), "/QSYS.LIB/QDBLDBR.PGM", parameters);

         if (pgmCall.run() && pgmCall.getMessageList().length < 1) {
            final String[] dirtyData = splitString(userSpace.read(260, userSpace.getLength()).trim(), 320);

            if (dirtyData.length > 0 && !dirtyData[0].contains("*NONE")) {
               final List<JObject> relationships = new ArrayList<>(dirtyData.length);

               for (final String element : dirtyData) {
                  final JObject file = get(element.substring(20, 30).trim(), element.substring(30, 40).trim(), FILE);

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

      return emptyList();
   }

   @Override
   public JJournal getJournal() {
      if (_journal == null) {
         final int offset = byteArrayToInt(_FILD0100, 378);

         if (offset > 0) {
            _journal = new JJournal((String) CHAR10.toObject(_FILD0100, offset + 10), (String) CHAR10.toObject(_FILD0100, offset));
         }
      }

      return _journal;
   }

   @Override
   public JMember toSql(final String library, final String object, final String member, final String type) {
      final JConnection connection = getConnection();

      if (!connection.exists(library, object, _type, member)) {
         final AS400File file = new SequentialFile(connection.getAS400(), toPath(library, object, "FILE"));

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

      String attribute = _attribute;

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
         _name,
         _library,
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
         new ProgramParameter(intToByteArray(ASStructure.getByteLength())),
         new ProgramParameter(CHAR8.toBytes("SQLR0100")),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAS400(), "/QSYS.LIB/QSQGNDDL.PGM", parameters);

      try {
         if (pgmCall.run() && pgmCall.getMessageList().length == 0) {
            final JAbstractMember sqlMember = new JSQLMember(member, object, library);
            sqlMember.setSourceType("SQL");
            sqlMember.performSetAttribute();
            return sqlMember;
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return getInstance();
   }

   private final void retriveFileDescription() {
      final JAPIOutput output = QUSRMBRD("MBRD0300", this);

      if (output.getMessages().length != 0) {
         return;
      }

      _MBRD0300 = output.getValue();

      // Eventuale attributo SQL
      final String attribute = CHAR10.toObject(_MBRD0300, 268).toString().trim();

      if (!attribute.isEmpty()) {
         _attribute = attribute;
      }

      _FILD0100 = QDBRTVFD("FILD0100", this).getValue();
      _FILD0200 = QDBRTVFD("FILD0200", this).getValue();
      _FILD0400 = QDBRTVFD("FILD0400", this).getValue();
   }
}
