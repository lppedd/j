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
package lppedd.j.api.objects.virtual;

import lppedd.j.api.files.database.JDatabaseFile;
import lppedd.j.api.objects.JProgram;

/**
 * Contains an IBMi trigger attributes.
 *
 * @author Edoardo Luppi
 * @see JVirtual
 */
public class JTrigger implements JVirtual
{
   private final JDatabaseFile _parent;
   private final String _time;
   private final String _event;
   private final JProgram _program;

   // Optional values
   private String _replace = "*NO";
   private String _name = "*GEN";
   private String _library = "*FILE";

   public JTrigger(final JDatabaseFile parent, final String time, final String event, final JProgram program) {
      _parent = parent;
      _time = time;
      _event = event;
      _program = program;
   }

   public JDatabaseFile getParent() {
      return _parent;
   }

   public JProgram getProgram() {
      return _program;
   }

   public String getTime() {
      return _time;
   }

   public String getEvent() {
      return _event;
   }

   public String getReplace() {
      return _replace;
   }

   public String getName() {
      return _name;
   }

   public String getLibrary() {
      return _library;
   }

   public void setReplace(final String replace) {
      _replace = replace;
   }

   public void setName(final String name) {
      _name = name;
   }

   public void setLibrary(final String library) {
      _library = library;
   }

   public String buildString() {
      final StringBuilder builder = new StringBuilder(100);
      builder.append("FILE(");
      builder.append(_parent.getLibrary());
      builder.append("/");
      builder.append(_parent.getName());
      builder.append(") TRGTIME(");
      builder.append(_time);
      builder.append(") TRGEVENT(");
      builder.append(_event);
      builder.append(") PGM(");
      builder.append(_program.getLibrary());
      builder.append("/");
      builder.append(_program.getName());
      builder.append(") RPLTRG(");
      builder.append(_replace);
      builder.append(") TRG(");
      builder.append(_name);
      builder.append(") TRGLIB(");
      builder.append(_library);
      builder.append(")");

      return builder.toString();
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof JTrigger)) {
         return false;
      }

      return buildString().equals(((JTrigger) object).buildString());
   }

   @Override
   public int hashCode() {
      return buildString().hashCode();
   }
}
