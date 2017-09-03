package linky.api;

import java.io.Serializable;


public abstract class XmlNodeData<T, V> implements Serializable  {
    
    public abstract void put(XmlNode node, T key, V value);    
    public abstract boolean write(XmlNode node, T key, V value);    
    public abstract V get(T key);
    public abstract boolean isConsumed();  
}
