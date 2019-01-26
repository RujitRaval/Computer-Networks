public class UDPPacket extends IPPacket
{	
	final int UDPStart = IPPayloadStart;
	final int UDPPortLength = (int)(16/BytesToBits);
	final int UDPSrcPort = UDPStart;
	final int UDPDstPort = UDPSrcPort + UDPPortLength;	
	public int SrcPort, DstPort;

	public UDPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "UDP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();	
		out+=outln("Source Port: \t\t\t"+SrcPort);
		out+=outln("Destination Port: \t\t"+DstPort);
		out+=outln("-----------------------------------------------------------------------------------");
		return out;
	}
	
	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("ip") || type.equals("UDP") );
	}
	
	private void Parse()
	{
		byte[] Source = new byte[4];
		byte[] Destination = new byte[4];	
        System.arraycopy(packet, UDPSrcPort, Source, 2, UDPPortLength);
        System.arraycopy(packet, UDPDstPort, Destination, 2, UDPPortLength);
        SrcPort = BytesToInt(Source);
        DstPort = BytesToInt(Destination);
	}
	
	public UDPPacket PortFilter(Integer SourcePortStart, Integer SourcePortEnd, Integer DestinationPortStart, Integer DestinationPortEnd) 
	{
		if(SourcePortStart!=null && SourcePortEnd!=null)
		{
			if(SourcePortStart <= SrcPort && SrcPort <= SourcePortEnd) return this;
		}
		if(DestinationPortStart!=null && DestinationPortEnd!=null)
		{
			if(DestinationPortStart <= DstPort && DstPort <= DestinationPortEnd) return this;
		}
		return null;
	}
}