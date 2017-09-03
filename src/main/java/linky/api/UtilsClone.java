package linky.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UtilsClone {
    public static <T> T cloneObject(T obj) throws Exception {
        //return (T) SerializationUtils.clone((Serializable) obj);
        Object newObj;
        ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
        newObj = new ObjectInputStream(bais).readObject();
        return (T) newObj;
        
    }

}
