 package linky.reactor;

import linky.R151DelimiterEnum;
import linky.R151ExpectedKeysEnum;
import linky.api.RowsDataHandler;
import linky.api.XmlNode;
import linky.api.XmlNodeData;

import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;


 public class ReactorLinkDataCollector extends XmlNodeData<R151ExpectedKeysEnum, byte[]> {

     private boolean isConsumed = false;
     private OutputStream outputStream;
     private Map<R151ExpectedKeysEnum, byte[]> data = new EnumMap<>(R151ExpectedKeysEnum.class);

     public ReactorLinkDataCollector(OutputStream outputStream) {
         this.outputStream = outputStream;
     }
     private void writeResult(XmlNode node) {
         final byte[] Numero_Abonnement = data.get(R151ExpectedKeysEnum.Numero_Abonnement);
         final byte[] Id_PRM = data.get(R151ExpectedKeysEnum.Id_PRM);
         final byte[] Date_Releve = data.get(R151ExpectedKeysEnum.Date_Releve);
         final byte[] Releve_Value = data.get(R151ExpectedKeysEnum.Releve_Value);
         final byte[] Puissance_Maximale_Value = data.get(R151ExpectedKeysEnum.Puissance_Maximale_Value);
         final byte[] res = new byte[Numero_Abonnement.length+Id_PRM.length+Date_Releve.length+Releve_Value.length+Puissance_Maximale_Value.length+6];
         System.arraycopy(Numero_Abonnement, 0, res, 0, Numero_Abonnement.length);
         res[Numero_Abonnement.length]=';';
         System.arraycopy(Id_PRM, 0, res, Numero_Abonnement.length+1, Id_PRM.length);
         res[Numero_Abonnement.length+Id_PRM.length+1]=';';
         System.arraycopy(Date_Releve, 0, res, Numero_Abonnement.length+Id_PRM.length+2, Date_Releve.length);
         res[Numero_Abonnement.length+Id_PRM.length+Date_Releve.length+2]=';';
         System.arraycopy(Releve_Value, 0, res, Numero_Abonnement.length+Id_PRM.length+Date_Releve.length+3, Releve_Value.length);
         res[Numero_Abonnement.length+Id_PRM.length+Date_Releve.length+Releve_Value.length+3]=';';
         System.arraycopy(Puissance_Maximale_Value, 0, res, Numero_Abonnement.length+Id_PRM.length+Date_Releve.length+Releve_Value.length+4, Puissance_Maximale_Value.length);
         res[res.length-2]='\r';
         res[res.length-1]='\n';
         System.out.println(new String(res));
         ((ReactorLinkyFile)node.getNodeHandler().getProcessGeneric()).getWriteFluxSink().next(res);
     }

     @Override
     public void put(XmlNode node, R151ExpectedKeysEnum key, byte[] value) {
         if (isConsumed)
             return;
     /*    if (value==null)
             System.out.println(key+" null");
         else
            System.out.println(key+" "+new String(value));*/
         data.put(key, value);
         XmlNode R151Node;
         switch ((R151DelimiterEnum)node.getDelimiter()) {
             case En_Tete_Flux:
                 R151Node = node.findParent(R151DelimiterEnum.R151);
                 XmlNode RPM = R151Node.findChild(R151DelimiterEnum.PRM);
                 RowsDataHandler prms = RPM.getRowsDataHandler();
                 if (prms!=null) {
                     for (XmlNode rpm: prms.getRows()) {
                         XmlNode donnee = rpm.findChild(R151DelimiterEnum.Donnees_Releve);
                         RowsDataHandler nodes = donnee.getRowsDataHandler();
                         if (nodes!=null) {
                             boolean done = false;
                             for (XmlNode donneNode :  nodes.getRows()) {
                                 if (donneNode==null)
                                     continue;
                                    XmlNodeData data = donneNode.getNodeData();
                                    if (data!=null){
                                        if (data.write(donneNode, key, value))
                                          done = true;
                                    }
                                }
                             if (done){
                                 nodes.clearRows();
                             }
                         }
                         }
                     }
             break;
             case Id_PRM:
                     RowsDataHandler nodes = node.getNext().getRowsDataHandler();
                     if (nodes!=null) {
                         boolean done = false;
                         for (XmlNode donneNode : nodes.getRows()) {
                             if (donneNode==null)
                                 continue;
                             XmlNodeData data = donneNode.getNodeData();
                             if (data!=null)
                                 if (data.write(donneNode, key, value))
                                     done= true;
                         }
                         if (done){
                             nodes.clearRows();
                         }
                     }
             break;
             case Donnees_Releve:
                 boolean num_ab = data.containsKey( R151ExpectedKeysEnum.Numero_Abonnement );
                 boolean Id_PRM = data.containsKey( R151ExpectedKeysEnum.Id_PRM );
                 RowsDataHandler donnee_rows = node.getRowsDataHandler();
                 if (!num_ab|| !Id_PRM){
                     R151Node = node.findParent(R151DelimiterEnum.R151);
                     if (!num_ab){
                         XmlNodeData En_Tete_Flux_Data = R151Node.findChild( R151DelimiterEnum.En_Tete_Flux ).getNodeData();
                         if ( En_Tete_Flux_Data!=null )
                             if (write(node, R151ExpectedKeysEnum.Numero_Abonnement, (byte[]) En_Tete_Flux_Data.get(R151ExpectedKeysEnum.Numero_Abonnement))){
                                 donnee_rows.removeRow(node);
                                 return;
                             }
                     }
                     if (!Id_PRM){
                         XmlNodeData ID_PRM_Data = R151Node.findChild( R151DelimiterEnum.Id_PRM ).getNodeData();
                         if ( ID_PRM_Data!=null )
                             if (write(node, R151ExpectedKeysEnum.Id_PRM, (byte[]) ID_PRM_Data.get(R151ExpectedKeysEnum.Id_PRM))){
                                 donnee_rows.removeRow(node);
                                 return;
                             }
                         }
                     }
                 if ( !isConsumed )
                     checkState(node);
                 if (isConsumed)
                     donnee_rows.removeRow(node);
         }
     }

     @Override
     public boolean isConsumed() {
         return isConsumed;
     }

     public boolean isReady() {
         return data.keySet().size() == R151ExpectedKeysEnum.values().length;
     }

     @Override
     public String toString() {
         return "LinkDataCollector [data=" + data + ", terminated=" + isConsumed + "]";
     }

     @Override
     public boolean write(XmlNode node, R151ExpectedKeysEnum key, byte[] value) {
         if (value==null)
             return isConsumed;
         data.put(key, value);
         checkState(node);
         return isConsumed;
     }

     private void checkState(XmlNode node) {
         if (!isConsumed)
             if (isReady())
             synchronized (this) {
                 if (!isConsumed){
                     isConsumed = true;
                     writeResult(node);
                 }
             }
     }
     @Override
     public byte[] get(R151ExpectedKeysEnum key) {
         return data.get(key);
     }
 }