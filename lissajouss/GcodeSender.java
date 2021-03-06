

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.text.DecimalFormat;

public class GcodeSender implements SerialPortEventListener {
    SerialPort serialPort;
    private static final String PORT_NAME[] = { 
            "COM3", // Windows
            "COM18", // Windows
            "/dev/ttyUSB0", // Linux
            "/dev/ttyACM0", // Raspberry Pi
            "COM10", // Windows
            "COM11", // Windows
            "/dev/tty.usbserial-A9007UX1", // Mac OS X
    };

    static String perfil = 
            "G1 X100 Y200,\n" +
            "S3 L4 F300 F300\n" +
            "S3 L5 F400 F300\n";

    

    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 115200; // 9600; //250000; // 250000;
    
    boolean fgGo = true;
    boolean fgTemDado = false;
    String  dado = "";
    static DecimalFormat d = new DecimalFormat("0.0");
 
    public void initialize(String portName)
    {
            System.out.println("Abrindo "+ portName);
            CommPortIdentifier portId = null;
            Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
            while( portEnum.hasMoreElements() ){
                portId  = portEnum.nextElement();
                if (portId.getName().equals(portName))
                    break;
            }
            if (portId == null) {
                System.out.println("Could not find COM port " + portName);
                return;
            }
            try {
                serialPort = (SerialPort) portId.open("Java Gcode Sender",TIME_OUT);
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                output = serialPort.getOutputStream();
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
                System.out.println("===================================");
                System.out.println(portId.getCurrentOwner());
                System.out.println(portId.getName());
                System.out.println(portId.getPortType());
                System.out.println("===================================");
                serialPort.setSerialPortParams(DATA_RATE,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
            }
            try{
               Thread.sleep(3000);  
            }catch(Exception e){e.printStackTrace();}
            while(fgTemDado)
                System.out.println(getDado());
            configSandDrawing();
        //configBancada();
    }

    public String[] getList()
    {
            
            CommPortIdentifier portId = null;
            int i;
            Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
            for( i = 0; portEnum.hasMoreElements();i++) {
                portId = portEnum.nextElement();
            }       
            String ports[] = new String[i];
            portEnum = CommPortIdentifier.getPortIdentifiers();
            for(i = 0; portEnum.hasMoreElements() && i < ports.length;i++) {
                portId = portEnum.nextElement();
                ports[i] = portId.getName();
            }
            return ports;
    }
    
    public void sendG1(double x, double y, double f)
    {
            String str = "G1 X"+ d.format(x).replace(",",".") + " Y" +  d.format(y).replace(",",".") + 
                        " F" +  d.format(f).replace(",",".") + "\n";  //2000
            write(str); 
    }

    public void write(String str)
    {
       try{
            output.write(str.getBytes());
        }catch(IOException err){
                err.printStackTrace();
        }
       try{
        while(!fgTemDado)
            Thread.sleep(10);
        System.out.println(str.trim()+ " " + getDado());
       }catch(Exception e){e.printStackTrace();}
}

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public String getDado()
    {
        String str = dado;
        dado = "";
        fgTemDado = false;
        return str.trim();
    }

    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine=input.readLine();
                dado = dado + "\n" + inputLine;
                fgTemDado = true;
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();

            }
        }
    }

    public void centerHoming(){
           write("$H\n");
           write("G10 P0 L20 X0 Y0 Z0\n");
           write("G1 X115 Y-130 F4000 \n");
           write("G10 P0 L20 X0 Y0 Z0\n");
           
       }
 
    public void homing(){
           write("$H\n");
           write("G10 P0 L20 X0 Y0 Z0\n");       
    }
    
    public void configSandDrawing(){
           write("$100=80\n");    //microsteps/mm
           write("$101=80\n");
           write("$102=80\n");
           write("$110=16001.000\n"); //  mm/min
           write("$111=16001.000\n");
           write("$112=16001.000\n");
           write("$120=4000.000\n");   // mm/s2??
           write("$121=4000.000\n");
           write("$122=4000.000\n");
           write("$130=220.000\n");  // mm
           write("$131=260.000\n");
           write("$132=200.000\n");     
    }


    public void configBancada(){
           write("$100=5.\n");    //microsteps/mm
           write("$101=5.\n");
           write("$102=5.\n");
           write("$110=1000.000\n"); //  mm/min
           write("$111=1000.000\n");
           write("$112=1000.000\n");
           write("$120=4000.000\n");   // mm/s2??
           write("$121=4000.000\n");
           write("$122=4000.000\n");
           write("$130=600.000\n");  // mm
           write("$131=600.000\n");
           write("$132=600.000\n");
       }
      
     public static void printList(String l[])
    {
        for(int i=0;i<l.length;i++)
            System.out.println("Serial " + i + ":" + l[i]);
    }

    public static void main(String[] args) throws Exception {
        GcodeSender xmain = new GcodeSender();
        String portas[] = xmain.getList();
        printList(portas);
        xmain.initialize(portas[0]);
        xmain.output.write("G0 X100 Y100\n".getBytes());        
        while(!xmain.fgTemDado)
                TimeUnit.SECONDS.sleep(1);
        System.out.println(xmain.dado);
        xmain.fgTemDado = false;
        xmain.close();
    }
    
}
