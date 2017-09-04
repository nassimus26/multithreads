package linky;

import linky.api.ProcessGeneric;
import linky.api.XmlNode;
import linky.api.XmlNodeHandler;
import org.nassimus.thread.BufferedBatchCallable;
import org.nassimus.thread.BufferedBatchFlowControlExecutor;
import org.nassimus.thread.FlowControlExecutor;
import org.scanner.FastScanner;
import org.scanner.MoveEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessLinkyFile extends ProcessGeneric {

    private static final Logger logger = LoggerFactory.getLogger(ProcessLinkyFile.class);
    private BufferedBatchFlowControlExecutor<Object> chunkProcess;
    private BufferedBatchFlowControlExecutor<byte[]> writerProcess;
    
    public final BufferedBatchFlowControlExecutor<byte[]> getWriterProcess() {
        return writerProcess;
    }

    private static List<XmlNode> initXmlNodes() {
        
        List<XmlNode> xmlNodes = new ArrayList<XmlNode>();
        xmlNodes.add(new XmlNode(R151DelimiterEnum.R151)
                .addChilds(new XmlNode(R151DelimiterEnum.En_Tete_Flux)
                        .addChild(R151DelimiterEnum.Numero_Abonnement), 
                        new XmlNode(R151DelimiterEnum.PRM).addChild(R151DelimiterEnum.Id_PRM)
                .addChilds( new XmlNode(R151DelimiterEnum.Donnees_Releve ).addChilds(new XmlNode(R151DelimiterEnum.Date_Releve),                        
                        new XmlNode(R151DelimiterEnum.Classe_Temporelle_Distributeur).addChild(R151DelimiterEnum.Valeur),
                        new XmlNode(R151DelimiterEnum.Puissance_Maximale).addChild(R151DelimiterEnum.Valeur)                       
                 ))));
        return xmlNodes;
    }

    public ProcessLinkyFile() {        
        super(initXmlNodes());
    }
    @Override
    public void initTheProcess(final FastScanner scanner, final OutputStream outputStream) {
        BufferedBatchCallable<byte[]> writerProcessTask = new BufferedBatchCallable<byte[]>(){
            @Override
            public void call(Object[] cols) throws Exception {
                int size = 0;
                for (int i=0;i<cols.length;i++)
                    size += ((byte[])cols[i]).length;
                byte[] res = new byte[size];
                int pos = 0;
                for (int i=0;i<cols.length;i++){
                    int length = ((byte[])cols[i]).length;
                    System.arraycopy(cols[i], 0, res, pos, length);
                    pos+=length;
                }
                outputStream.write( res );
            }
        };
        writerProcess = new BufferedBatchFlowControlExecutor<byte[]>(writerProcessTask, 500, 1, 100, "WriterProcess"){
            @Override
            public void handleException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public boolean isSubmitsEnds() {
                return chunkProcess.isShutdown();
            }
        };
        BufferedBatchCallable<Object> chunkProcessTask = new BufferedBatchCallable<Object>(){
            @Override
            public void call(Object[] cols) throws Exception {
                for (int i=0;i<cols.length;i++){
                    XmlNode node = (XmlNode) ((Object[])cols[i])[1];
                    if (node.getDelimiter()==R151DelimiterEnum.Donnees_Releve)
                        ProcessLinkyFileChunk.processChunk_Donnees_Releves((byte[]) ((Object[])cols[i])[0], outputStream, node, new LinkDataCollector(outputStream));
                    else if (node.getDelimiter()==R151DelimiterEnum.En_Tete_Flux)
                        ProcessLinkyFileChunk.processChunk_En_Tete_Flux((byte[]) ((Object[])cols[i])[0], outputStream, node, new LinkDataCollector(outputStream));
                    else
                        ProcessLinkyFileChunk.processChunk_Id_RPM((byte[]) ((Object[])cols[i])[0], outputStream, node, new LinkDataCollector(outputStream));
                }
            }
        };
        chunkProcess = new BufferedBatchFlowControlExecutor<Object>(chunkProcessTask,
                500, FlowControlExecutor.getNbCores()-1, 100, "ChunkProcess"){
            @Override
            public void handleException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public boolean isSubmitsEnds() {
                return scanner.isStreamTerminated();
            }
        };
        XmlNode xmlNodeR151 = (XmlNode) getRootNode().getChildren().get(0);        
        XmlNode En_Tete_Flux_Node = xmlNodeR151.findChild(R151DelimiterEnum.En_Tete_Flux);
        En_Tete_Flux_Node.setNodeHandler(new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                try {                                       
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT );
                    chunkProcess.submit(new Object[]{chunk, node});
                } catch (Throwable e) {
                    logger.error( e.getMessage(), e );
                }
            }
        });
        
        XmlNode ID_RPM_Node = xmlNodeR151.findChild(R151DelimiterEnum.Id_PRM);
        ID_RPM_Node.setNodeHandler( new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream,final XmlNode node) {
                try {                        
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT );

                    chunkProcess.submit(new Object[]{chunk, node});
                } catch (Throwable e) {
                    logger.error( e.getMessage(), e );
                }
            }
        });
                
        XmlNode Donnees_Releve_Node = xmlNodeR151.findChild(R151DelimiterEnum.Donnees_Releve);
        Donnees_Releve_Node.setNodeHandler( new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                try {
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT );                     
                    chunkProcess.submit(new Object[]{chunk, node});
                } catch (Throwable e) {
                    logger.error( e.getMessage(), e );
                }         
            }            
        });        
    }

    @Override
    public void onProcessEnd(OutputStream outputStream) {
        try {            
            chunkProcess.waitAndFlushAndShutDownWithException();
            writerProcess.waitAndFlushAndShutDownWithException();
        } catch (Throwable e) {
            logger.error( e.getMessage(), e );
        }
    }
    
}

