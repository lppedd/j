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

/**
 * @author Edoardo Luppi
 */
public interface JBase
{
   /**
    * Returns if the object exists in the system.
    */
   public boolean exists();
   
   /**
    * Persists all changes made to the object.
    */
   public boolean persist();
   
   /**
    * Delete the object.
    */
   public boolean delete();
   
   /**
    * Returns the object IFS path.
    */
   public String getIfsPath();
   
   /**
    * Returns the object qualified path (10 characters for the library name and 10 characters for the object name).
    */
   public String getQualifiedPath();
   
   /**
    * Returns the object library.
    */
   public String getLibrary();
   
   /**
    * Returns the object name.
    */
   public String getName();
   
   /**
    * Returns the object type.
    */
   public JType getType();
   
   /**
    * Returns the object attribute.
    */
   public String getAttribute();
   
   /**
    * Returns the object description text.
    */
   public String getText();
   
   /**
    * Returns the object creator.
    */
   public String getCreator();
   
   /**
    * Returns the object creation time.
    */
   public long getCreationDateTime();
   
   /**
    * Returns the connection to the system where the object is located.
    */
   public JConnection getConnection();
   
   /**
    * Set the object name.
    *
    * @param name
    *        The new name
    */
   public void setName(final String name);
   
   /**
    * Set the object description text.
    *
    * @param text
    *        The new description text
    */
   public void setText(final String text);
}
