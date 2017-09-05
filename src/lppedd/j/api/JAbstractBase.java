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
package lppedd.j.api;

import lppedd.j.api.ibm.IBMiDataTypes;

/**
 * Represents every IBMi object.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractBase implements JBase, IBMiDataTypes
{
   private final JConnection connection;

   protected String name = "";
   protected String library = "";

   protected String originalName = "";
   protected JType type = JType.NONE;
   protected String attribute = "";
   protected String text = "";
   protected String creator = "";
   protected long creationDateTime;
   
   protected boolean isNameChanged;
   protected boolean isAttributeChanged;
   protected boolean isTextChanged;
   protected boolean isSourceChanged;
   
   // MBRD0300 -> QUSRMBRD
   protected byte[] MBRD0300;
   
   protected JAbstractBase(final JConnection connection, final String name, final String library, final JType type) {
      this.connection = connection;
      originalName = name;
      this.name = name.trim().toUpperCase();
      this.library = library.trim().toUpperCase();
      this.type = type;
   }

   @Override
   public boolean persist() {
      if (!exists()) {
         return false;
      }
      
      if (isNameChanged) {
         if (!performSetName()) {
            return false;
         }
         
         originalName = getName();
         isNameChanged = false;
      }
      
      if (isTextChanged) {
         if (!performSetText()) {
            return false;
         }
         
         isTextChanged = false;
      }
      
      return true;
   }
   
   @Override
   public String getLibrary() {
      return library;
   }
   
   @Override
   public JType getType() {
      return type;
   }
   
   @Override
   public String getAttribute() {
      return attribute;
   }
   
   @Override
   public String getText() {
      return text;
   }
   
   @Override
   public String getCreator() {
      return creator;
   }
   
   @Override
   public long getCreationDateTime() {
      return creationDateTime;
   }
   
   @Override
   public JConnection getConnection() {
      return connection;
   }
   
   @Override
   public void setName(String name) {
      name = name.trim().toUpperCase();
      
      if (!this.name.equalsIgnoreCase(name)) {
         this.name = name;
         isNameChanged = true;
      }
   }
   
   @Override
   public void setText(final String text) {
      if (!this.text.equalsIgnoreCase(text)) {
         this.text = text;
         isTextChanged = true;
      }
   }
   
   /**
    * Execute the IBMi command to persist the name change.
    */
   protected abstract boolean performSetName();
   
   /**
    * Execute the IBMi command to persist the description text change.
    */
   protected abstract boolean performSetText();
   
   @Override
   public String toString() {
      return getIfsPath();
   }
   
   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof JAbstractBase)) {
         return false;
      }
      
      return getIfsPath().equals(((JBase) object).getIfsPath());
   }
   
   @Override
   public int hashCode() {
      return getIfsPath().hashCode();
   }
}
