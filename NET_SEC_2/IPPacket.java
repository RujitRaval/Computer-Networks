import java.net.*;

public class IPPacket extends EthernetPacket 
{	
	
	final int IPPacketStart = EthernetPayloadStart;	
	final int IPMaxHeaderSize = 60 + EthernetPayloadStart;	
	final int IPProtType = IPPacketStart + (int)(72/BytesToBits);
	final int IPSrc = IPPacketStart + (int)(96/BytesToBits);
	final int IPDst = IPSrc + 4; 
	final int IPLength = IPPacketStart + (int)(16/BytesToBits);
	final int IPIdentification = IPPacketStart + (int)(BytesToBits);
	final int IPFlagFragment = IPPacketStart + (int)(48/BytesToBits);
	final int FragOffsetMultiplier = 8;
	final int IPTTL = IPPacketStart + (int)(64/BytesToBits);
	int IPPayloadStart, IPPayloadEnd;
	
	final String TCP = "06";
	final String UDP = "11";
	final String ICMP = "01";
	
	public int HeaderLen, TotalLength, Ident, FragOffset, TToL;
	public String Protocol, Flags, FlagsFragOffset;
	public InetAddress SourceIP, DestinationIP;
	public int DataLength, First, Last;

	public IPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "IP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();
		out+=outln("Header Length:\t\t\t"+HeaderLen);
		out+=outln("Data Length: \t\t\t"+DataLength); 
		out+=outln("Total Length: \t\t\t"+TotalLength);
		out+=outln("Identification: \t\t"+Ident);
		out+=outln("Flags: \t\t\t\t"+Flags);
		out+=outln("Last Fragment?:\t\t\t"+this.LastFrag());
		out+=outln("Fragment Offset:\t\t"+FragOffset);
		out+=outln("First octet: \t\t\t"+First);
		out+=outln("Last octet: \t\t\t"+Last);
		out+=outln("TTL: \t\t\t\t"+TToL);
		out+=outln("Payload Start:\t\t\t"+IPPayloadStart);
		out+=outln("Protocol:\t\t\t"+Protocol);
		out+=outln("Source Address: \t\t"+SourceIP);
		out+=outln("Destination Address: \t\t"+DestinationIP);
		return out;
	}
	
	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("IP") );
	}
	
	private void Parse()
	{
		byte[] HeaderLength = new byte[1];
		byte[] IPProt = new byte[1];
		byte[] Source = new byte[4];
		byte[] Destination = new byte[4];
		byte[] Len = new byte[2];
		byte[] IPIdent = new byte[2];
		byte[] FlagAndFrag = new byte[2];
		byte[] TtL = new byte[1];
		
        System.arraycopy(packet, IPPacketStart, HeaderLength, 0, 1);
        String StringHLen = driver.byteToHex(HeaderLength[0]);
        HeaderLen = GetHeaderLength(StringHLen.charAt(1));
        
		IPPayloadStart = IPPacketStart + HeaderLen;
        
		System.arraycopy(packet, IPLength, Len, 0, 2);
        TotalLength = bytesToDecimal(Len);
        DataLength = TotalLength - HeaderLen;
        System.arraycopy(packet, IPIdentification, IPIdent, 0, 2);
        Ident = bytesToDecimal(IPIdent);
        System.arraycopy(packet, IPFlagFragment, FlagAndFrag, 0, 2);
        parseFlagFrag(FlagAndFrag);
        System.arraycopy(packet, IPTTL, TtL, 0, 1);
        TToL = bytesToDecimal(TtL);
		
        System.arraycopy(packet, IPProtType, IPProt, 0, 1);
        String ProtocolNum = HexString(IPProt);
        if(ProtocolNum.equals(TCP))
		{
        	Protocol = "TCP";
        }
		else if(ProtocolNum.equals(UDP))
		{
        	Protocol = "UDP";
        }
		else if(ProtocolNum.equals(ICMP))
		{
        	Protocol = "ICMP";
        }
		else
		{
        	Protocol = "Unrecognized IP protocol: "+ProtocolNum;
        }
        
        System.arraycopy(packet, IPSrc, Source, 0, 4);
        System.arraycopy(packet, IPDst, Destination, 0, 4);
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

	private void parseFlagFrag(byte[] FlagAndFrag) 	
	{
		String binaryFlagFrag = BytesToBinary(FlagAndFrag);
		FlagsFragOffset = binaryFlagFrag;
		Flags = binaryFlagFrag.substring(0, 3);
		FragOffset = Integer.parseInt(binaryFlagFrag.substring(3), 2);
		First = (FragOffset * FragOffsetMultiplier);
		Last = (FragOffset* FragOffsetMultiplier) + DataLength;
	}
	
	public boolean LastFrag(){
		return Flags.charAt(2)=='0';
	}
	
	public boolean FirstFrag(){
		return FragOffset==0;
	}
	
	private int GetHeaderLength(char HLenChar) 
	{
		String HLString = String.valueOf(HLenChar);
		int HLValue = Integer.parseInt(HLString, 16);
		return HLValue * 4; 
	}
	
	public byte[] getData(){
		byte[] packetData = new byte[DataLength];
		System.arraycopy(packet, IPPayloadStart, packetData, 0, DataLength);
		return packetData;
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
