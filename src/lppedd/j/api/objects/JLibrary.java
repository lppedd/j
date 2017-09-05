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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.factories.JObjectFactory;
import lppedd.j.api.misc.JUtil;

/**
 * Represents an IBMi library.
 *
 * @author Edoardo Luppi
 */
public class JLibrary extends JAbstractObject
{
   public JLibrary(final JConnection connection, final String name) {
      super(connection, name, "QSYS", JType.LIB);
   }

   /**
    * Returns the library content.
    *
    * @return A list of the objects contained in the library
    */
   public List<JObject> getObjects(final JType objectType) {
      final ObjectList objectList = new ObjectList(getConnection().getAs400(), name, ObjectList.ALL, objectType.getObjectType());

      try {
         objectList.load();

         final int size = objectList.getLength();
         final List<JObject> objects = new ArrayList<>(size);
         final ObjectDescription[] objectDescriptions = objectList.getObjects(0, size);

         for (int i = 0; i < size; i++) {
            final ObjectDescription objectDescription = objectDescriptions[i];
            objects.add(JObjectFactory.get(getConnection(), objectDescription.getName(), objectDescription.getLibrary(), JType.valueOf(objectDescription.getType())));
         }

         return objects;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return Collections.emptyList();
   }

   /**
    * Create the library.
    */
   public boolean create() {
      if (exists()) {
         return false;
      }

      final StringBuilder builder = new StringBuilder(95);
      builder.append("CRTLIB LIB(");
      builder.append(name);
      builder.append(") TYPE(*PROD)");

      if (!text.isEmpty()) {
         builder.append(" TEXT('");
         builder.append(text);
         builder.append("')");
      }

      return JUtil.checkForMessage("CPC2102", getConnection().executeCommand(builder.toString()));
   }

   @Override
   public boolean delete() {
      return JUtil.checkForMessage("CPC2194", getConnection().executeCommand("DLTLIB (" + name + ")"));
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(30);
      builder.append("RNMOBJ (");
      builder.append(library);
      builder.append("/");
      builder.append(originalName);
      builder.append(") OBJTYPE(");
      builder.append(type.getObjectType());
      builder.append(") NEWOBJ(");
      builder.append(name);
      builder.append(")");

      return JUtil.checkForMessage("CPC2192", getConnection().executeCommand(builder.toString()));
   }
}
