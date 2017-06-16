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
package j.abstracts;

import j.JConnection;
import j.enums.JType;
import j.interfaces.AS400DataTypes;
import j.interfaces.JBase;

import static j.JConnection.getInstance;
import static j.enums.JType.NONE;

/**
 * Represents every IBMi object.
 *
 * @author Edoardo Luppi
 */
public abstract class JAbstractBase implements JBase, AS400DataTypes
{
   // Connessione AS400 al quale l'oggetto appartiene
   private static final JConnection _CONNECTION = getInstance();

   // Path dell'oggetto
   protected String _name = "";
   protected String _library = "";

   // Attributi dell'oggetto
   protected String _originalName = "";
   protected JType _type = NONE;
   protected String _attribute = "";
   protected String _text = "";
   protected String _creator = "";
   protected long _creationDateTime;

   // Indicatori di stato dell'oggetto
   protected boolean _changedName;
   protected boolean _changedAttribute;
   protected boolean _changedText;
   protected boolean _changedSource;

   // Formato MBRD0300 -> QUSRMBRD
   protected byte[] _MBRD0300;

   protected JAbstractBase(final String name, final String library, final JType type) {
      _name = name.trim().toUpperCase();
      _library = library.trim().toUpperCase();
      _type = type;
      _originalName = _name;
   }

   @Override
   public boolean commitChanges() {
      if (!exists()) {
         return false;
      }

      if (_changedName) {
         if (!performSetName()) {
            return false;
         }

         _originalName = getName();
         _changedName = false;
      }

      if (_changedText) {
         if (!performSetText()) {
            return false;
         }

         _changedText = false;
      }

      return true;
   }

   @Override
   public String getLibrary() {
      return _library;
   }

   @Override
   public JType getType() {
      return _type;
   }

   @Override
   public String getAttribute() {
      return _attribute;
   }

   @Override
   public String getText() {
      return _text;
   }

   @Override
   public String getCreator() {
      return _creator;
   }

   @Override
   public long getCreationDateTime() {
      return _creationDateTime;
   }

   @Override
   public JConnection getConnection() {
      return _CONNECTION;
   }

   @Override
   public void setName(String name) {
      name = name.trim().toUpperCase();

      if (!_name.equalsIgnoreCase(name)) {
         _name = name;
         _changedName = true;
      }
   }

   @Override
   public void setText(final String text) {
      if (!_text.equalsIgnoreCase(text)) {
         _text = text;
         _changedText = true;
      }
   }

   /**
    * Lancia il comando AS400 per rendere effettiva la modifica del nome
    * dell'oggetto
    */
   protected abstract boolean performSetName();

   /**
    * Lancia il comando AS400 per rendere effettiva la modifica del testo
    * descrittivo dell'oggetto
    */
   protected abstract boolean performSetText();

   @Override
   public String toString() {
      return getPath();
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof JAbstractBase)) {
         return false;
      }

      return getPath().equals(((JAbstractBase) object).getPath());
   }

   @Override
   public int hashCode() {
      return getPath().hashCode();
   }
}
