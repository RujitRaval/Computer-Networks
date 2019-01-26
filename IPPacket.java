import java.net.*;

public class IPPacket extends EthernetPacket 
{	
	final int IPPacketStart = EthernetPayloadStart;	
	final int IPHLength = 1;
	final int ProtocolType = IPPacketStart + (int)(72/BytesToBits);
	final int ProtocolTypeLength = 1;
	final int IPSrc = IPPacketStart + (int)(96/BytesToBits);
	final int IPAddressLength = 4;
	final int IPDst = IPSrc + IPAddressLength; 
	
	int IPPayloadStart;
	
	final String TCP = "06";
	final String UDP = "11";
	final String ICMP = "01";
	
	public int HeaderLen;
	public String protocol;
	public InetAddress SourceIP, DestinationIP;

	public IPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "IP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();
		out+=outln("Header Length: "+HeaderLen);
		out+=outln("Payload Start: "+IPPayloadStart);
		out+=outln("Protocol: "+protocol);
		out+=outln("Source Address: "+SourceIP);
		out+=outln("Destination Address: "+DestinationIP);
		return out;
	}
	

	
	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("IP") );
	}
	
	private void Parse()
	{
		byte[] HeaderLength = new byte[1];
		byte[] Protocol = new byte[1];
		byte[] Source = new byte[4];
		byte[] Destination = new byte[4];
		
        System.arraycopy(packet, IPPacketStart, HeaderLength, 0, IPHLength);
        String StringHLen = driver.byteToHex(HeaderLength[0]);
        HeaderLen = GetHeaderLen(StringHLen.charAt(1));
        IPPayloadStart = IPPacketStart + HeaderLen;
        
        System.arraycopy(packet, ProtocolType, Protocol, 0, ProtocolTypeLength);
        String ProtocolNum = HexString(Protocol);
        if(ProtocolNum.equals(TCP))
		{
        	protocol = "TCP";
        }
		else if(ProtocolNum.equals(UDP))
		{
        	protocol = "UDP";
        }
		else if(ProtocolNum.equals(ICMP))
		{
        	protocol = "ICMP";
        }
		else
		{
        	protocol = "Unrecognized IP protocol: "+ProtocolNum;
        }
        
        System.arraycopy(packet, IPSrc, Source, 0, IPAddressLength);
        System.arraycopy(packet, IPDst, Destination, 0, IPAddressLength);
        try 
		{
			DestinationIP = InetAddress.getByAddress(Destination);
			SourceIP = InetAddress.getByAddress(Source);
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}
	}

	private int GetHeaderLen(char HLenChar) 
	{
		String HLString = String.valueOf(HLenChar);
		int HLValue = Integer.parseInt(HLString, 16);
		return HLValue * 4; 
	}
	public IPPacket AddressFilter(InetAddress Source, InetAddress Destination, InetAddress SourceOR, InetAddress DestinationOR, InetAddress SourceAND, InetAddress DestinationAND) 
	{
		if(SourceIP.equals(Source)) return this;
		if(DestinationIP.equals(Destination)) return this;
		if(SourceIP.equals(SourceOR) || DestinationIP.equals(DestinationOR)) return this;
		if(SourceIP.equals(SourceAND) && DestinationIP.equals(DestinationAND)) return this;
		return null;
	}
}
