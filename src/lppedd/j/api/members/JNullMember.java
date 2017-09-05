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
package lppedd.j.api.members;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lppedd.j.api.JConnection;
import lppedd.j.api.JType;
import smi.workitem.SmiAbstractWorkItem;

/**
 * @author Edoardo Luppi
 */
public class JNullMember implements JMember
{
   private static JNullMember instance = null;
   
   /**
    * Returns the only one ever instance of the class.
    */
   public static synchronized JNullMember getInstance() {
      if (instance == null) {
         instance = new JNullMember();
      }
      
      return instance;
   }
   
   private JNullMember() {
      //
   }
   
   @Override
   public boolean exists() {
      return false;
   }
   
   @Override
   public boolean loadSource() {
      return false;
   }
   
   @Override
   public void dispose() {
      //
   }
   
   @Override
   public boolean persist() {
      return false;
   }
   
   @Override
   public boolean create(final boolean overwrite) {
      return false;
   }
   
   @Override
   public boolean copy(final String library, final String object, final String name, final boolean overwrite) {
      return false;
   }
   
   @Override
   public boolean move(final String library, final String object, final String name, final boolean overwrite) {
      return false;
   }
   
   @Override
   public boolean delete() {
      return false;
   }
   
   @Override
   public boolean compile(final String library) {
      return false;
   }
   
   @Override
   public SmiAbstractWorkItem[] getWorkItems() {
      return new SmiAbstractWorkItem[0];
   }
   
   @Override
   public Optional<SmiAbstractWorkItem> getWorkItem(final int number) {
      return null;
   }
   
   @Override
   public boolean addWorkItem(
         final int index,
         final int number,
         final String username,
         final long date,
         final String text,
         final boolean work) {
      return false;
   }
   
   @Override
   public boolean putWorkItem(
         final int number,
         final String username,
         final long date,
         final String text,
         final boolean work) {
      return false;
   }
   
   @Override
   public SmiAbstractWorkItem removeWorkItem(final int number, final boolean clean) {
      return null;
   }
   
   @Override
   public String getIfsPath() {
      return "";
   }
   
   @Override
   public String getQualifiedPath() {
      return "";
   }
   
   @Override
   public String getLibrary() {
      return "";
   }
   
   @Override
   public String getObject() {
      return "";
   }
   
   @Override
   public String getName() {
      return "";
   }
   
   @Override
   public JType getType() {
      return JType.NONE;
   }
   
   @Override
   public String getAttribute() {
      return "";
   }
   
   @Override
   public String getText() {
      return "";
   }
   
   @Override
   public String getCreator() {
      return "";
   }
   
   @Override
   public long getCreationDateTime() {
      return 0;
   }
   
   @Override
   public List<String> getSource() {
      return Collections.emptyList();
   }
   
   @Override
   public void setName(final String name) {
      //
   }
   
   @Override
   public void setSourceType(final String attribute) {
      //
   }
   
   @Override
   public void setText(final String text) {
      //
   }
   
   @Override
   public void setSource(final List<String> source) {
      //
   }
   
   @Override
   public JConnection getConnection() {
      return null;
   }
}
