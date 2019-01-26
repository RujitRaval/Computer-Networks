import java.io.*;
import java.net.*;
import java.util.Scanner;

public class PacketParser 
{
	private SimplePacketDriver driver;	
	private int Count = -1;
	private String IFile, OFile;
	private BufferedWriter BW;
	private String PType = "All";
	private boolean HeaderOnly = false;
	private InetAddress SrcAddress, DestAddress, SrcAddressOR, DestAddressOR, SrcAddressAND, DestAddressAND;
	private Integer SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd;

	public PacketParser() 
	{
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
	
	private void output(Object obj) 
	{
		if(OFile==null)
		{
			if(!HeaderOnly)
			{	
				System.out.println(obj.toString());	
			}
			else
			{
				System.out.println(obj.toString());
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
				PacketHandler handler = new PacketHandler(packet);
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
							PacketHandler handler = new PacketHandler(packet);
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
		private Thread thisThread;
		private byte[] packet;

		public PacketHandler(byte[] packet) 
		{
			this.packet = packet;	
			thisThread = new Thread(this);
			thisThread.start();
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
					if (ipPack.protocol.equals("TCP")) 
					{
						TCPPacket tcpPack = new TCPPacket(packet);
						if (HasPortArgument()) 
						{
							tcpPack = tcpPack.PortFilter(SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd);
						}
						if(tcpPack!=null && tcpPack.PacketType(PType))
						{
							output(tcpPack);
						}
					} 
					else if (ipPack.protocol.equals("UDP")) 
					{
						UDPPacket udpPack = new UDPPacket(packet);
						if (HasPortArgument()) 
						{
							udpPack = udpPack.PortFilter(SrcPortStart, SrcPortEnd, DestPortStart, DestPortEnd);
						}
						if(udpPack!=null && udpPack.PacketType(PType))
						{
							output(udpPack);
						}
						
					} 
					else if (ipPack.protocol.equals("ICMP")) 
					{
						ICMPPacket icmpPack = new ICMPPacket(packet);
						if (!HasPortArgument()) 
						{
							if(icmpPack.PacketType(PType))
							{
								output(icmpPack);
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
							output(arpPack);
						}
					}	
				}
			} else {}
		}
	}
	
	public static int HextoBytes(String Hex)
	{		
		Integer HexInt = Integer.parseInt(Hex, 16);
		byte HexByte = HexInt.byteValue();
		int HexPair = (int) HexByte & 0xFF;
		return HexPair;
	}
}
