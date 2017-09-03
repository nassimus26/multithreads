import linky.reactor.ReactorLinkyFile;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.nassimus.date.UtilsChrono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ReactorLinkyFileTest {
   /* @Test
    public void processSimpleLinkyTest() throws Exception {
        processLinkyTest("ERDF_R151_17X1111111111111_GRD-F666_1234567890_00001_M_00001_00001_20151002135511.xml", false);
    }
    
    @Test
    public void processMuliplePRMTest() throws Exception {
        processLinkyTest("LINKY_MULTIPLE_RPM.xml", false);
    }

    @Test
    public void processMulipleR151Test() throws Exception {
        processLinkyTest("LINKY_MULTIPLE_R151.xml", false);
    }
*/ 
    /*@Test
    public void processMuliplePRMUnOrderedTest() throws Exception {
        processLinkyTest("LINKY_MULTIPLE_RPM_UnOrdered.xml", true);
    }*/
   
    @Test
    public void processMuliplePRMUnOrderedTest() throws Exception {
        processLinkyTest("LINKY_GEN.xml", true);
    }
    private void processLinkyTest(String fileName, boolean unOrdered) throws Exception {
        UtilsChrono utilsChrono = UtilsChrono.getInstance();
        FileInputStream inputStream = new FileInputStream(new File("src/main/resources/" + fileName));
        FileOutputStream outputStream = new FileOutputStream( new File("src/main/resources/output.csv" ) );
        new ReactorLinkyFile().execute(inputStream, outputStream);
       //new ProcessLinkyFile(unOrdered).execute(inputStream, System.out);
        outputStream.flush();
        IOUtils.closeQuietly(inputStream);

        System.out.println(utilsChrono.getFormattedSS_MSFull());
        System.out.flush();
    }

}
