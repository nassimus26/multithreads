package linky.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import linky.BytesXmlNode;
import linky.ProcessLinkyFile;
import linky.R151DelimiterEnum;
import linky.api.ProcessGeneric;
import linky.api.XmlNode;
import linky.api.XmlNodeHandler;
import org.scanner.FastScanner;
import org.scanner.MoveEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AkkaLinkyFile extends ProcessGeneric {

    private static final Logger logger = LoggerFactory.getLogger(ProcessLinkyFile.class);

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
    private  ActorRef chunkFinder;
    private  ActorRef processChunk;

    private  ActorRef processChunkEmitter;

    private  ActorRef writer;
    final ActorSystem parseSystem;
    final ActorSystem writeSystem;

    public AkkaLinkyFile() {
        super(initXmlNodes());
        parseSystem = ActorSystem.create("parse");

        writeSystem = ActorSystem.create("write");
    }

    public ActorRef getProcessChunkEmitter() {
        return processChunkEmitter;
    }

    @Override
    public void initTheProcess(final FastScanner scanner, final OutputStream outputStream) {
        chunkFinder = parseSystem.actorOf(Props.create(ProcessChunkActor.class, outputStream));
        processChunk = parseSystem.actorOf(Props.create(ProcessChunkActor.class, outputStream));
        processChunkEmitter = writeSystem.actorOf(Props.create(WriterActor.class, outputStream, parseSystem, writeSystem));
        writer = writeSystem.actorOf(Props.create(WriterActor.class, outputStream, parseSystem, writeSystem));

        XmlNode xmlNodeR151 = (XmlNode) getRootNode().getChildren().get(0);
        XmlNode En_Tete_Flux_Node = xmlNodeR151.findChild(R151DelimiterEnum.En_Tete_Flux);

        parseSystem.eventStream().subscribe(processChunk, BytesXmlNode.class);


        writeSystem.eventStream().subscribe(writer, BytesRes.class);

        En_Tete_Flux_Node.setNodeHandler(new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                try {
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT);
                    //fluxSink.next(new BytesXmlNode(chunk, node));
                    chunkFinder.tell(new BytesXmlNode(chunk, node), null);
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            }
        });

        XmlNode ID_RPM_Node = xmlNodeR151.findChild(R151DelimiterEnum.Id_PRM);
        ID_RPM_Node.setNodeHandler(new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                try {
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT);
                    chunkFinder.tell(new BytesXmlNode(chunk, node), null);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });

        XmlNode Donnees_Releve_Node = xmlNodeR151.findChild(R151DelimiterEnum.Donnees_Releve);
        Donnees_Releve_Node.setNodeHandler(new XmlNodeHandler(this) {
            @Override
            public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                try {
                    final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT);
                    chunkFinder.tell(new BytesXmlNode(chunk, node), null);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    public ActorSystem getParseSystem() {
        return parseSystem;
    }

    public ActorSystem getWriteSystem() {
        return writeSystem;
    }

    @Override
    public void onProcessEnd(OutputStream outputStream) {
        try{
            parseSystem.awaitTermination();
            writeSystem.awaitTermination();
            //parseSystem.shutdown();
            //writeSystem.shutdown();
        } catch (Throwable e) {
            logger.error( e.getMessage(), e );
        }
    }
    
}

