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
     * @param library Libreria di destinazione
     * @param name Oggetto di destinazione
     * @param overwrite Sovrascrive l'oggetto se gia' esistente
     */
    public boolean copy(final String library, final String name, final boolean overwrite);

    /**
     * Sposta il membro in un'altra path.
     *
     * @param library Libreria di destinazione
     * @param name Oggetto di destinazione
     * @param overwrite Sovrascrive l'oggetto se gia' esistente
     */
    public boolean move(final String library, final String name, final boolean overwrite);
}
