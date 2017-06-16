package lppedd.j;

import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lppedd.j.abstracts.JAbstractMember;
import lppedd.j.exceptions.JInvalidWorkItemException;
import lppedd.j.interfaces.JObject;
import smi.workitem.SMIPgmWorkItem;
import smi.workitem.abstracts.SMIWorkItem;

import static com.ibm.as400.access.PrintObject.ATTR_JOBNAME;
import static com.ibm.as400.access.PrintObject.ATTR_MFGTYPE;
import static com.ibm.as400.access.PrintObject.ATTR_SPOOLFILE;
import static com.ibm.as400.access.PrintObject.ATTR_WORKSTATION_CUST_OBJECT;
import static java.lang.Long.parseLong;
import static lppedd.j.JConnection.getInstance;
import static lppedd.j.JObjectFactory.get;
import static lppedd.j.enums.JType.MODULE;
import static lppedd.j.enums.JType.PGM;
import static lppedd.j.misc.JUtil.isSpecification;
import static lppedd.misc.Util.indexOfAnyBut;
import static lppedd.misc.Util.isInteger;

/**
 * @author Edoardo Luppi
 */
public class JProgramMember extends JAbstractMember
{
    public JProgramMember(final String name, final String object, final String library) {
        super(name, object, library);
    }

    /**
     * Controllo che i prototipi definiti nel sorgente siano coerenti a quelli
     * dei programmi chiamati
     */
    public static void checkParameters(final List<JProgramMember> programs) {
        final JConnection connection = getInstance();

        // OCM00C ->   OCM00C : prototipo
        //             AVTKCC : prototipo
        //             OCM01C : prototipo
        final Map<String, Map<String, List<String>>> pgmNameToPgmList = new HashMap<>();

        for (final JProgramMember p : programs) {
            // Salvo la data e l'ora prima di compilare
            String date;
            String time;

            try {
                // Tento reperendo la data di sistema
                date = "1" + connection.getSystemValue("QDATE").toString().substring(2).replace("-", "");
                time = connection.getSystemValue("QTIME").toString().replace(":", "");
            } catch (final IndexOutOfBoundsException e) {
                e.printStackTrace();

                // Se non riesco, provo con la data del sistema operativo, anche se potrebbe essere sbagliata
                final String dateTime = "1" + new SimpleDateFormat("yyMMddHHmmss").format(new Date());
                date = dateTime.substring(0, 7);
                time = dateTime.substring(7);
            }

            // Compilo il programma
            if (!p.compile("QTEMP")) {
                return;
            }

            // Reperisco e leggo lo spool
            final SpooledFileList spoolList = new SpooledFileList(connection.getAS400());

            SpooledFile spool = null;
            String spoolName = null;
            String spoolJobName = null;

            try {
                spoolList.setAttributesToRetrieve(new int[]{
                    ATTR_SPOOLFILE, ATTR_JOBNAME});
                spoolList.setUserFilter(connection.getAS400().getUserId());
                spoolList.setStartDateFilter(date);
                spoolList.setEndDateFilter(date);
                spoolList.setStartTimeFilter(time);
                spoolList.openSynchronously();

                spool = (SpooledFile) spoolList.getObject(spoolList.size() - 1);
                spoolName = spool.getName();
                spoolJobName = spool.getJobName();

                // Controllo se lo spool che sto per leggere e' quello giusto.
                // Se non lo e' passo al prossimo programma
                if (!p._name.equals(spoolName) || !p._name.equals(spoolJobName)) {
                    continue;
                }

                final PrintParameterList parameters = new PrintParameterList();
                parameters.setParameter(ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST");
                parameters.setParameter(ATTR_MFGTYPE, "*WSCST");

                final Map<String, List<String>> protoList = new HashMap<>(16);
                final Map<String, String> plistMap = new HashMap<>(16);
                String plistName = null;

                final BufferedReader reader = new BufferedReader(new InputStreamReader(spool.getTransformedInputStream(parameters)));

                for (String line = null; (line = reader.readLine()) != null;) {
                    // Trasformo tutta la riga in maiuscolo
                    line = line.toUpperCase();

                    // Chiamata ad un programma, quindi memorizzo il suo nome e la sua eventuale
                    // corrispondente PLIST
                    if (line.indexOf("CALL") == 28) {
                        final String programName = line.substring(28, 37).trim();

                        if (!pgmNameToPgmList.containsKey(programName)) {
                            pgmNameToPgmList.put(programName, new HashMap<>(16));
                        }

                        // Salvo un eventuale nome di PLIST, per poi reperire i suoi parametri man mano che scorro il codice
                        // Se non lo trovo, il nome di PLIST e' il nome del programma chiamato stesso
                        plistName = line.substring(52, 62).trim();

                        if (plistName.isEmpty()) {
                            plistName = programName;
                        }

                        plistMap.put(programName, plistName);
                    }

                    // PLIST
                    if (line.indexOf("PLIST") == 27) {
                        plistName = line.substring(13, 23).trim();
                        protoList.put(plistName, new ArrayList<>(12));
                    }

                    // Parametro di una PLIST
                    if (line.indexOf("PARM") == 27) {
                        plistMap.get(plistName);
                        protoList.get(plistName).add(line.substring(52, 62).trim());
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                if (spoolList != null) {
                    spoolList.close();
                }

                // Cancello lo spool
                if (spool != null) {
                    final StringBuilder builder = new StringBuilder("100");
                    builder.delete(0, builder.length());
                    builder.append("DLTSPLF FILE(QPUOPRTF) JOB(");
                    builder.append(spool.getJobNumber());
                    builder.append("/");
                    builder.append(spool.getJobUser());
                    builder.append("/");
                    builder.append(spoolJobName);
                    builder.append(")");

                    connection.executeCommand(builder.toString());
                }
            }
        }
    }

    @Override
    public boolean compile(final String library) {
        final JConnection connection = getConnection();
        final long time = parseLong(connection.getSystemValue("QDATETIME").toString().substring(0, 14) + "000");

        final StringBuilder builder = new StringBuilder(156);
        builder.append("ABCRTOBJ MEM(");
        builder.append(_name);
        builder.append(") SRC(");
        builder.append(_attribute);
        builder.append(") FILE(");
        builder.append(_object);
        builder.append(") LIB(");
        builder.append(_library);
        builder.append(") OBJ(");
        builder.append(_name);
        builder.append(") LIO(");
        builder.append(library);
        builder.append(") LIM(");
        builder.append(library);
        builder.append(") OBT(*ALL) AGG(*NO) BCH(*NO)");

        connection.executeCommand(builder.toString());

        final JObject program = get(_name, library, PGM);
        final JObject module = get(_name, library, MODULE);

        if (!(module instanceof JNullObject)) {
            return time <= program.getCreationDateTime() && time < module.getCreationDateTime();
        }

        return time <= program.getCreationDateTime();
    }

    @Override
    protected void inspectForWorkItems() {
        // Se necessario pulisco la lista prima di popolarla
        if (_workItemList.size() > 0) {
            _workItemList.clear();
        }

        for (int i = 0; i < _source.size(); i++) {
            final String line = _source.get(i);

            // Sono alla fine dei work item in testatina? Se si, salvo la posizione
            if (!_workItemList.isEmpty() && PATTERN_EOC.matcher(line).matches()) {
                _workItemsEnd = i;
                break;
            }

            // Se la linea corrisponde ad un un work item, lo aggiungo alla lista
            // e mi salvo la posizioni in modo da poter, successivamente, andare ad inserire il suo testo
            if (PATTERN_MOD.matcher(line).matches() && isInteger(line.substring(15, 23))) {
                try {
                    _workItemList.add(new SMIPgmWorkItem(this, line), i);
                } catch (final JInvalidWorkItemException e) {
                    e.printStackTrace();
                }
            }
        }

        // Completo i work item con i rispettivi testi
        final int size = _workItemList.size() - 1;

        for (int i = 0; i <= size; i++) {
            final SMIWorkItem workItem = _workItemList.getWorkItem(i);

            for (int k = _workItemList.getPosition(i) + 1; k < (i == size ? _workItemsEnd : _workItemList.getPosition(i + 1)); k++) {
                workItem.appendText(_source.get(k).substring(8));
            }
        }
    }

    @Override
    protected String backToThePast(final String line, final String modNumber) {
        if (!line.startsWith(modNumber) || line.length() < 7) {
            return null;
        }

        // Verifico se c'e' un EX
        final int ex = line.toUpperCase().indexOf("EX ") + 3;

        if (ex < 7) {
            // Vado tranquillo e rimuovo la riga
            return null;
        }

        // Risalgo al vecchio numero di modifica, che devo ripristinare
        final int mod = indexOfAnyBut(line.substring(ex), " ");

        // Inzio a costruire la riga
        final StringBuilder builder = new StringBuilder(120);
        builder.append(line.substring(ex + mod, ex + mod + 4));
        builder.append(" ");

        final char spec = line.charAt(5);

        if (isSpecification(spec) || line.charAt(6) == '*') {
            // E' posizionale
            final String oldLine = line.substring(ex + mod + 4).trim();

            if (isSpecification(oldLine.charAt(0)) && oldLine.charAt(1) == ' ') {
                // La vecchia riga inizia con una specifica di calcolo, quindi teoricamente e' gia' formattata
                // Poi vedremo sperimentando che casi ci sono...
                builder.append(oldLine);
            } else {
                // Recupero la specifica di calcolo dalla posizione 5
                builder.append(spec);
                // TODO verificare se la vecchia riga inizia con un opcode (es CHAIN, READ, DOW, oppure OR, AND...)
            }
        } else {
            // E' free
            // TODO aggiungere la parte per il free
        }

        return line;
    }
}
