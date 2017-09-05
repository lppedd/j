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

import static com.ibm.as400.access.QSYSObjectPathName.toPath;
import static lppedd.j.api.JType.USRSPC;
import static lppedd.j.api.ibm.JApi.QUSDLTUS;
import static lppedd.j.api.misc.JUtil.checkForMessage;

import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.UserSpace;

import lppedd.j.api.JConnection;

/**
 * @author Edoardo Luppi
 */
public class JUserSpace extends JAbstractObject
{
   protected final UserSpace userSpace;
   protected final int length;
   protected final boolean isAutoExtendible;
   protected final byte initialValue;

   private JUserSpace(final JUserSpaceBuilder builder) {
      super(builder.connection, builder.name, builder.library, USRSPC);

      length = builder.length;
      isAutoExtendible = builder.isAutoExtendible;
      initialValue = builder.initialValue;
      userSpace = new UserSpace();

      // Setting these properties to true ensure everything is run on the same job, so using QTEMP should be safe.
      userSpace.setMustUseProgramCall(true);
      userSpace.setMustUseSockets(true);

      try {
         userSpace.setSystem(getConnection().getAs400());
         userSpace.setPath(toPath(builder.library, builder.name, type.getObjectType().substring(1)));
      } catch (final PropertyVetoException e) {
         e.printStackTrace();
      }
   }

   public boolean create() {
      try {
         userSpace.create(length, false, "", (byte) 0x00, text, "*ALL");

         // Not always calling setAutoExtendible() to avoid a possibile exception.
         if (isAutoExtendible) {
            userSpace.setAutoExtendible(true);
         }

         return true;
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }

   public String read(final int start, final int length) {
      try {
         return userSpace.read(start, length);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return "";
   }

   public int read(final byte[] buffer, final int bufferStart, final int start, final int length) {
      try {
         return userSpace.read(buffer, start, bufferStart, length);
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return -1;
   }

   public int getLength() {
      try {
         return userSpace.getLength();
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return 0;
   }

   public UserSpace getHandle() {
      return userSpace;
   }

   @Override
   public boolean delete() {
      return QUSDLTUS(this);
   }

   @Override
   protected boolean performSetText() {
      final StringBuilder builder = new StringBuilder(90);
      builder.append("CHGOBJD OBJ(");
      builder.append(library);
      builder.append("/");
      builder.append(name);
      builder.append(") OBJTYPE(");
      builder.append(type.getObjectType());
      builder.append(") TEXT('");
      builder.append(text);
      builder.append("')");

      return checkForMessage("CPC2103", getConnection().executeCommand(builder.toString()));
   }

   public static class JUserSpaceBuilder
   {
      private final JConnection connection;
      private final String name;
      private final String library;
      private int length;
      private boolean isAutoExtendible;
      private byte initialValue;

      public JUserSpaceBuilder(final JConnection connection, final String name, final String library) {
         this.connection = connection;
         this.name = name;
         this.library = library;
      }

      public JUserSpaceBuilder length(final int length) {
         this.length = length;
         return this;
      }

      public JUserSpaceBuilder autoExtendible(final boolean autoExtendible) {
         isAutoExtendible = autoExtendible;
         return this;
      }

      public JUserSpaceBuilder initialValue(final byte initialValue) {
         this.initialValue = initialValue;
         return this;
      }

      public JUserSpace build() {
         return new JUserSpace(this);
      }
   }
}
