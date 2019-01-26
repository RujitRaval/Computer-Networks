import java.net.InetAddress;
import java.util.Vector;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class DatagramBuffer 
{
	
	final static int MaxSize = 65536;
	final static int Infinity = MaxSize+1;
	final static int LifeTime = 5 * 1000; 
	int Ident;
	int HeaderOffset, Size, LastOctet;
	EthernetPacket reassembled;
	InetAddress SrcIP, DestIP;
	String protocol, SrcMAC, DestMAC;
	public Vector<Hole> HList = new Vector<Hole>();
	public Vector<EthernetPacket> Frags  = new Vector<EthernetPacket>();
	IPPacket FirstFrag;
	byte[] packet = new byte[MaxSize];
	boolean[] UsedBytes = new boolean[MaxSize];
	boolean[] OverlapBytes = new boolean[MaxSize];
	boolean Overlap = false;
	boolean Oversize = false;
	boolean TimeOut = false;
	boolean FirstArrived = false;
	boolean LastArrived = false;
	long CreationTime = System.currentTimeMillis();

	public DatagramBuffer(IPPacket Frag) 
	{
		this.Ident = Frag.Ident;
		this.protocol = Frag.Protocol;
		this.SrcMAC = Frag.GetSourceMAC();
		this.DestMAC = Frag.GetDestinationMAC();
		this.SrcIP = Frag.SourceIP;
		this.DestIP = Frag.DestinationIP;		
		this.FirstFrag = Frag;
		this.HList.add( new Hole(0,Infinity) );
		this.HeaderOffset = Frag.IPMaxHeaderSize;
	}
	
	public SIDHandler GenerateTuple()
	{
		int SID = -1;
		if(!Overlap && !Oversize)
		{
			SID = SIDHandler.CorrectSID;
		}
		else if(Oversize)
		{
			SID = SIDHandler.OversizeSID;
		}
		else if(Overlap)
		{
			SID = SIDHandler.OverlapSID;
		}
		this.reassembled = new IPPacket(packet);
		return new SIDHandler(SID, reassembled, Frags);
	}
	
	public SIDHandler OversizeTuple()
	{
		return new SIDHandler(SIDHandler.OversizeSID, this.FirstFrag, Frags);
	}
	
	public SIDHandler TimeOutTuple()
	{
		return new SIDHandler(SIDHandler.TimeoutSID, this.FirstFrag, Frags);
	}
	
	public void IsTimeOut()
	{
		long now = System.currentTimeMillis();
		if( (now - CreationTime) >= LifeTime )
		{
			TimeOut = true;
		}
	}
	


	public boolean Match(IPPacket Frag)
	{
		if ( Frag.Ident==Ident && 
				Frag.Protocol.equals(protocol) &&
				Frag.SourceIP.equals(SrcIP) &&
				Frag.DestinationIP.equals(DestIP) &&
				Frag.GetSourceMAC().equals(SrcMAC) &&
				Frag.GetDestinationMAC().equals(DestMAC))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	synchronized public SIDHandler AddFragment(IPPacket Frag) 
	{
		Frags.add(Frag);
		boolean firstFrag = Frag.FirstFrag();
		boolean lastFrag = Frag.LastFrag();
		if(firstFrag)
		{ 
			FirstArrived = true;
			HeaderOffset = Frag.IPPayloadStart;
			System.arraycopy(Frag.packet, 0, this.packet, 0, Frag.IPPayloadStart);
			if(!LastArrived)
			{
				System.arraycopy(this.packet, Frag.IPMaxHeaderSize+1, this.packet, Frag.IPPayloadStart, MaxSize-Frag.IPMaxHeaderSize-1);	
			}
		}
		
		if(lastFrag)
		{ 
			LastArrived = true;
			LastOctet = Frag.Last;	
		}
		
		if(FirstArrived && LastArrived)
		{
			Size = HeaderOffset + LastOctet;
			if(packet.length > Size)
			{
				byte[] newPacket = new byte[Size];
				boolean[] newUsed = new boolean[Size];
				boolean[] newOverlap = new boolean[Size];
				System.arraycopy(packet, 0, newPacket, 0, Size);
				System.arraycopy(UsedBytes, 0, newUsed, 0, Size);
				System.arraycopy(OverlapBytes, 0, newOverlap, 0, Size);
				this.packet = newPacket;
				this.UsedBytes = newUsed;
				this.OverlapBytes = newOverlap;
			}
			
			if(Size > MaxSize)
			{
				Oversize = true;
				return OversizeTuple();
			}
		}
		
		Vector<Hole> tempHList = new Vector<Hole>(HList); 
		for (Hole hole : tempHList) 
		{
			if( Frag.First <= hole.last && Frag.Last >= hole.first)
			{	
				WriteData(Frag);
				HList.remove(hole);
				if (Frag.First > hole.first)
				{
					HList.add( new Hole(hole.first, Frag.First-1) );
				}
				if (Frag.Last < hole.last)
				{
					HList.add( new Hole(Frag.Last+1, hole.last) );
				}
			}
		}
		
		
		if(lastFrag)
		{
			tempHList = new Vector<Hole>(HList);
			for(Hole holeCheck : tempHList)
			{
				if(holeCheck.last==Infinity)
				{
					HList.remove(holeCheck);
				}
			}
		}
		

		if (HList.size() == 0)
		{ 
			return GenerateTuple();
		}
		else if (TimeOut) 
		{	
			return TimeOutTuple();
		}
		else
		{ 
			return null; 
		}
		
	}

	private void WriteData(IPPacket Frag) 
	{
		byte[] FragData = Frag.getData();
		int FirstOct = Frag.First;
		for (int i = 0; i < FragData.length; i++) 
		{
			int place = this.HeaderOffset + FirstOct + i;
			if(UsedBytes[place])
			{
				packet[place] = FragData[i];
				Overlap = true;
				OverlapBytes[place] = true;
			}
			else
			{
				packet[place] = FragData[i];
				UsedBytes[place] = true;
			}
		}
	}
	
	public static int bytesToDecimal(byte[] b)
	{
		BigInteger bigInt = new BigInteger(1, b);
		return Integer.parseInt(bigInt.toString());
	}
}