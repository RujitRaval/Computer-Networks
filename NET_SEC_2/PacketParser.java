import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class PacketParser 
{
	private SimplePacketDriver driver;	
	private int Count = -1;
	private String IFile, OFile;
	private BufferedWriter BW;
	private String PType = "All";
	private boolean HeaderOnly = false;
	private final ExecutorService packetPool; 
	private InetAddress SrcAddress, DestAddress, SrcAddressOR, DestAddressOR, SrcAddressAND, DestAddressAND;
	private Integer SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd;
	Vector<DatagramBuffer> buffers = new Vector<DatagramBuffer>();
	DatagramBufferWatcher buffersWatcher;
	
	public PacketParser() 
	{
		packetPool = Executors.newFixedThreadPool(30);
		this.driver = new SimplePacketDriver();
        String[] adapters=driver.getAdapterNames();
        if (driver.openAdapter(adapters[0])) System.out.println("Adapter is open: "+adapters[0]+"\n"); 
	}

	public static void main(String[] args) 
	{
		PacketParser Par = new PacketParser();
		if (args.length > 0)
		{
			int Arg = 0;
			while(Arg < args.length)
			{
				String arg = args[Arg];
				switch(arg.charAt(1))
				{
					case 'c':
						Par.SetPacketCount(args[++Arg]);
						break;
					case 'r':
						Par.SetInputFile(args[++Arg]);
						break;
					case 'o':
						Par.SetOutputFile(args[++Arg]);
						break;
					case 't':
						Par.SetPacketType(args[++Arg]);
						break;
					case 'h':
						Par.SetHeaderOnly();
						break;
					case 's':
						if(arg.equals("-src"))
						{
							Par.SetSrcAddress(args[++Arg]);
						}
						else if(arg.equals("-sord"))
						{
							Par.SetORAddress(args[++Arg], args[++Arg]);
						}
						else if(arg.equals("-sandd"))
						{
							Par.SetANDAddress(args[++Arg], args[++Arg]);
						}
						else if(arg.equals("-sport"))
						{
							Par.SetSrcPort(args[++Arg], args[++Arg]);
						}
						break;
					case 'd':
						if(arg.equals("-dst"))
						{
							Par.SetDestAddress(args[++Arg]);
						}
						else if(arg.equals("-dport"))
						{
							Par.SetDestPort(args[++Arg], args[++Arg]);
						}
						break;
					default: 
						System.out.println("INVALID ARGUMENT !!");
						System.exit(1);
						break;		
				}
				Arg++; 
			}
			//Par.init();
		}
		Par.init();
	}

	

	private boolean HasAddressArgument() 
	{		
		return ( (SrcAddress!=null)||(DestAddress!=null)||(SrcAddressOR!=null)||(DestAddressOR!=null)||(SrcAddressAND!=null)||(DestAddressAND!=null) );
	}
	
	private boolean HasPortArgument() 
	{		
		return ( (SrcPortStart!=null)||(SrcPortEnd!=null)||(DestPortStart!=null)||(DestPortEnd!=null) );
	}
	
	private void SetSrcPort(String portStart, String portEnd) 
	{
		try
		{
			SrcPortStart = Integer.parseInt(portStart);
			SrcPortEnd = Integer.parseInt(portEnd);
			if( (SrcPortStart > 65535 || SrcPortStart < 0) || (SrcPortEnd > 65535 || SrcPortEnd < 0) )
			{
				System.out.println("Please provide proper value for the PORT !!");
				System.exit(1);
			}
			else if(SrcPortStart > SrcPortEnd)
			{
				System.out.println("Start port must be smaller than end port !!");
				System.exit(1);
			}
		}
		catch(Exception e)
		{
			System.out.println("Invalid PORT !!");
			System.exit(1);
		}
	}
	
	private void SetDestPort(String portStart, String portEnd) 
	{
		try
		{
			DestPortStart = Integer.parseInt(portStart);
			DestPortEnd = Integer.parseInt(portEnd);
			if( (DestPortStart > 65535 || DestPortStart < 0) || (DestPortEnd > 65535 || DestPortEnd < 0) )
			{
				System.out.println("Please provide proper value for the PORT !!");
				System.exit(1);
			}
			else if(DestPortStart > DestPortEnd)
			{
				System.out.println("Start port must be smaller than end port !!");
				System.exit(1);
			}
		}catch(Exception e){
			System.out.println("Invalid PORT !!");
			System.exit(1);
		}
	}

	private void SetORAddress(String srcAdd, String dstAdd) 
	{
		try 
		{
			DestAddressOR = InetAddress.getByName(dstAdd);
			SrcAddressOR = InetAddress.getByName(srcAdd);
		} 
		catch (Exception e) 
		{
			System.out.println("Invalid values given for OR !!");
			System.exit(1);
		}
	}
	
	private void SetANDAddress(String srcAdd, String dstAdd) 
	{
		try 
		{
			DestAddressAND = InetAddress.getByName(dstAdd);
			SrcAddressAND = InetAddress.getByName(srcAdd);
		} 
		catch (Exception e) 
		{
			System.out.println("Invalid values given for AND !!");
			System.exit(1);
		}
	}

	private void SetSrcAddress(String srcAdd) 
	{
		try 
		{
			SrcAddress = InetAddress.getByName(srcAdd);
		} 
		catch (Exception e) 
		{
			System.out.println("Invalid value given for Source Address !!");
			System.exit(1);
		}
	}
	
	private void SetDestAddress(String dstAdd) 
	{
		try 
		{
			DestAddress = InetAddress.getByName(dstAdd);
		} 
		catch (Exception e) 
		{
			System.out.println("Invalid value given for Destination Address !!");
			System.exit(1);
		}
	}

	private void SetPacketType(String Type) 
	{
		PType = Type;
	}
	
	private void SetHeaderOnly()
	{
		HeaderOnly = true;
	}
	private void SetPacketCount(String count)
	{
		try
		{
			Count = Integer.parseInt(count);
		}
		catch(Exception e)
		{
			System.out.println("Invalid value given for Count !!");
			System.exit(1);
		}
	}
	
	private void SetInputFile(String in) 
	{
		IFile = in;
	}
	
	private void SetOutputFile(String out) 
	{
		OFile = out;
		try 
		{
			BW = new BufferedWriter(new FileWriter(OFile));
		} 
		catch (Exception e) 
		{
			System.out.println("Invalid output File !!");
			System.exit(1);
		}
	}
	
	private void output(SIDHandler tuple, Object obj) 
	{
		if(OFile==null)
		{
			if(!HeaderOnly)
			{	
				System.out.println(tuple.toString()+"\n"+obj.toString());	
			}
			else
			{
				System.out.println(tuple.toString()+"\n"+obj.toString());
			}
		}
		else
		{	
				String output = ((EthernetPacket) obj).ToHex();
				try 
				{
					BW = new BufferedWriter(new FileWriter(OFile, true));
					BW.write(output);
				    BW.close();
				}
				catch (Exception e) 
				{
					System.out.println("Invalid output File !!");
					System.exit(1);
				}
		}
	}

	
	private void init() 
	{
		if(IFile==null)
		{
			int num_packets = 0;
			while(num_packets != Count)
			{
				byte [] packet=driver.readPacket();
				//PacketHandler handler = new PacketHandler(packet);
				packetPool.submit( new PacketHandler(packet) );
				num_packets++;
			}
		}
		else
		{

			FileReader fr;
			BufferedReader br;
			ByteArrayOutputStream bytesOut;
			int num_packets = 0;
			try 
			{
				fr = new FileReader(IFile);
				br = new BufferedReader(fr);
				bytesOut = new ByteArrayOutputStream();
				
				String line;
				while((line = br.readLine()) != null) 
				{
					String[] theline = line.split(" ");
					for (int i = 0; i < theline.length; i++) 
					{
						String Hex = theline[i];
						try
						{
							bytesOut.write(HextoBytes(Hex));
						}
						catch (Exception e) {}
					}
					
					if(line.isEmpty())
					{
						byte[] packet = bytesOut.toByteArray();
						if (packet.length>14 && num_packets != Count)
						{
							packetPool.submit( new PacketHandler(packet) );
							num_packets++;
						}
						bytesOut = new ByteArrayOutputStream();
					}
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				System.out.println("Invalid input File !!");
			}
		}
	}


	private class PacketHandler implements Runnable
	{	
		
		private byte[] packet;

		public PacketHandler(byte[] packet) 
		{
			this.packet = packet;	
		}

		public void run() 
		{
			handlePacket();
		}

		private void handlePacket() 
		{   
			EthernetPacket ethPack = new EthernetPacket(packet);
			
			if (ethPack.EtherType.equals("IPv4")) 
			{	
				IPPacket ipPack = new IPPacket(packet);
				if (HasAddressArgument()) 
				{
					ipPack = ipPack.AddressFilter(SrcAddress,DestAddress,SrcAddressOR,DestAddressOR,SrcAddressAND,DestAddressAND);
				}
				
				if(ipPack != null)
				{
					if (ipPack.Protocol.equals("TCP")) 
					{
						TCPPacket tcpPack = new TCPPacket(packet);
						if (HasPortArgument()) 
						{
							tcpPack = tcpPack.PortFilter(SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd);
						}
						if(tcpPack!=null && tcpPack.PacketType(PType))
						{
							SIDHandler tuple = AssemblePacket(tcpPack);
							if(tuple!=null){
								byte[] assembled = tuple.Packet.packet;
								tcpPack = new TCPPacket(assembled);
								output(tuple, tcpPack);
							}
						}
					} 
					else if (ipPack.Protocol.equals("UDP")) 
					{
						UDPPacket udpPack = new UDPPacket(packet);
						if (HasPortArgument()) 
						{
							udpPack = udpPack.PortFilter(SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd);
						}
						if(udpPack!=null && udpPack.PacketType(PType))
						{
							SIDHandler tuple = AssemblePacket(udpPack);
							if(tuple!=null){
								byte[] assembled = tuple.Packet.packet;
								udpPack = new UDPPacket(assembled);
								output(tuple, udpPack);
							}
						}
						
					} 
					else if (ipPack.Protocol.equals("ICMP")) 
					{
						ICMPPacket icmpPack = new ICMPPacket(packet);
						if (!HasPortArgument()) 
						{
							if(icmpPack.PacketType(PType))
							{
								SIDHandler tuple = AssemblePacket(icmpPack);
								if(tuple!=null){
									byte[] assembled = tuple.Packet.packet;
									icmpPack = new ICMPPacket(assembled);
									output(tuple, icmpPack);
								}
							}
						}
					} else {}
				}
				
			} 
			else if (ethPack.EtherType.equals("ARP")) 
			{	
				ARPPacket arpPack = new ARPPacket(packet);
				
				if (HasAddressArgument()) 
				{
					arpPack = arpPack.AddressFilter(SrcAddress,DestAddress,SrcAddressOR,DestAddressOR,SrcAddressAND,DestAddressAND);
				}
				if(arpPack!=null)
				{
					if (!HasPortArgument()) 
					{
						if(arpPack.PacketType(PType))
						{
							
							SIDHandler tuple = new SIDHandler(SIDHandler.ArpSID, arpPack);
							output(tuple, arpPack);
							//output(arpPack);
						}
					}	
				}
			} else {}
		}
	}
	
	synchronized private DatagramBuffer NextBuffer(IPPacket fragment)
	{
		Iterator<DatagramBuffer> Iter = buffers.iterator();
		DatagramBuffer bufferMatch = null;
		DatagramBuffer buffer;
		while(Iter.hasNext())
		{
			buffer = Iter.next();
			if(buffer.Match(fragment))
			{
				bufferMatch = buffer;
			}
		}
		
		if (bufferMatch==null)
		{
			DatagramBuffer newBuffer = new DatagramBuffer(fragment);
			buffers.add(newBuffer);
			bufferMatch = newBuffer;
		}
		return bufferMatch;
	}
	
	synchronized private SIDHandler AssemblePacket(IPPacket fragment)
	{
		DatagramBuffer buffer = NextBuffer(fragment);
		SIDHandler tuple = buffer.AddFragment(fragment);
		if(tuple!=null)
		{
			buffers.remove(buffer);
		}
		return tuple;
	}
	
	private void startWatcher(){
		 buffersWatcher = new DatagramBufferWatcher();
	}


	public static int HextoBytes(String Hex)
	{		
		Integer HexInt = Integer.parseInt(Hex, 16);
		byte HexByte = HexInt.byteValue();
		int HexPair = (int) HexByte & 0xFF;
		return HexPair;
	}
	
	private class DatagramBufferWatcher implements Runnable
	{
	
		private Thread T;
		private int interval = 1 * 1000;
		private boolean done = false;

		public DatagramBufferWatcher() 
		{
			T = new Thread(this);
			T.start();
		}

		synchronized public void run() 
		{
			while(!done)
			{
				try
				{ 
					Thread.sleep(interval);
				}
				catch (InterruptedException ioe)
				{
					continue;
				}
				synchronized ( this )
				{
					Iterator<DatagramBuffer> iter = buffers.iterator();
					DatagramBuffer buffer;
					while(iter.hasNext())
					{
						buffer = iter.next();
						buffer.IsTimeOut();
						if(buffer.TimeOut)
						{
							SIDHandler tuple = buffer.TimeOutTuple();
							output(tuple, buffer.FirstFrag);
							iter.remove();
						}
					}
				}
			}
		}
	}
}
