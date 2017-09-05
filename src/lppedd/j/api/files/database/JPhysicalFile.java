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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.BinaryConverter;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JProgram;
import lppedd.j.api.objects.virtual.JTrigger;

/**
 * Represents an IBMi physical file (PF and SQL).
 *
 * @author Edoardo Luppi
 */
public class JPhysicalFile extends JAbstractDatabaseFile
{
   /**
    * Physical file maximum number of records.
    */
   protected static final int MAX_DIMENSION = 2147483646;

   public JPhysicalFile(final JConnection connection, final String name, final String library) {
      super(connection, name, library);
   }

   /**
    * Copy all the records to another physical file.<br>
    * By default it uses <code>FMTOPT(*MAP *DROP)</code>
    *
    * @param physicalFile
    *        The target physical file
    */
   public boolean copyFile(final JPhysicalFile physicalFile) {
      final StringBuilder builder = new StringBuilder(136);
      builder.append("CPYF FROMFILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") TOFILE(");
      builder.append(physicalFile.library);
      builder.append("/");
      builder.append(physicalFile.name);
      builder.append(") FROMMBR(*ALL) TOMBR(*FROMMBR) MBROPT(*ADD) FMTOPT(*MAP *DROP)");

      final AS400Message[] messages = getConnection().executeCommand(builder.toString());
      return JUtil.checkForMessage("CPC2955", messages) || JUtil.checkForMessage("CPC2957", messages);
   }

   /**
    * Changes the physical file size.
    *
    * @param newSize
    *        The new size.
    */
   public boolean changeSize(final int newSize) {
      if (newSize < 1 || newSize > MAX_DIMENSION) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(100);
      builder.append("CHGPF FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") SIZE(");
      builder.append(newSize);
      builder.append(")");

      return JUtil.checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Add a trigger to the physical file.
    *
    * @param trigger
    *        The trigger to be added
    */
   public boolean addTrigger(final JTrigger trigger) {
      return JUtil.checkForMessage("CPC3203", getConnection().executeCommand("ADDPFTRG " + trigger.buildString()));
   }

   /**
    * Returns the triggers attached to the physical file.
    */
   public List<JTrigger> getTriggers() {
      // Find Qdb_Qdbfphys offset
      int offset = BinaryConverter.byteArrayToInt(FILD0100, 364);

      if (offset > 0) {
         // Find the number of triggers
         final int triggerCount = BinaryConverter.byteArrayToUnsignedShort(FILD0100, offset + 28);

         if (triggerCount > 0) {
            final List<JTrigger> triggers = new ArrayList<>(triggerCount);

            // Find Qdb_Qdbftrg offset
            offset = BinaryConverter.byteArrayToInt(FILD0100, offset + 24);

            for (int i = 0; i < triggerCount; i++) {
               if ((FILD0100[offset + 23] & 0x02) != 0) {
                  // Analyze the FILD0400 format for more accurate information about the trigger
                  // TODO
               }

               final JProgram program = (JProgram) JObjectFactory.get(
                     getConnection(),
                     (String) CHAR10.toObject(FILD0100, offset + 2),
                     (String) CHAR10.toObject(FILD0100, offset + 12),
                     JType.PGM);

               final String time = getTimeFromAPI((String) CHAR1.toObject(FILD0100, offset));
               final String event = getEventFromAPI((String) CHAR1.toObject(FILD0100, offset + 1));

               triggers.add(new JTrigger(this, time, event, program));

               // Move to the next trigger
               offset = offset + 48;
            }

            return triggers;
         }
      }

      return Collections.emptyList();
   }

   /**
    * Returns if the physical file is a source file.
    */
   public boolean isSourceFile() {
      return (FILD0100[8] & 0x08) != 0;
   }

   /**
    * Returns if the physical file has multiple members.
    */
   public boolean isMultiMember() {
      return BinaryConverter.byteArrayToUnsignedShort(FILD0100, 41) != 1;
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGPF FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") TEXT('");
      builder.append(text);
      builder.append("')");

      return JUtil.checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
   }

   protected final String getTimeFromAPI(final String string) {
      String time = "";

      switch (string) {
         case "1":
            time = "*AFTER";
            break;
         case "2":
            time = "*BEFORE";
            break;
         case "3":
            time = "*INSTEAD";
            break;
         default:
            break;
      }

      return time;
   }

   protected final String getEventFromAPI(final String string) {
      String event = "";

      switch (string) {
         case "1":
            event = "*INSERT";
            break;
         case "2":
            event = "*DELETE";
            break;
         case "3":
            event = "*UPDATE";
            break;
         case "4":
            event = "*READ";
            break;
         default:
            break;
      }

      return event;
   }
}
