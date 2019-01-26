import java.io.*;
import java.net.*;
import java.util.*;

public class PacketGenerator 
{
	private SimplePacketDriver driver;	
	private String IFile;
	public PacketGenerator() {
		this.driver = new SimplePacketDriver();
        String[] adapters=driver.getAdapterNames();
		System.out.println("Number of adapters: "+adapters.length);
        for (int i=0; i< adapters.length; i++) 
		{
			System.out.println("Device name in Java ="+adapters[i]);	
		}
		Scanner reader = new Scanner(System.in);  
		System.out.println("Enter adapter to use: ");
		int n = reader.nextInt(); 
        if (driver.openAdapter(adapters[n]))
		{	
			System.out.println("Adapter is open: "+adapters[n]+"\n");
        }
		reader.close();
	}
	
	public static void main(String[] args) 
	{
		PacketGenerator PGen = new PacketGenerator();
		if (args.length > 0)
		{
			PGen.SetFile(args[0]);
		}
		else
		{
			System.out.println("Must provide a FILENAME !!");
			System.exit(1);
		}
		PGen.init();	
	}

	
	private void SetFile(String file) 
	{
		IFile = file;
	}

	private void init() 
	{
		FileReader FR;
		BufferedReader BR;
		ByteArrayOutputStream BytesArray;
		try 
		{
			FR = new FileReader(IFile);
			BR = new BufferedReader(FR);
			BytesArray = new ByteArrayOutputStream();
			
			String InLine;
			while((InLine = BR.readLine()) != null)
			{
				String[] pair = InLine.split(" ");
				for (int i = 0; i < pair.length; i++) 
				{
					String hexPair = pair[i];
					try
					{
						BytesArray.write(hexPairToByte(hexPair));
					}
					catch (Exception e) {}
					
				}
				if(InLine.isEmpty())
				{
					byte[] packet = BytesArray.toByteArray();
					if (packet.length>14)
					{
						PacketSender handler = new PacketSender(packet);
					}
					BytesArray = new ByteArrayOutputStream();
					
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("InputFile not Found !!");
		}
	}

	private class PacketSender implements Runnable
	{	
		private Thread T;
		private byte[] packet;
		public PacketSender(byte[] packet) 
		{
			this.packet = packet;
			T = new Thread(this);
			T.start();
		}
		public void run() 
		{
			sendPacket();
		}
		private void sendPacket() 
		{
	       if(!driver.sendPacket(packet))
		   {
	    	   System.out.println("Packet cannot be sent !!");
	       } 
		}
	}
	
	public static int hexPairToByte(String hexPair)
	{		
		Integer hexPairInt = Integer.parseInt(hexPair, 16);
		byte hexPairByte = hexPairInt.byteValue();
		int hexPairByteInt = (int) hexPairByte & 0xFF;
		return hexPairByteInt;
	}
}
