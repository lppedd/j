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

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * Rappresenta un indice di modifica SMI di un membro sorgente
 *
 * @author Edoardo Luppi
 */
public abstract class SmiAbstractWorkItem
{
   public static final int INDEX = 0;
   public static final int WORK = 4;
   public static final int USER = 2;
   public static final int DATE = 3;
   public static final int NUMBER = 1;

   protected JMember _parent;
   protected int _index;
   protected int _number;
   protected final String _user;
   protected long _date;
   protected final boolean _work;
   protected final List<String> _text = new ArrayList<>(8);

   public SmiAbstractWorkItem(final JMember parent, final String line) throws JInvalidWorkItemException {
      final String[] elements = getElements(line);

      try {
         _parent = parent;
         _index = parseInt(elements[INDEX]);
         _work = "§".equals(elements[WORK]);
         _user = elements[USER];
         _date = parseLong(elements[DATE]);
         _number = parseInt(elements[NUMBER]);
      } catch (final NumberFormatException e) {
         throw new JInvalidWorkItemException("Work item del membro " + _parent.getIfsPath() + " invalido");
      }
   }

   public SmiAbstractWorkItem(
           final JMember parent,
           final int index,
           final int number,
           final String user,
           final long date,
           final boolean work) {
      _parent = parent;
      _index = index;
      _work = work;
      _user = user;
      _date = date < 20000101 || date > 20203112 ? parseLong(_parent.getConnection().getSystemValue("QDATE").toString().replace("-", "")) : date;
      _number = number;
   }

   public JMember getParent() {
      return _parent;
   }

   public int getIndex() {
      return _index;
   }

   public int getNumber() {
      return _number;
   }

   public String getUser() {
      return _user;
   }

   public long getDate() {
      return _date;
   }

   public String getText() {
      final int textSize = _text.size();
      final StringBuilder builder = new StringBuilder(textSize > 0 ? 100 * textSize : 0);

      _text.forEach((line) -> {
         builder.append(line);
      });

      return builder.toString();
   }

   public boolean inWork() {
      return _work;
   }

   public void appendText(final String line) {
      _text.add(line);
   }

   public void clearText() {
      _text.clear();
   }

   public abstract List<String> getSource();

   public abstract String[] getElements(final String line);

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof SmiAbstractWorkItem)) {
         return false;
      }

      return _number == ((SmiAbstractWorkItem) object)._number;
   }

   @Override
   public int hashCode() {
      return _number;
   }
}
