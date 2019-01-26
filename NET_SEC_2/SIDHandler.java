import java.util.Vector;

public class SIDHandler 
{
	final static int ArpSID = 0;
	final static int CorrectSID = 1;
	final static int OverlapSID = 2;
	final static int OversizeSID = 3;
	final static int TimeoutSID = 4;
	int SID;
	EthernetPacket Packet;
	String TYPE;
	Vector<EthernetPacket> Fragments = new Vector<EthernetPacket>();
	
	public SIDHandler(int sid, EthernetPacket packet, Vector<EthernetPacket> fragments) 
	{
		this.SID = sid;
		this.Packet = packet;
		this.Fragments = fragments;
		this.TYPE = Fragments.elementAt(0).Packet_Type;
	}
	
	public SIDHandler(int sid, EthernetPacket packet) {
		this.SID = sid;
		this.Packet = packet;
		Fragments.add(this.Packet);
	}
	
	public String toString(){
		return String.format("Type: \t\t\t\t%s \nPacket Size: \t\t\t%s \nSID: \t\t\t\t%s \nNumber of Fragments: \t\t%s", this.TYPE, PacketSize(), this.SID,this.Fragments.size());
	}
	
	private String PacketSize()
	{
		int PacketBytes = Packet.packet.length;
		return String.valueOf(PacketBytes)+" b";	
	}
}
