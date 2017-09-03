package linky;

import linky.api.XmlNode;

public class BytesXmlNode {
    byte[] chunk;
    XmlNode xmlNode;

    public BytesXmlNode(byte[] chunk, XmlNode xmlNode) {
        this.chunk = chunk;
        this.xmlNode = xmlNode;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public XmlNode getXmlNode() {
        return xmlNode;
    }
}