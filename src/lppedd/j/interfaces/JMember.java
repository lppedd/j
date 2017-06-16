package lppedd.j.interfaces;

import java.util.List;
import smi.workitem.abstracts.SMIWorkItem;

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
   public void releaseResources();

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
   public SMIWorkItem[] getWorkItems();

   /**
    * Ritorna un work item contenuto nel membro sorgente.
    *
    * @param number Il numero di work item
    *
    * @return L'istanza del work item o {@code null} se non e' presente
    */
   public SMIWorkItem getWorkItem(final int number);

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
   public SMIWorkItem removeWorkItem(final int number, final boolean cleanAll);

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
