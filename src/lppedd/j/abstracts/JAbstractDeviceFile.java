package lppedd.j.abstracts;

import lppedd.j.interfaces.JDeviceFile;

/**
 * @author Edoardo Luppi
 */
public abstract class JAbstractDeviceFile extends JAbstractFile implements JDeviceFile
{
    protected JAbstractDeviceFile(final String name, final String library) {
        super(name, library);
    }
}
