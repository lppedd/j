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
package j.commands;

import j.JConnection;
import j.JJournal;
import j.JPhysicalFile;
import j.interfaces.JCommand;

import static j.JConnection.getInstance;
import static j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public class ENDJRNPF implements JCommand<Boolean>
{
   public static final String LOGLVL_ALL = "*ALL";
   public static final String LOGLVL_ERRORS = "*ERRORS";

   private JPhysicalFile[] _files;
   private JJournal _journal;
   private String _logLevel = LOGLVL_ERRORS;

   @Override
   public Boolean execute() {
      final JConnection connection = getInstance();
      final StringBuilder builder = new StringBuilder(85);
      builder.append("ENDJRNPF FILE(");

      for (final JPhysicalFile file : _files) {
         builder.append(file.getLibrary());
         builder.append("/");
         builder.append(file.getName());
         builder.append(" ");
      }

      builder.deleteCharAt(builder.length());
      builder.append(") JRN(");
      builder.append(_journal.getLibrary());
      builder.append("/");
      builder.append(_journal.getName());
      builder.append(") LOGLVL(");
      builder.append(_logLevel);
      builder.append(")");

      return checkForMessage("CPC7031", connection.executeCommand(builder.toString()));
   }

   public JPhysicalFile[] getFiles() {
      return _files;
   }

   public JJournal getJournal() {
      return _journal;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public void setFiles(final JPhysicalFile... files) {
      _files = files;
   }

   public void setJournal(final JJournal journal) {
      _journal = journal;
   }

   public void setLogLevel(final String logLevel) {
      _logLevel = logLevel;
   }
}
