package linky.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class XmlNode<T extends IDelimiter> implements Serializable{
    private static final Logger logger = LoggerFactory.getLogger(XmlNode.class);
    private transient XmlNode<T> parent;
    private transient XmlNode<T> previous;
    private transient XmlNode<T> next;
    private T delimiter;
    private transient XmlNodeHandler nodeHandler;
    private transient List<XmlNode<T>> children;
    private transient RowsDataHandler rowsDataHandler;
    private transient XmlNodeData nodeData;
    public XmlNode(T delimiter) {
        this();
        this.delimiter = delimiter;
    }
    
    public XmlNode(XmlNode parent, T delimiter) {
        this();
        this.parent = parent;
        this.delimiter = delimiter;
    }
    

    public void setNodeHandler(XmlNodeHandler nodeHandler) {
        this.nodeHandler = nodeHandler;
    }

    public final XmlNodeHandler getNodeHandler() {
        return nodeHandler;
    }

    public XmlNode(){
    }
    private boolean used = false;
    public final XmlNode newRow() throws Exception{                
        XmlNode newXmlNode = null;        
        if (!used){            
            if ( rowsDataHandler == null )
                rowsDataHandler = new RowsDataHandler();                            
            newXmlNode = this;                
        }else {                     
            synchronized (rowsDataHandler) {
                if (rowsDataHandler.size()>0){
                    newXmlNode = rowsDataHandler.get(0);        
                    if (newXmlNode!=null && !newXmlNode.used)
                        return newXmlNode;                    
                }
            }                
            
            newXmlNode = new XmlNode(delimiter);
            newXmlNode.parent = parent;                  
            newXmlNode.rowsDataHandler = rowsDataHandler;
            newXmlNode.nodeHandler = nodeHandler;
            
            if (next!=null){
                newXmlNode.next = next.newRow();                                                      
                newXmlNode.next.previous = newXmlNode;
            }        
        
            attachChildren(this, newXmlNode);            
        
        }
        
        rowsDataHandler.add(newXmlNode);            
                  
        
        newXmlNode.used = true;
        
        return newXmlNode;
    }
    
    private final void attachChildren(XmlNode original, XmlNode node){         
        if (original.children==null)
            return;                        
        for (int i=0;i<original.children.size();i++){
            XmlNode ochild = (XmlNode) original.children.get(i);
            XmlNode child = new XmlNode(ochild.getDelimiter());
            child.nodeHandler = ochild.nodeHandler;
            node.addChild(child);
            if (ochild.children!=null)
                attachChildren(ochild, child);        
        }
    }
    
    public final RowsDataHandler getRowsDataHandler() {
        return rowsDataHandler;
    }
    
    public final XmlNode getNext() {
        return next;
    }
    
    public final <T extends XmlNodeData> T setAndGetData(T data) {
        this.nodeData = data;
        return (T) data;
    }
    
    public XmlNodeData getNodeData() {
        return nodeData;
    }
    
    public XmlNode getParent() {
        return parent;
    }
    
    public final XmlNode findParent(T delimiter) {
        XmlNode parent = this;
        XmlNode nextParent = this;
        while(nextParent!=null){
            parent = nextParent;
            if (parent.getDelimiter() == delimiter)
                break;
            nextParent = nextParent.getParent();
        }            
        return parent;
    }
    public final XmlNode getChildByDelimiterStart(byte[] delimiterStart) {
        if (children == null)
            return null;
        for (XmlNode node : children)
            if (Arrays.equals(node.getDelimiter().getTagStart(), delimiterStart))
                return node;
        return null;
    }
    public final XmlNode getChild(T delimiter) {
        if (children == null)
            return null;
        for (XmlNode node : children)
            if (node.getDelimiter() == delimiter)
                return node;
        return null;
    }
    
    public final XmlNode findChild(T delimiter) {
        if (children == null)
            return null;  
      XmlNode  child = getChild(delimiter);
        if (child!=null){
            return child;
        }    
        for (XmlNode node : children){
            child = node.findChild(delimiter);
            if (child!=null)
                return child;
        }            
        return null;
    }
    
    public final XmlNode<T> addChild(T delimiter) {
        addChild(new XmlNode<T>(delimiter));
        return this;
    }

    public final XmlNode addChild(XmlNode child) {
        if (children == null)
            children = new ArrayList<XmlNode<T>>();
        else
        if (children.size()>0){
            child.previous = children.get(children.size()-1);
            child.previous.next = child;            
        }    
        children.add(child);
        child.parent = this;
        return this;
    }
       
    public final XmlNode addChilds(T... delimiters) {
        for (T delimiter : delimiters)
            addChild(delimiter);
        return this;
    }
    
    public final XmlNode addChilds(Collection<XmlNode<T>> children) {
        for (XmlNode node : children)
            addChild(node);
        return this;
    }
    
    public final XmlNode addChilds(XmlNode<T>... children) {
        for (XmlNode node : children)
            addChild(node);
        return this;
    }

    public final List<XmlNode<T>> getChildren() {
        return children;
    }
   
    public final T getDelimiter() {
        return delimiter;
    }

    

    @Override
    public String toString() {
        return "XmlNode [delimiter=" + delimiter + ", next=" + next + ", children=" + children + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((delimiter == null) ? 0 : delimiter.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XmlNode other = (XmlNode) obj;
        if (delimiter == null) {
            if (other.delimiter != null)
                return false;
        } else if (delimiter!=other.delimiter)
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        return true;
    }
}