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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400File;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SequentialFile;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.members.JDdsMember;
import lppedd.j.api.members.JMember;
import lppedd.j.api.members.JNullMember;
import lppedd.j.api.members.JProgramMember;
import lppedd.j.api.members.JSqlMember;
import lppedd.j.api.misc.JUtil;

/**
 * Represent an IBMi source physical file.
 *
 * @author Edoardo Luppi
 */
public class JSourcePhysicalFile extends JPhysicalFile
{
   /**
    * Source member record length.
    */
   private int _recordLength = 13;

   public JSourcePhysicalFile(final JConnection connection, final String name, final String library) {
      super(connection, name, library);
   }

   /**
    * Create the source physical file from its source.
    */
   public boolean create() {
      if (exists()) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(200);
      builder.append("CRTSRCPF FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") RCDLEN(");
      builder.append(_recordLength);
      builder.append(")");

      if (!text.isEmpty()) {
         builder.append(" TEXT('");
         builder.append(text);
         builder.append("')");
      }

      return JUtil.checkForMessage("CPC7301", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Add a new source member.
    *
    * @param name
    *        Member name
    * @param type
    *        Member type
    * @param description
    *        Member description
    */
   public JMember addMember(final String name, final String description, final JType type) {
      final AS400File file = new SequentialFile(getConnection().getAs400(), getIfsPath());
      JMember member = null;

      try {
         file.addPhysicalFileMember(name, description);

         switch (type) {
            case SQLRPGLE:
            case RPGLE:
            case CLLE:
               member = new JProgramMember(null, name, name, library);
               break;
            case PF:
            case LF:
            case DS:
            case PRTF:
               member = new JDdsMember(null, name, name, library);
               break;
            case TABLE:
            case VIEW:
               member = new JSqlMember(null, name, name, library);
               break;
            default:
               member = JNullMember.getInstance();
               break;
         }

         if (member.exists()) {
            member.setSourceType(type.getSourceType());
            member.persist();
         }
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

      return member;
   }

   /**
    * Scansiona il file fisico sorgente data una stringa
    *
    * @param correspondences
    *        Lista da popolare con le corrispondenze
    * @param searchString
    *        Stringa per cui cercare
    * @param members
    *        Eventuale elenco di membri in cui cercare. Se vuoto si
    *        assume {@code *ALL}
    *
    * @return Lista dei membri con una o piu' corrispondenze
    */
   public synchronized void scan(
         final List<String> correspondences,
         final String searchString,
         final int startColumn,
         final int endColumn,
         final String... members) {
      final StringBuilder builder = new StringBuilder(67 + searchString.length() + members.length * 11);
      builder.append("FNDSTRPDM STRING('");
      builder.append(searchString);
      builder.append("') FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") MBR(");

      if (members.length > 0) {
         for (final String member : members) {
            builder.append(member);
            builder.append(" ");
         }
      } else {
         builder.append("*ALL");
      }

      builder.append(") OPTION(*NONE) PRTMBRLIST(*YES)");

      if (startColumn > 1 || endColumn >= 1) {
         builder.append(" COL(");
         builder.append(startColumn > 1 ? String.valueOf(startColumn) : "1");
         builder.append(" ");
         builder.append(endColumn >= 1 ? String.valueOf(endColumn) : "*RCDLEN");
         builder.append(")");
      }

      // Salvo la data e l'ora prima di generare lo spool
      String date = "";
      String time = "";

      try {
         // Tento reperendo la data di sistema
         date = "1" + getConnection().getSystemValue("QDATE").toString().substring(2).replace("-", "");
         time = getConnection().getSystemValue("QTIME").toString().replace(":", "");
      } catch (final IndexOutOfBoundsException e) {
         e.printStackTrace();

         // Se non riesco, provo con la data del sistema operativo, anche se potrebbe essere sbagliata
         final String dateTime = "1" + new SimpleDateFormat("yyMMddHHmmss").format(new Date());
         date = dateTime.substring(0, 7);
         time = dateTime.substring(7);
      }

      // Cancello eventuali spool precedenti per sicurezza
      getConnection().deleteSpools(getConnection().getAs400().getUserId());

      // Genero lo spool da FNDSTRPDM
      getConnection().executeCommand(builder.toString());

      // Reperisco lo spool
      final SpooledFileList spoolList = new SpooledFileList(getConnection().getAs400());
      SpooledFile spool = null;
      String spoolName = null;
      String spoolJobName = null;

      try {
         spoolList.setAttributesToRetrieve(new int[] {
               PrintObject.ATTR_SPOOLFILE, PrintObject.ATTR_JOBNAME
         });
         spoolList.setUserFilter(getConnection().getAs400().getUserId());
         spoolList.setStartDateFilter(date);
         spoolList.setEndDateFilter(date);
         spoolList.setStartTimeFilter(time);
         spoolList.openSynchronously();

         spool = (SpooledFile) spoolList.getObject(spoolList.size() - 1);
         spoolName = spool.getName();
         spoolJobName = spool.getJobName();

         if (!"QPUOPRTF".equals(spoolName) || !"QPRTJOB".equals(spoolJobName)) {
            return;
         }

         final PrintParameterList parameters = new PrintParameterList();
         parameters.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST");
         parameters.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

         final BufferedReader reader = new BufferedReader(new InputStreamReader(spool.getTransformedInputStream(parameters)));

         try {
            for (String line = null; (line = reader.readLine()) != null;) {
               if (line.length() > 12 && line.charAt(0) == ' ' && line.charAt(1) != ' ' && line.charAt(1) != '-') {
                  final String member = line.substring(1, 11).trim();

                  if ("Membro".equals(member)) {
                     continue;
                  }

                  correspondences.add(member);
               }
            }
         } catch (final IOException e) {
            e.printStackTrace();
         } finally {
            if (reader != null) {
               reader.close();
            }
         }
      } catch (final Exception e) {
         e.printStackTrace();
      } finally {
         if (spoolList != null) {
            spoolList.close();
         }

         if (spool != null) {
            builder.delete(0, builder.length());
            builder.append("DLTSPLF FILE(QPUOPRTF) JOB(");
            builder.append(spool.getJobNumber());
            builder.append("/");
            builder.append(spool.getJobUser());
            builder.append("/");
            builder.append(spoolJobName);
            builder.append(")");

            // Cancello lo spool
            getConnection().executeCommand(builder.toString());
         }
      }
   }

   /**
    * Returns the source member record length.
    */
   public int getRecordLength() {
      return _recordLength;
   }

   /**
    * Set the source member record length.<br>
    * Valid operation only if the source physical file does not exists.
    */
   public boolean setRecordLength(final int length) {
      if (exists() || _recordLength == length || length < 13) {
         return false;
      }

      _recordLength = length;
      return true;
   }
}
