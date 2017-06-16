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
package lppedd.j.commands;

import lppedd.j.JJournal;
import lppedd.j.JLogicalFile;
import lppedd.j.interfaces.JCommand;

import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.checkForMessage;

/**
 * @author Edoardo Luppi
 */
public final class STRJRNAP implements JCommand<Boolean>
{
   public static final String LOGLVL_ALL = "*ALL";
   public static final String LOGLVL_ERRORS = "*ERRORS";

   private JLogicalFile _file;
   private JJournal _journal;
   private String _logLevel = LOGLVL_ALL;

   @Override
   public Boolean execute() {
      final StringBuilder builder = new StringBuilder(85);
      builder.append("STRJRNAP FILE(");
      builder.append(_file.getLibrary());
      builder.append("/");
      builder.append(_file.getName());
      builder.append(") JRN(");
      builder.append(_journal.getLibrary());
      builder.append("/");
      builder.append(_journal.getName());
      builder.append(") LOGLVL(");
      builder.append(_logLevel);
      builder.append(")");

      return checkForMessage("CPC7031", getInstance().executeCommand(builder.toString()));
   }

   public JLogicalFile getFile() {
      return _file;
   }

   public String getLogLevel() {
      return _logLevel;
   }

   public void setFile(final JLogicalFile file) {
      _file = file;
   }

   public void setLogLevel(final String logLevel) {
      _logLevel = logLevel;
   }
}
