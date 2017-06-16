package lppedd.j.interfaces;

import lppedd.j.JConnection;
import lppedd.j.enums.JType;

/**
 * @author Edoardo Luppi
 */
public interface JBase
{
   /**
    * Ritorna l'esistenza dell'oggetto.
    */
   public boolean exists();

   /**
    * Salva l'oggetto e quindi rende effettive tutte le modifiche effettuate.
    */
   public boolean commitChanges();

   /**
    * Elimina l'oggetto.
    */
   public boolean delete();

   /**
    * Ritorna la path IFS dell'oggetto.
    */
   public String getPath();

   /**
    * Ritorna la path qualificata (10 caratteri per la libreria e 10 per
    * l'oggetto).
    */
   public String getQualifiedPath();

   /**
    * Ritorna la libreria dell'oggetto.
    */
   public String getLibrary();

   /**
    * Ritorna il nome dell'oggetto.
    */
   public String getName();

   /**
    * Ritorna il tipo dell'oggetto.
    */
   public JType getType();

   /**
    * Ritorna l'attributo dell'oggetto.
    */
   public String getAttribute();

   /**
    * Ritorna il testo descrittivo dell'oggetto.
    */
   public String getText();

   /**
    * Ritorna l'utente che ha creato l'oggetto.
    */
   public String getCreator();

   /**
    * Ritorna la data di creazione dell'oggetto.
    */
   public long getCreationDateTime();

   /**
    * Returns the connection to the system where the object is located.
    */
   public JConnection getConnection();

   /**
    * Imposta il nome dell'oggetto.
    *
    * @param name Nuovo nome
    */
   public void setName(final String name);

   /**
    * Imposta il testo descrittivo dell'oggetto.
    *
    * @param text Nuovo testo
    */
   public void setText(final String text);
}
