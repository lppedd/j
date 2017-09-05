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
package lppedd.j.api.factories;

import java.io.IOException;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IFSFile;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import lppedd.j.api.files.database.JLogicalFile;
import lppedd.j.api.files.database.JPhysicalFile;
import lppedd.j.api.files.database.JSourcePhysicalFile;
import lppedd.j.api.files.device.JPrinterFile;
import lppedd.j.api.files.device.JSaveFile;
import lppedd.j.api.objects.JJournal;
import lppedd.j.api.objects.JLibrary;
import lppedd.j.api.objects.JModule;
import lppedd.j.api.objects.JNullObject;
import lppedd.j.api.objects.JObject;
import lppedd.j.api.objects.JProgram;
import lppedd.j.api.objects.JServiceProgram;

/**
 * @author Edoardo Luppi
 */
public final class JObjectFactory
{
   /**
    * Retrives an object from the system.
    *
    * @param name
    *        The name of the object
    * @param library
    *        The library of the object. Allowed values:
    *        <code>*LIBL, *USRLIBL, *CURLIB, *ALLUSR, *ALL</code>
    * @param type
    *        The type of the object. Allowed values: <code>*ALL</code>
    */
   public static JObject get(final JConnection connection, final String name, String library, final JType type) {
      JObject object = JNullObject.getInstance();
      
      final ObjectList objectList = new ObjectList(connection.getAs400(), library, name, type.getObjectType());
      objectList.addObjectAttributeToRetrieve(ObjectDescription.EXTENDED_ATTRIBUTE);
      
      try {
         objectList.load();
         
         if (objectList.getLength() > 0) {
            final ObjectDescription objDescription = objectList.getObjects(0, 1)[0];
            final String attribute = objDescription.getValueAsString(ObjectDescription.EXTENDED_ATTRIBUTE);
            library = objDescription.getLibrary();
            
            switch (objDescription.getType()) {
               case "FILE":
                  switch (attribute) {
                     case "PF":
                        final StringBuilder builder = new StringBuilder(40);
                        builder.append("/QSYS.LIB/");
                        builder.append(library);
                        builder.append("/");
                        builder.append(name);
                        builder.append(".FILE");
                        
                        final IFSFile file = new IFSFile(connection.getAs400(), builder.toString());
                        object = file.isSourcePhysicalFile() ? new JSourcePhysicalFile(connection, name, library) : new JPhysicalFile(connection, name, library);
                        break;
                     case "LF":
                        object = new JLogicalFile(connection, name, library);
                        break;
                     case "PRTF":
                        object = new JPrinterFile(connection, name, library);
                        break;
                     case "SAVF":
                        object = new JSaveFile(connection, name, library);
                     default:
                        break;
                  }
                  
                  break;
               case "PGM":
                  object = new JProgram(connection, name, library);
                  break;
               case "MODULE":
                  object = new JModule(connection, name, library);
                  break;
               case "SRVPGM":
                  object = new JServiceProgram(connection, name, library);
                  break;
               case "JRN":
                  object = new JJournal(connection, name, library);
                  break;
               case "LIB":
                  object = new JLibrary(connection, name);
               default:
                  break;
            }
         }
      } catch (NullPointerException | AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      } finally {
         if (objectList != null) {
            try {
               objectList.close();
            } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
               e.printStackTrace();
            }
         }
      }
      
      return object;
   }
}
