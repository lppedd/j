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
package lppedd.j.api.files.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.as400.access.BinaryConverter;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.ibm.JApi;
import lppedd.j.api.ibm.JApiResult;
import lppedd.j.api.misc.JUtil;

public class JSaveFile extends JAbstractDeviceFile
{
   public static class JSavedObject
   {
      private final String name;
      private final String library;
      private final JType type;
      private final String attribute;
      private final String text;
      private final List<String> members;
      
      private JSavedObject(final String name, final String library, final JType type, final String attribute, final String text, final List<String> members) {
         this.name = name;
         this.library = library;
         this.type = type;
         this.attribute = attribute;
         this.text = text;
         this.members = members;
      }
      
      public String getName() {
         return name;
      }
      
      public String getLibrary() {
         return library;
      }
      
      public JType getType() {
         return type;
      }
      
      public String getAttribute() {
         return attribute;
      }
      
      public String getText() {
         return text;
      }
      
      public List<String> getMembers() {
         return members;
      }
   }
   
   public JSaveFile(final JConnection connection, final String name, final String library) {
      super(connection, name, library);
   }
   
   public List<JSavedObject> getSavedObjects() {
      final JConnection connection = getConnection();

      // Recupero gli oggetti.
      JApiResult output = JApi.QSRLSAVF(connection, "SAVF0200", "*ALL", "*ALL", this);
      
      if (output.getMessages().length != 0) {
         return Collections.emptyList();
      }
      
      final byte[] objectsBytes = output.getValue();
      int objectsOffset = BinaryConverter.byteArrayToInt(objectsBytes, 124);
      int objectsCount = BinaryConverter.byteArrayToInt(objectsBytes, 132);
      final int objectEntrySize = BinaryConverter.byteArrayToInt(objectsBytes, 136);
      final List<JSavedObject> savedObjects = new ArrayList<>(objectsCount);
      
      for (; objectsCount > 0; --objectsCount, objectsOffset += objectEntrySize) {
         final String savedObjectName = ((String) CHAR10.toObject(objectsBytes, objectsOffset)).trim();
         final JType savedObjectType = JType.valueOf(((String) CHAR10.toObject(objectsBytes, objectsOffset + 20)).trim().substring(1));
         
         // Lista vuota, dato che l'oggetto potrebbe non essere un file, e quindi contenere membri.
         List<String> savedObjectMembers = Collections.emptyList();
         
         // Se l'oggetto e' un file, reperisco i suoi membri.
         if (JType.FILE == savedObjectType) {
            output = JApi.QSRLSAVF(connection, "SAVF0300", savedObjectName, "*ALL", this);
            
            if (output.getMessages().length != 0) {
               continue;
            }
            
            final byte[] membersBytes = output.getValue();
            int membersOffset = BinaryConverter.byteArrayToInt(membersBytes, 124);
            int membersCount = BinaryConverter.byteArrayToInt(membersBytes, 132);
            final int memberEntrySize = BinaryConverter.byteArrayToInt(membersBytes, 136);
            
            // Riassegno alla lista una nuova lista con dimensione iniziale uguale al numero di membri.
            savedObjectMembers = new ArrayList<>(membersCount);
            
            for (; membersCount > 0; --membersCount, membersOffset += memberEntrySize) {
               savedObjectMembers.add(((String) CHAR10.toObject(membersBytes, membersOffset + 20)).trim());
            }
         }
         
         savedObjects.add(new JSavedObject(
               savedObjectName,
               ((String) CHAR10.toObject(objectsBytes, objectsOffset + 10)).trim(),
               savedObjectType,
               ((String) CHAR10.toObject(objectsBytes, objectsOffset + 30)).trim(),
               ((String) CHAR50.toObject(objectsBytes, objectsOffset + 154)).trim(),
               savedObjectMembers));
      }
      
      return savedObjects;
   }
   
   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(144);
      builder.append("CHGSAVF FILE(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") TEXT('");
      builder.append(text);
      builder.append("')");
      
      return JUtil.checkForMessage("CPC7303", getConnection().executeCommand(builder.toString()));
   }
}
