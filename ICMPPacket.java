public class ICMPPacket extends IPPacket
{
	final int ICMPPacketStart = IPPayloadStart;	
	final int ICMPTypeLength = (int)(8/BytesToBits);
	final int ICMPCodeLength = (int)(8/BytesToBits);
	
	final int ICMPType = ICMPPacketStart;
	final int ICMPCode = ICMPType+ICMPTypeLength;
	
	public int Type, Code;
	
	public ICMPPacket(byte[] packet) 
	{
		super(packet);
		this.Packet_Type = "ICMP";
		Parse();
	}
	
	public String toString() 
	{
		String out = super.toString();
		out+=outln("Type: "+Type);
		out+=outln("Code: "+Code);
		out+=outln("-----------------------------------------------------------------------------------");
		return out;
	}

	public boolean PacketType(String type)
	{
		return ( type.equals("All") || type.equals("Ethernet") || type.equals("IP") || type.equals("ICMP") );
	}
	
	private void Parse()
	{
		byte[] type = new byte[1];
		byte[] code = new byte[1];	
        System.arraycopy(packet, ICMPType, type, 0, ICMPTypeLength);
        System.arraycopy(packet, ICMPCode, code, 0, ICMPCodeLength);
        Type = UBytesToInt(type[0]);
        Code = UBytesToInt(code[0]);
	}
}