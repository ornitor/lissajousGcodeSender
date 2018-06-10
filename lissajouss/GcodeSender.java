

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


public class GcodeSender implements SerialPortEventListener {
    SerialPort serialPort;
    private static final String PORT_NAME[] = { 
            "/dev/ttyUSB0", // Linux
            "/dev/ttyACM0", // Raspberry Pi
            "COM3", // Windows
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

    public void initialize(){
       System.setProperty("gnu.io.rxtx.SerialPorts", PORT_NAME[0]);
        CommPortIdentifier portId = null;
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            portId = currPortId;
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }
        try {
            serialPort = (SerialPort) portId.open("Gcode Sender",TIME_OUT);
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
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();
            
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();

        }
    }


    public void write(String str)
    {
        try{
            output.write(str.getBytes());
        }catch(IOException err){
                err.printStackTrace();
        }
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

    public void configSandDrawing(){
           String str;
           str = "$100=80\n";
           write(str);
           write("$101=80\n");
           write("$102=80\n");
           write("$110=2000.000\n");
           write("$111=2000.000\n");
           write("$112=2000.000\n");
           write("$120=50.000\n");
           write("$121=50.000\n");
           write("$122=50.000\n");

        
       }
       
       
    public static void main(String[] args) throws Exception {
        GcodeSender xmain = new GcodeSender();
        xmain.initialize();
        xmain.output.write("G0 X100 Y200\n".getBytes());        
        while(!xmain.fgTemDado)
                TimeUnit.SECONDS.sleep(1);
        System.out.println(xmain.dado);
        xmain.fgTemDado = false;
        xmain.close();
    }
    
}
