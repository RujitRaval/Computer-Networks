import java.net.*;

public class ARPPacket extends EthernetPacket 
{	
	final int ARPPacketStart = EthernetPayloadStart;
	final int ARPAddressLength = ARPPacketStart + (int)(32/BytesToBits);
	final int ARPProtAddressLength = ARPAddressLength + 1;	
	final int ARPSenderAddress = ARPPacketStart + (int)(BytesToBits);

	Packet p = new Packet();	
	public int HWAddressLength, ProtocolAddressLength;
	public byte[] SenderAddress;
	public byte[] TargetAddress;
	public InetAddress SenderIP, TargetIP;
	
	public ARPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "ARP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();
		out+=outln("Hardware Address Length: "+HWAddressLength);
		out+=outln("IP Address Length: "+ProtocolAddressLength);
		out+=outln("Sender Hardware Address: "+GetAddress(SenderAddress));
		out+=outln("Sender IP Address: "+SenderIP);
		out+=outln("Target Hardware Address: "+GetAddress(TargetAddress));
		out+=outln("Target IP Address: "+TargetIP);
		out+=outln("-----------------------------------------------------------------------------------");
		return out;
	}
	

	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("ARP") );
	}
	
	private void Parse()
	{
		byte[] HWLen = new byte[1];
		byte[] ProtocolLength = new byte[1];
		
		byte[] SenderIPAddr;
		byte[] TargetIPAddr;
		
		System.arraycopy(packet, ARPAddressLength, HWLen, 0, 1);
		HWAddressLength = UBytesToInt(HWLen[0]);
        
        System.arraycopy(packet, ARPProtAddressLength, ProtocolLength, 0, 1);
        ProtocolAddressLength = UBytesToInt(ProtocolLength[0]);
		
		SenderAddress = new byte[HWAddressLength];
		TargetAddress = new byte[HWAddressLength];
		SenderIPAddr = new byte[ProtocolAddressLength];
		TargetIPAddr = new byte[ProtocolAddressLength];
		
		int POS = ARPSenderAddress;
		System.arraycopy(packet, POS, SenderAddress, 0, HWAddressLength);
		POS = POS + HWAddressLength;
		
		System.arraycopy(packet, POS, SenderIPAddr, 0, ProtocolAddressLength);
		POS = POS + ProtocolAddressLength;
		
		System.arraycopy(packet, POS, TargetAddress, 0, HWAddressLength);
		POS = POS + HWAddressLength;
		
		System.arraycopy(packet, POS, TargetIPAddr, 0, ProtocolAddressLength);
		POS = POS + ProtocolAddressLength;
		
        try 
		{
			SenderIP = InetAddress.getByAddress(SenderIPAddr);
			TargetIP = InetAddress.getByAddress(TargetIPAddr);
		} 
		catch (Exception e) 
		{
			System.out.println("Could not get the addresses !!");
		}
	}
	
	public ARPPacket AddressFilter(InetAddress Source, InetAddress Destination, InetAddress SourceOR, InetAddress DestinationOR, InetAddress SourceAND, InetAddress DestinationAND) 
	{
		if(SenderIP.equals(Source)) return this;
		if(TargetIP.equals(Destination)) return this;
		if(SenderIP.equals(SourceOR) || TargetIP.equals(DestinationOR)) return this;
		if(SenderIP.equals(SourceAND) && TargetIP.equals(DestinationAND)) return this;
		return null;
	}
}