package lppedd.j;

import java.util.List;
import lppedd.j.enums.JType;
import lppedd.j.interfaces.JMember;
import smi.workitem.abstracts.SMIWorkItem;

import static java.util.Collections.EMPTY_LIST;
import static lppedd.j.enums.JType.NONE;

/**
 * @author Edoardo Luppi
 */
public class JNullMember implements JMember
{
    private static final JConnection _CONNECTION = JConnection.getInstance();
    private static JNullMember _instance = null;

    /**
     * Returns the only one ever instance of the class.
     */
    public static synchronized JNullMember getInstance() {
        if (_instance == null) {
            _instance = new JNullMember();
        }

        return _instance;
    }

    private JNullMember() {
        //
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean loadSource() {
        return false;
    }

    @Override
    public void releaseResources() {
        //
    }

    @Override
    public boolean commitChanges() {
        return false;
    }

    @Override
    public boolean create(final boolean overwrite) {
        return false;
    }

    @Override
    public boolean copy(final String library, final String object, final String name, final boolean overwrite) {
        return false;
    }

    @Override
    public boolean move(final String library, final String object, final String name, final boolean overwrite) {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean compile(final String library) {
        return false;
    }

    @Override
    public SMIWorkItem[] getWorkItems() {
        return new SMIWorkItem[0];
    }

    @Override
    public SMIWorkItem getWorkItem(final int number) {
        return null;
    }

    @Override
    public boolean addWorkItem(
            final int index,
            final int number,
            final String username,
            final long date,
            final String text,
            final boolean work) {
        return false;
    }

    @Override
    public boolean putWorkItem(
            final int number, final String username, final long date, final String text, final boolean work) {
        return false;
    }

    @Override
    public SMIWorkItem removeWorkItem(final int number, final boolean clean) {
        return null;
    }

    @Override
    public String getPath() {
        return "";
    }

    @Override
    public String getQualifiedPath() {
        return "";
    }

    @Override
    public String getLibrary() {
        return "";
    }

    @Override
    public String getObject() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public JType getType() {
        return NONE;
    }

    @Override
    public String getAttribute() {
        return "";
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public String getCreator() {
        return "";
    }

    @Override
    public long getCreationDateTime() {
        return 0;
    }

    @Override
    public List<String> getSource() {
        return EMPTY_LIST;
    }

    @Override
    public void setName(final String name) {
        //
    }

    @Override
    public void setSourceType(final String attribute) {
        //
    }

    @Override
    public void setText(final String text) {
        //
    }

    @Override
    public void setSource(final List<String> source) {
        //
    }

    @Override
    public JConnection getConnection() {
        return _CONNECTION;
    }
}
