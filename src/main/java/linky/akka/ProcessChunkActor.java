package linky.akka;

import akka.actor.UntypedActor;
import linky.BytesXmlNode;
import linky.ProcessLinkyFileChunk;
import linky.R151DelimiterEnum;
import linky.api.XmlNode;

import java.io.OutputStream;

public class ProcessChunkActor extends UntypedActor {
    private final OutputStream outputStream;

    public ProcessChunkActor(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void onReceive(Object o) throws Exception {
        BytesXmlNode bytesXmlNode = (BytesXmlNode) o;
        XmlNode node = bytesXmlNode.getXmlNode();
        byte[] chunk = bytesXmlNode.getChunk();
        if (node.getDelimiter()== R151DelimiterEnum.Donnees_Releve)
            ProcessLinkyFileChunk.processChunk_Donnees_Releves((byte[]) chunk, outputStream, node, new AkkaLinkDataCollector(outputStream));
        else if (node.getDelimiter()==R151DelimiterEnum.En_Tete_Flux)
            ProcessLinkyFileChunk.processChunk_En_Tete_Flux((byte[]) chunk, outputStream, node, new AkkaLinkDataCollector(outputStream));
        else
            ProcessLinkyFileChunk.processChunk_Id_RPM((byte[]) chunk, outputStream, node, new AkkaLinkDataCollector(outputStream));

    }
}
