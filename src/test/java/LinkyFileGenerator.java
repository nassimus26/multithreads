import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.scanner.FastScanner;
import org.scanner.MoveEnum;

public class LinkyFileGenerator {

    public static void main(String args[]) throws Exception {

        File linkyFile = new File( "src/main/resources/LINKY_ITEM.xml" );
        String R151 = FileUtils.readFileToString( linkyFile );
        
        FastScanner scanner = new FastScanner( new FileInputStream(linkyFile) );
        scanner.moveToNextElement("<PRM>", MoveEnum.LEFT_FROM_ELEMENT);
        String PRM = new String (scanner.readToElement("</PRM>", MoveEnum.RIGHT_FROM_ELEMENT));
        
        scanner = new FastScanner( new FileInputStream(linkyFile) );
        scanner.moveToNextElement("<Donnees_Releve>", MoveEnum.LEFT_FROM_ELEMENT);
        String Donnees_Releve = new String (scanner.readToElement("</Donnees_Releve>", MoveEnum.RIGHT_FROM_ELEMENT));
        
        String Entete = "\n"+R151.replace(PRM, "").replace("<R151>", "").replace("</R151>", "");
        PRM = "\n"+PRM.replace(Donnees_Releve, "").replace("</PRM>", "");
        Donnees_Releve = "\n"+Donnees_Releve.replace("</Donnees_Releve>", "");
        
        OutputStream outputStream = new FileOutputStream( new File( "src/main/resources/LINKY_GEN.xml" ) );
        outputStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n".getBytes());
        String ID_PRM = "<Id_PRM>#{Id_PRM}</Id_PRM>\r\n";
        for (long i=1;i<=10;i++){
            outputStream.write("<R151>\r\n".getBytes());
            if (i%2==0)
                outputStream.write( 
                    Entete.
                        replace("#{Numero_Abonnement}", "Numero_Abonnement_"+i).getBytes() );
            for (long j=1;j<=20;j++){
                outputStream.write("<PRM>\r\n".getBytes());
                if (i%2==0)
                    outputStream.write(  
                        ID_PRM.
                            replace("#{Id_PRM}", "Id_PRM_R151-"+i+"_"+j).getBytes() );
                for (long k=1;k<=1400;k++){
                    outputStream.write((
                        Donnees_Releve. 
                            replace("#{Date_Releve}", "Date_Releve_R151-"+i+"_PRM-"+j+"_"+k).
                            replace("#{Classe_Temporelle_Distributeur_Value}", "Classe_Temporelle_Distributeur_Value_R151-"+i+"_PRM-"+j+"_"+k).
                            replace("#{Puissance_Maximale_Value}", "Puissance_Maximale_Value_R151-"+i+"_PRM-"+j+"_"+k)+"</Donnees_Releve>\r\n").getBytes());
                }
                if (i%2==1)
                    outputStream.write(  
                        ID_PRM.
                            replace("#{Id_PRM}", "Id_PRM_R151-"+i+"_"+j).getBytes() );
                outputStream.write("</PRM>\r\n".getBytes() );
            }                
            if (i%2==1)
                outputStream.write( 
                    Entete.
                        replace("#{Numero_Abonnement}", "Numero_Abonnement_"+i).getBytes() );
            outputStream.write("</R151>\r\n".getBytes() );
        }
            
        IOUtils.closeQuietly(outputStream);
        System.out.flush();
    }
}
