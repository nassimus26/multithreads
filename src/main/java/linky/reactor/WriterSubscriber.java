package linky.reactor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterSubscriber implements Subscriber<byte[]> {
    private OutputStream stream;
    public WriterSubscriber(OutputStream stream){
        this.stream = stream;
    }
    Subscription subscription;
    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(1);
        this.subscription = subscription;
    }
    AtomicInteger n = new AtomicInteger();
    boolean complete = false;

    public boolean isComplete() {
        return complete;
    }

    @Override
    public void onNext(byte[] bytes) {
        try {
      //      stream.write(bytes);
            subscription.request(1);
            if(n.incrementAndGet()==489755)
                synchronized (this) {
                    notify();
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
