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
