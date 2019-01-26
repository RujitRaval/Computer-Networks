//
// Class: SimplePacketDriver
//
// Written by Mauricio Papa
//    with modifications by Aaron Curley.
//
// Information:  Here, we simply attempt to load the appropriate DLL and
//               define the native interfaces.
//
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------

public class SimplePacketDriver{

    //--------------------------------------------------------------------------

    public SimplePacketDriver(){
        super();
    }

    //--------------------------------------------------------------------------

    public native boolean openAdapter(String adapterName);
    public native String[] getAdapterNames();
    public native byte[] readPacket();
    public native boolean sendPacket(byte[] packet);

    //--------------------------------------------------------------------------

    static{
        try{
            System.loadLibrary("simplepacketdriver_x64");
        }catch(Error e){
            System.loadLibrary("simplepacketdriver");
        }
    }

    //--------------------------------------------------------------------------

}

//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
