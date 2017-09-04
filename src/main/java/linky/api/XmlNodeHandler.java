package linky.api;

import org.scanner.FastScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public abstract class XmlNodeHandler {
    private static final Logger logger = LoggerFactory.getLogger(XmlNodeHandler.class);
    protected ProcessGeneric processGeneric;
    public ProcessGeneric getProcessGeneric() {
        return processGeneric;
    }
    public XmlNodeHandler(ProcessGeneric processGeneric){
        this.processGeneric = processGeneric;        
    }
        
    public abstract void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node);
}