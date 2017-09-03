package linky;

import java.io.IOException;
import java.io.OutputStream;

import linky.api.XmlNode;
import linky.api.XmlNodeData;
import org.scanner.FastScanner;
import org.scanner.MoveEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ProcessLinkyFileChunk {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessLinkyFileChunk.class);
    private final static byte[] getValue(FastScanner scannerFastString, R151DelimiterEnum delimiter){
        try {
            return scannerFastString.retrieveNextToken( delimiter.getTagStart(), delimiter.getTagEnd(), MoveEnum.RIGHT_FROM_ELEMENT );
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    final public static void processChunk_En_Tete_Flux(byte[] chunk, OutputStream outputStream, XmlNode node, XmlNodeData<R151ExpectedKeysEnum, byte[]> dataNode) {
        FastScanner scannerFastString = new FastScanner(chunk);
        node.setAndGetData(dataNode).put(node,
                R151ExpectedKeysEnum.Numero_Abonnement, getValue(scannerFastString, R151DelimiterEnum.Numero_Abonnement));        
    }
    
    final public static void processChunk_Donnees_Releves(byte[] chunk, OutputStream outputStream, XmlNode node, XmlNodeData<R151ExpectedKeysEnum, byte[]> dataNode) {
        FastScanner scannerFastString = new FastScanner(chunk);
        node.setAndGetData(dataNode)
            .put(node, R151ExpectedKeysEnum.Date_Releve, getValue(scannerFastString, R151DelimiterEnum.Date_Releve));
        
        byte[] Classe_Temporelle_Distributeur = getValue(scannerFastString, R151DelimiterEnum.Classe_Temporelle_Distributeur);

        if (Classe_Temporelle_Distributeur==null)
            return;
        FastScanner scannerFastStringClasse_Temporelle_Distributeur = new FastScanner(Classe_Temporelle_Distributeur);



        dataNode.put(node, R151ExpectedKeysEnum.Releve_Value, getValue(scannerFastStringClasse_Temporelle_Distributeur,  R151DelimiterEnum.Valeur) );
        
        byte[] Puissance_Maximale = getValue(scannerFastString, R151DelimiterEnum.Puissance_Maximale);
        FastScanner scannerFastStringPuissance_Maximale = new FastScanner(Puissance_Maximale);

        dataNode.put(node, R151ExpectedKeysEnum.Puissance_Maximale_Value, getValue(scannerFastStringPuissance_Maximale, R151DelimiterEnum.Valeur));
                
    }
    final public static void processChunk_Id_RPM(byte[] Id_PRM, OutputStream outputStream, XmlNode node, XmlNodeData<R151ExpectedKeysEnum, byte[]> dataNode) {
        node.setAndGetData(dataNode)
        .put(node, R151ExpectedKeysEnum.Id_PRM, Id_PRM );
    }    
    
}
