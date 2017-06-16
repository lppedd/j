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
package j.misc;

import java.util.Objects;

/**
 * @author Edoardo Luppi
 */
public final class Triple<F, S, T>
{
   public final F first;
   public final S second;
   public final T third;

   public Triple(final F first, final S second, final T third) {
      this.first = first;
      this.second = second;
      this.third = third;
   }

   @Override
   public boolean equals(final Object object) {
      if (!(object instanceof Triple)) {
         return false;
      }

      final Triple<?, ?, ?> p = (Triple<?, ?, ?>) object;
      return Objects.equals(p.first, first) && Objects.equals(p.second, second) && Objects.equals(p.third, third);
   }

   @Override
   public int hashCode() {
      return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
   }

   public static <F, S, T> Triple<F, S, T> of(F f, S s, T t) {
      return new Triple<>(f, s, t);
   }
}
