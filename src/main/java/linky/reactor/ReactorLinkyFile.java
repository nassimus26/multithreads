package linky.reactor;

import linky.BytesXmlNode;
import linky.ProcessLinkyFile;
import linky.ProcessLinkyFileChunk;
import linky.R151DelimiterEnum;
import linky.api.*;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.scanner.FastScanner;
import org.scanner.MoveEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
* @Author : Moualek Nassim cd_boite@yahoo.fr
*
* */
public class ReactorLinkyFile extends ProcessGeneric {

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
    FluxSink<byte[]> writeFluxSink;

    public void setWriteFluxSink(FluxSink<byte[]> writeFluxSink) {
        this.writeFluxSink = writeFluxSink;
    }

    public FluxSink<byte[]> getWriteFluxSink() {
        return writeFluxSink;
    }

    public ReactorLinkyFile() {
        super(initXmlNodes());
    }
    private WriterSubscriber writerSubscriber;
    @Override
    public void initTheProcess(final FastScanner scanner, final OutputStream outputStream) {

        XmlNode xmlNodeR151 = (XmlNode) getRootNode().getChildren().get(0);        
        XmlNode En_Tete_Flux_Node = xmlNodeR151.findChild(R151DelimiterEnum.En_Tete_Flux);

        Processor<BytesXmlNode, byte[]> processChunk = new Processor<BytesXmlNode, byte[]>() {
            private Subscription subscription;
            private int batchSize = 50;
            private int seen;
            private Flux<byte[]> writer;

            @Override
            public void subscribe(Subscriber<? super byte[]> subscriber) {

            }

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(batchSize);
                writer = Flux.empty();
            }

            @Override
            public void onNext(BytesXmlNode bytesXmlNode) {
                XmlNode node = bytesXmlNode.getXmlNode();
                byte[] chunk = bytesXmlNode.getChunk();
                if (node.getDelimiter()==R151DelimiterEnum.Donnees_Releve)
                    ProcessLinkyFileChunk.processChunk_Donnees_Releves((byte[]) chunk, outputStream, node, new ReactorLinkDataCollector(outputStream));
                else if (node.getDelimiter()==R151DelimiterEnum.En_Tete_Flux)
                    ProcessLinkyFileChunk.processChunk_En_Tete_Flux((byte[]) chunk, outputStream, node, new ReactorLinkDataCollector(outputStream));
                else
                    ProcessLinkyFileChunk.processChunk_Id_RPM((byte[]) chunk, outputStream, node, new ReactorLinkDataCollector(outputStream));
               //subscription.request(1);

                if (++seen > batchSize-1/2) {
                    seen = 0;
                    subscription.cancel();
                    subscription.request(batchSize/2);
                }else
                    subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        Flux<byte[]> writeFlux = Flux.<byte[]>create(fluxSink -> {
            setWriteFluxSink(fluxSink);
        });
        writerSubscriber = new WriterSubscriber(outputStream);
        writeFlux.subscribe(writerSubscriber);
        writeFlux.publish();
        Flux<BytesXmlNode> xmlChunkParser = Flux.create(fluxSink -> {
            En_Tete_Flux_Node.setNodeHandler(new XmlNodeHandler(this) {
                @Override
                public void onNodeVisit(final FastScanner scanner, final OutputStream outputStream, final XmlNode node) {
                    try {
                        final byte[] chunk = scanner.readToElement(node.getDelimiter().getTagEnd(), MoveEnum.LEFT_FROM_ELEMENT );
                        fluxSink.next(new BytesXmlNode(chunk, node));
                    } catch (Throwable e) {
                        e.printStackTrace();
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
                        fluxSink.next(new BytesXmlNode(chunk, node));
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
                        fluxSink.next(new BytesXmlNode(chunk, node));
                    } catch (Throwable e) {
                        logger.error( e.getMessage(), e );
                    }
                }
            });
        });

        //xmlChunkParser.buffer(150);
        xmlChunkParser.parallel(15).runOn(Schedulers.parallel()).subscribe(processChunk);
        xmlChunkParser.publish();
    }

    @Override
    public void onProcessEnd(OutputStream outputStream) {
        try {
            System.out.println("waiting");
            synchronized (writerSubscriber){
                writerSubscriber.wait();
            }
            System.out.println("waking");
        } catch (Throwable e) {
            logger.error( e.getMessage(), e );
        }
    }
    
}

