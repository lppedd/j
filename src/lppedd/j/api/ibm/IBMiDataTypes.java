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
package lppedd.j.api.ibm;

import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400Text;

/**
 * @author Edoardo Luppi
 */
public interface IBMiDataTypes
{
   public AS400Text CHAR1 = new AS400Text(1);
   public AS400Text CHAR3 = new AS400Text(3);
   public AS400Text CHAR8 = new AS400Text(8);
   public AS400Text CHAR10 = new AS400Text(10);
   public AS400Text CHAR13 = new AS400Text(13);
   public AS400Text CHAR20 = new AS400Text(20);
   public AS400Text CHAR36 = new AS400Text(36);
   public AS400Text CHAR50 = new AS400Text(50);
   public AS400Text CHAR258 = new AS400Text(258);
   public AS400Bin4 BIN4 = new AS400Bin4();
}
