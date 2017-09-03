package linky.akka;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterActor extends UntypedActor {
    private final OutputStream outputStream;
    private final ActorSystem parseSystem;
    private final ActorSystem writeSystem;

    public WriterActor(OutputStream outputStream, ActorSystem parseSystem, ActorSystem writeSystem) {
        this.outputStream = outputStream;
        this.writeSystem = writeSystem;
        this.parseSystem = parseSystem;
    }
    static AtomicInteger n = new AtomicInteger();

    @Override
    public void onReceive(Object o) throws Exception {
        outputStream.write(((BytesRes) o).res);
        if (n.incrementAndGet()==489755){
            parseSystem.shutdown();
            writeSystem.shutdown();
        }

    }
}
