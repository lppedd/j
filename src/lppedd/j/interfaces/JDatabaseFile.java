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

import java.util.List;
import lppedd.j.JJournal;

public interface JDatabaseFile extends JFile
{
   /**
    * Ritorna la lista dei membri del file
    */
   public String[] getMembers();

   /**
    * Ritorna tutti i file dipendenti.
    */
   public List<JObject> getDatabaseRelations();

   /**
    * Ritorna il giornale al quale e' iscritto il file fisico.
    */
   public JJournal getJournal();

   /**
    * Genera uno statement SQL data la definizione del file.
    *
    * @param library Libreria membro di destinazione
    * @param object  Oggetto membro di destinazione
    * @param member  Nome membro di destinazione
    * @param type    Valido solo per le viste logiche (es: VIEW, INDEX).<br>
    * Se e' un file fisico viene assunto di default il valore TABLE
    */
   public JMember toSql(final String library, final String object, final String member, final String type);
}
