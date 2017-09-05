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
package lppedd.j.api.files;

import java.util.List;

/**
 * @author Edoardo Luppi
 */
public class JRecordFormat
{
   private final JFile _parent;
   private final String _name;
   private final String _description;
   private final List<JField> _fields;

   public JRecordFormat(final JFile parent, final String name, final String description, final List<JField> fields) {
      _parent = parent;
      _name = name;
      _description = description;
      _fields = fields;
   }

   public JFile getParent() {
      return _parent;
   }

   public String getName() {
      return _name;
   }

   public String getDescription() {
      return _description;
   }

   public List<JField> getFields() {
      return _fields;
   }

   @Override
   public String toString() {
      return _name;
   }
}
