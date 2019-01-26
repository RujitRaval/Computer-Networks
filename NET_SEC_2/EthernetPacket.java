import java.math.BigInteger;

public class EthernetPacket extends Packet 
{
	public static SimplePacketDriver driver;
	public byte[] packet;
	public byte[] DestinationMac;
	public byte[] SourceMac;
	public String EtherType;		
	final int EthernetMacDest = 0;
	final int EthernetMacSrc = 6;
	final int EthernetMacLength = 6;
	final int EthernetType = 12;
	final int EthernetTypeLen = 2;
	final int EthernetPayloadStart = 14;
	final int HexPerLine = 16;
	final static int BytesToBits = 8;
	
	final String IPV4 = "0800";
	final String ARP = "0806";
	
	public EthernetPacket(byte[] packet) 
	{
		this.driver = new SimplePacketDriver();
		this.packet = packet;
		this.Packet_Type = "Ethernet";
		Parse();
	}
  
	public String toString() 
	{
		String out = "";
		out += outln("Source MAC: \t\t\t"+GetAddress(SourceMac));
		out += outln("Destination MAC: \t\t"+GetAddress(DestinationMac));
		out += outln("EtherType: \t\t\t"+EtherType);
		out += outln("Data:\n "+driver.byteArrayToString(packet));	
		return out;
	}
	
	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") );
	}

	private void Parse() 
	{
		byte[] Destination = new byte[6];
		byte[] Source = new byte[6];
		byte[] Type = new byte[2];
		
        System.arraycopy(packet, EthernetMacDest, Destination, 0, EthernetMacLength);
        DestinationMac = Destination;
        
        System.arraycopy(packet, EthernetMacSrc, Source, 0, EthernetMacLength);
        SourceMac = Source;
        
        System.arraycopy(packet, EthernetType, Type, 0, EthernetTypeLen);
        String Ether_Type = HexString(Type);
        if(Ether_Type.equals(IPV4))
		{
        	EtherType = "IPv4";
        }
		else if(Ether_Type.equals(ARP))
		{
        	EtherType = "ARP";
        }
		else
		{
        	EtherType = "Unknown Type: "+Ether_Type;
        }   
	}
	
	public String ToHex()
	{
		String HexData = "";
		for (int i = 0; i < packet.length; i++) 
		{
			if(i%HexPerLine==0)
			{
				HexData+="\n";
			}
			HexData += driver.byteToHex(packet[i]).toLowerCase()+" ";
		}
		HexData+="\n";
		return HexData;
	}
	
	public String outln(String str)
	{
		return str+"\n";
	}
	
	public static String HexString(byte[] b) 
	{
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	public static int BytesToInt(byte[] Data) 
	{
		if (Data == null || Data.length != 4) return 0x0;
		// ----------
		return (int)( 
		(0xff & Data[0]) << 24 |
		(0xff & Data[1]) << 16 |
		(0xff & Data[2]) << 8 |
		(0xff & Data[3]) << 0 );
	}
	
	
	
	public static int UBytesToInt(byte b) 
	{
	    return (int) b & 0xFF;
    }
	
	public String GetSourceMAC()
	{
		return GetAddress(SourceMac);
	}
	
	public String GetDestinationMAC()
	{
		return GetAddress(DestinationMac);
	}
	
	public static String GetAddress(byte[] b)
	{
		return driver.byteArrayToString(b).substring(0,17).replace(" ", "-");
	}

	public static String BytesToBinary(byte[] b)
	{
		int Bits = b.length * BytesToBits;
		BigInteger BigInt = new BigInteger(1, b);
		String STR = BigInt.toString(2);
		while(STR.length() < Bits)
		{	
			STR = "0"+STR;
		}
		return STR;
	}
	
	public static int bytesToDecimal(byte[] b)
	{
		BigInteger BigInt = new BigInteger(1, b);
		return Integer.parseInt(BigInt.toString());
	}
	
}