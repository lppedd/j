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
package lppedd.j.api.commands;

import lppedd.j.api.JConnection;
import lppedd.j.api.files.database.JLogicalFile;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JJournal;

/**
 * @author Edoardo Luppi
 */
public final class STRJRNAP implements JCommand<Boolean>
{
   public static final String LOGLVL_ALL = "*ALL";
   public static final String LOGLVL_ERRORS = "*ERRORS";

   private JLogicalFile file;
   private JJournal _journal;
   private String logLevel = LOGLVL_ALL;

   @Override
   public Boolean execute(final JConnection connection) {
      final StringBuilder builder = new StringBuilder(85);
      builder.append("STRJRNAP FILE(");
      builder.append(file.getLibrary());
      builder.append("/");
      builder.append(file.getName());
      builder.append(") JRN(");
      builder.append(_journal.getLibrary());
      builder.append("/");
      builder.append(_journal.getName());
      builder.append(") LOGLVL(");
      builder.append(logLevel);
      builder.append(")");

      return JUtil.checkForMessage("CPC7031", connection.executeCommand(builder.toString()));
   }

   public JLogicalFile getFile() {
      return file;
   }

   public String getLogLevel() {
      return logLevel;
   }

   public void setFile(final JLogicalFile file) {
      this.file = file;
   }

   public void setLogLevel(final String logLevel) {
      this.logLevel = logLevel;
   }
}
