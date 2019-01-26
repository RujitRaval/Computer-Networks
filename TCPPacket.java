public class TCPPacket extends IPPacket
{	
	final int TCPStart = IPPayloadStart;
	final int TCPPortLength = (int)(16/BytesToBits);
	final int TCPSrcPort = TCPStart;
	final int TCPDstPort = TCPSrcPort + TCPPortLength;
	public int SrcPort, DstPort;

	public TCPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "TCP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();	
		out+=outln("Source Port: "+SrcPort);
		out+=outln("Destination Port: "+DstPort);
		out+=outln("-----------------------------------------------------------------------------------");
		return out;
	}
	
	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("IP") || type.equals("TCP") );
	}
	
	private void Parse()
	{
		byte[] Source = new byte[4];
		byte[] Destination = new byte[4];	
        System.arraycopy(packet, TCPSrcPort, Source, 2, TCPPortLength);
        System.arraycopy(packet, TCPDstPort, Destination, 2, TCPPortLength);
        SrcPort = BytesToInt(Source);
        DstPort = BytesToInt(Destination);
	}

	public TCPPacket PortFilter(Integer SourcePortStart, Integer SourcePortEnd, Integer DestinationPortStart, Integer DestinationPortEnd) 
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