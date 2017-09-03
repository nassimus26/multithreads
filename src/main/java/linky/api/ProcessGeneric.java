 package linky.api;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.scanner.FastByteArrayOutputStream;
import org.scanner.FastScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessGeneric {    
    private static final Logger logger = LoggerFactory.getLogger(ProcessGeneric.class);
    
    private XmlNode rootNode = new XmlNode( RootDelimiterEnum.Root );
    public XmlNode getRootNode() {
        return rootNode;
    }
    protected ProcessGeneric(List<XmlNode> xmlNodes) {        
        rootNode.addChilds(xmlNodes);          
    }
    public abstract void initTheProcess(final FastScanner scanner, final OutputStream outputStream);

    public abstract void onProcessEnd(final OutputStream outputStream);
    public final synchronized void execute(InputStream inputStream, OutputStream outputStream) throws Exception {
        FastScanner scanner = new FastScanner(inputStream);
        
        initTheProcess(scanner, outputStream);
        
        visitXmlNode(scanner, outputStream, rootNode);
    
        onProcessEnd( outputStream );
    }
    
    private enum TagType { isEndTag, isEquals, isNotEquals }
    
    private final TagType isEndTagAndTagsAreEquals(byte[] tag1, byte[] tag2) {        
        if (tag1.length>2 && tag1[1]=='/'){
            if (tag1.length!=tag2.length)
                return TagType.isNotEquals;
            for (int i=2;i<tag1.length-1; i++)
                if (tag2[i]!=tag1[i])
                    return TagType.isEndTag;                
            return TagType.isEquals;
        }     
        return TagType.isNotEquals;
    }
    
    private FastByteArrayOutputStream tagReader = new FastByteArrayOutputStream(512);
    final void visitXmlNode(final FastScanner scanner, final OutputStream outputStream, XmlNode xmlNode) throws Exception {
        byte[] nextTag;
        byte[] lastTag = null;
        XmlNode tagNode = null;
        XmlNodeHandler nodeHandler;
        byte[] endParent = xmlNode.getDelimiter().getTagEnd();
         
        while (( nextTag = scanner.retrieveNextRightTokenBytes('<', '>', tagReader))!=null) {   
            //System.out.println(new String(nextTag));
            if (lastTag!=null && !Arrays.equals( nextTag, lastTag )){                
                TagType tagType = isEndTagAndTagsAreEquals(nextTag, endParent);
                if (tagType == TagType.isEquals)
                    break;
                else
                    if (tagType == TagType.isEndTag)
                        continue;
                tagNode = xmlNode.getChildByDelimiterStart(nextTag);
            }else
                tagNode = xmlNode.getChildByDelimiterStart(nextTag);
            if (tagNode!=null){                    
                tagNode = tagNode.newRow();
                nodeHandler = tagNode.getNodeHandler();
                if (nodeHandler!=null){
                    nodeHandler.onNodeVisit(scanner, outputStream, tagNode);                    
                    byte[] endTag = scanner.retrieveNextRightTokenBytes('<', '>', tagReader);
                    if (isEndTagAndTagsAreEquals(endTag, endParent)==TagType.isEquals)
                        break;                   
                }else{
                    visitXmlNode(scanner, outputStream, tagNode);
                }
                lastTag = nextTag;                                                    
            }                                
       }   
    }
    enum RootDelimiterEnum implements IDelimiter{
        Root;
        private byte[] tag;
        private byte[] tagStart;
        private byte[] tagEnd;        
        private RootDelimiterEnum() {
            tag = name().getBytes();
            tagStart = ("<"+name()+">").getBytes();
            tagEnd = ("</"+name()+">").getBytes();
        }
        @Override
        public byte[] getTag() {
            return tag;
        }
        @Override
        public byte[] getTagStart() {
            return tagStart;
        }
        @Override
        public byte[] getTagEnd() {
            return tagEnd;
        }        
    }
}