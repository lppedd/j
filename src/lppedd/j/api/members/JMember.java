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

import java.util.List;
import java.util.Optional;

import lppedd.j.api.JBase;
import smi.workitem.SmiAbstractWorkItem;

/**
 * Metodi di base per un membro di un file fisico sorgente AS400
 *
 * @author Edoardo Luppi
 */
public interface JMember extends JBase
{
   /**
    * Carica il codice sorgente del membro.
    */
   public boolean loadSource();

   /**
    * Rilascia tutta la memoria utilizzata dall'istanza dell'oggetto.
    */
   public void dispose();

   /**
    * Crea il membro sorgente.
    *
    * @param overwrite Specifica se si vuole sovrascrivere un eventuale membro
    *                  gia' esistente
    */
   public boolean create(final boolean overwrite);

   /**
    * Copia il membro in un'altra path.<br>
    * E' necessario istanziare un altro membro con riferimento alla nuova
    * posizione.
    *
    * @param library   Libreria di destinazione
    * @param object    Oggetto di destinazione
    * @param name      Membro di destinazione
    * @param overwrite Sovrascrive il membro se gia' esistente.
    */
   public boolean copy(final String library, final String object, final String name, final boolean overwrite);

   /**
    * Sposta il membro in un'altra path.
    *
    * @param library   Libreria di destinazione
    * @param object    Oggetto di destinazione
    * @param name      Membro di destinazione
    * @param overwrite Sovrascrive il membro se gia' esistente
    */
   public boolean move(final String library, final String object, final String name, final boolean overwrite);

   /**
    * Compila il membro sorgente.
    *
    * @param library Libreria dove posizionare l' oggetto compilato
    */
   public boolean compile(final String library);

   /**
    * Ritorna tutti i work item del membro sorgente.
    */
   public SmiAbstractWorkItem[] getWorkItems();

   /**
    * Ritorna un work item contenuto nel membro sorgente.
    *
    * @param number Il numero di work item
    *
    * @return L'istanza del work item o {@code null} se non e' presente
    */
   public Optional<SmiAbstractWorkItem> getWorkItem(final int number);

   /**
    * Aggiunge un work item alla testatina del membro sorgente.
    */
   public boolean putWorkItem(
           final int number, final String username, final long date, final String text, final boolean work);

   /**
    * Aggiunge un work item alla testatina del membro sorgente.
    */
   public boolean addWorkItem(
           final int index,
           final int number,
           final String username,
           final long date,
           final String text,
           final boolean work);

   /**
    * Rimuove un work item dalla testatina del membro sorgente.
    *
    * @param number   Il numero del work item
    * @param cleanAll Indica se rimuovere anche tutte le specifiche collegate
    *
    * @return L'istanza del work item rimosso o {@code null} se non presente
    */
   public SmiAbstractWorkItem removeWorkItem(final int number, final boolean cleanAll);

   /**
    * Ritorna l'oggetto che contiene il membro sorgente.
    */
   public String getObject();

   /**
    * Ritorna il codice sorgente del membro sorgente.
    */
   public List<String> getSource();

   /**
    * Imposta il codice sorgente del membro sorgente.
    *
    * @param source Codice sorgente
    */
   public void setSource(final List<String> source);

   /**
    * Imposta l'attributo del membro sorgente.
    *
    * @param attribute Nuovo attributo
    */
   public void setSourceType(final String attribute);
}
