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
package smi.workitem;

import java.util.ArrayList;
import java.util.List;

import lppedd.j.api.exceptions.JInvalidWorkItemException;
import lppedd.j.api.members.JMember;

/**
 * @author Edoardo Luppi
 */
public class SmiSqlWorkItem extends SmiAbstractWorkItem
{
   public SmiSqlWorkItem(final JMember parent, final String line) throws JInvalidWorkItemException {
      super(parent, line);
   }

   public SmiSqlWorkItem(
           final JMember parent,
           final int index,
           final int number,
           final String user,
           final long date,
           final boolean work) {
      super(parent, index, number, user, date, work);
   }

   @Override
   public List<String> getSource() {
      final StringBuilder builder = new StringBuilder(120);
      builder.append("--");
      builder.append(_index);
      builder.append(_work ? "§" : " ");
      builder.append("* ");
      builder.append(_user);
      builder.append(" ");
      builder.append(_date);
      builder.append(" WORKITEM ");
      builder.append(_number);

      final List<String> source = new ArrayList<>(1 + _text.size());
      source.add(builder.toString());

      builder.replace(2, 6, "    ");

      for (int i = 0; i < _text.size(); i++) {
         builder.replace(9, builder.length(), _text.get(i));
         source.add(builder.toString());
      }

      return source;
   }

   @Override
   public String[] getElements(final String line) {
      final String[] elements = new String[5];
      elements[INDEX] = line.substring(2, 6);
      elements[WORK] = line.substring(6, 7);
      elements[USER] = line.substring(9, 15);
      elements[DATE] = line.substring(16, 24);
      elements[NUMBER] = line.substring(34, 44).trim();

      return elements;
   }
}
