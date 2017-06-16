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
package lppedd.j.interfaces;

/**
 * Metodi di base per un oggetto AS400
 *
 * @author Edoardo Luppi
 */
public interface JObject extends JBase
{
   /**
    * Copia il membro in un'altra path.<br>
    * E' necessario istanziare un altro oggetto con riferimento alla nuova
    * posizione.
    *
    * @param library   Libreria di destinazione
    * @param name      Oggetto di destinazione
    * @param overwrite Sovrascrive l'oggetto se gia' esistente
    */
   public boolean copy(final String library, final String name, final boolean overwrite);

   /**
    * Sposta il membro in un'altra path.
    *
    * @param library   Libreria di destinazione
    * @param name      Oggetto di destinazione
    * @param overwrite Sovrascrive l'oggetto se gia' esistente
    */
   public boolean move(final String library, final String name, final boolean overwrite);
}
