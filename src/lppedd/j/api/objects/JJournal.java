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
package lppedd.j.api.objects;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.misc.JUtil;

/**
 * Represents an IBMi journal.
 *
 * @author Edoardo Luppi
 */
public class JJournal extends JAbstractObject
{
   public JJournal(final JConnection connection, final String name, final String library) {
      super(connection, name, library, JType.JRN);
   }

   @Override
   public boolean delete() {
      return JUtil.checkForMessage("CPC2191", getConnection().executeCommand("DLTJRN JRN(" + library + "/" + name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGJRN JRN(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") TEXT('");
      builder.append(text);
      builder.append("')");

      // FIXME: cambiare il codice del messaggio
      return JUtil.checkForMessage("CPC0540", getConnection().executeCommand(builder.toString()));
   }
}
