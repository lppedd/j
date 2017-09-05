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

import com.ibm.as400.access.QSYSObjectPathName;

import lppedd.j.api.JAbstractBase;
import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.ibm.JApi;
import lppedd.j.api.ibm.JApiResult;
import lppedd.j.api.misc.JUtil;

/**
 * Represents an IBMi object.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractObject extends JAbstractBase implements JObject
{
   // OBJD0400 -> QUSROBJD
   protected byte[] OBJD0400;

   protected JAbstractObject(final JConnection connection, final String name, final String library, final JType type) {
      super(connection, name, library, type);

      if (exists()) {
         retriveObjectDescription();
      }
   }

   @Override
   public boolean exists() {
      return getConnection().exists(library, name, type, null);
   }

   @Override
   public boolean copy(final String library, final String name, final boolean overwrite) {
      final JConnection connection = getConnection();

      if (!overwrite && connection.exists(library, name, type, null)) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(112);
      builder.append("CRTDUPOBJ OBJ(");
      builder.append(name);
      builder.append(") FROMLIB(");
      builder.append(library);
      builder.append(") NEWOBJ(");
      builder.append(name);
      builder.append(") TOLIB(");
      builder.append(library);
      builder.append(") OBJTYPE(");
      builder.append(type.getObjectType());
      builder.append(") CST(*NO) TRG(*NO) DATA(*NO)");

      return JUtil.checkForMessage("CPI2101", connection.executeCommand(builder.toString()));
   }

   @Override
   public boolean move(final String library, final String name, final boolean overwrite) {
      if (!copy(library, name, overwrite)) {
         return false;
      }

      // Delete the object from the actual position
      if (!delete()) {
         return false;
      }

      this.library = library;
      this.name = name;
      return true;
   }

   @Override
   public String getIfsPath() {
      return QSYSObjectPathName.toPath(library, name, type.getObjectType().substring(1));
   }

   @Override
   public String getQualifiedPath() {
      return JUtil.getQualifiedPath(library, name);
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   protected boolean performSetName() {
      final StringBuilder builder = new StringBuilder(84);
      builder.append("RNMOBJ OBJ(");
      builder.append(library);
      builder.append("/");
      builder.append(originalName);
      builder.append(") NEWOBJ(");
      builder.append(name);
      builder.append(") OBJTYPE(");
      builder.append(type.getObjectType());
      builder.append(")");

      return JUtil.checkForMessage("CPC2192", getConnection().executeCommand(builder.toString()));
   }

   /**
    * Calls QUSROBJD API to retrive the object informations.
    */
   private final void retriveObjectDescription() {
      final JApiResult output = JApi.QUSROBJD(getConnection(), "OBJD0400", this);

      if (output.getMessages().length != 0) {
         return;
      }

      OBJD0400 = output.getValue();

      text = CHAR50.toObject(OBJD0400, 100).toString().trim();
      creator = CHAR10.toObject(OBJD0400, 219).toString().trim();

      if (attribute == null || attribute.isEmpty()) {
         attribute = CHAR10.toObject(OBJD0400, 90).toString().trim();
      }

      final StringBuilder builder = new StringBuilder(19);
      builder.append(CHAR13.toObject(OBJD0400, 64));

      if (builder.charAt(0) == '0') {
         builder.insert(1, "19");
      } else {
         builder.insert(1, "20");
      }

      builder.delete(0, 1);
      builder.append("000");

      creationDateTime = Long.parseLong(builder.toString());
   }
}
