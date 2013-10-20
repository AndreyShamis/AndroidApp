package com.example.com.andrey.app;

import java.net.InetAddress;
import java.util.Collection;

import android.R.string;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
@SuppressLint("NewApi")
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager 			mManager;
    private String					m_P2pSSID			= "";
    private String					m_P2PInterfaceName 	= "";
    private WifiP2pGroup			m_P2PGroup;
    private Channel 				mChannel;
    private MainActivity 			mActivity;
    private WifiP2pDeviceList 		Ourpeers 		= new WifiP2pDeviceList();
    private WifiP2pConfig 			config 			= new WifiP2pConfig(); 
	private NetworkInfo 			netInfo;
	private String					connectStatus	=	"Not connected";
	
	private PeerListListener 		myPeerListListener = new PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			Ourpeers = peers;
		}
	};
	
	public void clearLocalServices(){
		mManager.clearLocalServices(mChannel, new ActionListenerImpl("clearLocalServices"));
	}
	
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;        
    }
    
    /**
     * Get p2p interface name. Example p2p-p2p0-9
     * @return String p2p interface name
     */
    public String  API_GetP2PInterfaceName(){
    	return m_P2PInterfaceName;
    }
    
    public String getConnectionStatus(){
    	return connectStatus;
    }
    
	public void SetDeviceMacAddress(String mac){
    	config.deviceAddress = mac;
    }

	public void API_P2pConnect(String PeerMacAddress,Integer wpsMethod,Integer goIntent){
		WifiP2pDevice dev 				= this.getDeviceByMac( PeerMacAddress);
		if(dev != null){
	    	this.config.deviceAddress 		= dev.deviceAddress;
	    	this.config.wps.pin				= "10293847";
	    	this.config.wps.setup 			= wpsMethod;
	    	this.config.groupOwnerIntent 	= goIntent;
	    	mManager.connect(mChannel, this.config, new ActionListenerImpl("connect"));
		}
	}
	
	public Integer P2PMethodToInt(String str)
	{
		Integer ret = 0;
		if(str.equals("PBC"))
			ret = WpsInfo.PBC;
		else if(str.equals("KEYPAD"))
			ret = WpsInfo.KEYPAD;
		else if(str.equals("LABEL"))
			ret = WpsInfo.LABEL;	
		else if(str.equals("DISPLAY"))
			ret = WpsInfo.DISPLAY;
		
		return ret;
	}
	
	public Integer API_P2pGetDevicesCount(){
		Integer ret = 0;
		Collection<WifiP2pDevice> devs = Ourpeers.getDeviceList();
		ret = devs.size();
		return ret;
	}
	
	public void API_P2pDisconnect(){
        if (mManager != null) {
            mManager.cancelConnect(mChannel, new ActionListenerImpl("cancelConnect"));
        }
	}
	
	public void API_P2pDiscoverPeers(){
        mManager.discoverPeers(mChannel,new ActionListenerImpl("discoverPeers"));
	}

    private void ConnectionChangedAction(Intent intent)
    {
    	netInfo = (NetworkInfo)intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
    	WifiP2pInfo wifip2pinfo = (WifiP2pInfo) intent.getParcelableExtra(
                WifiP2pManager.EXTRA_WIFI_P2P_INFO);
    	
    	mManager.requestGroupInfo(mChannel, new GroupInfoL());
        if (netInfo.isConnected()) {
            ConnectionInfoListener connectionListener = new ConnInfoList();
			mManager.requestConnectionInfo(mChannel, connectionListener );
			connectStatus = "Connecting";

        }else{
        	connectStatus = "Not connected";
        }
    }

    public void API_P2pRemoveGroup(){
		mManager.removeGroup(mChannel,  new ActionListenerImpl("removeGroup"));
    }
    
	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
        	this.RequestPeers(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
        	this.ConnectionChangedAction(intent);	// Respond to new connection or disconnections
        }
    }

    private void RequestPeers( Intent intent){
        if (mManager != null) {
            mManager.requestPeers(mChannel, myPeerListListener);
        }
    }
	
	/**
	 * Get Collection<WifiP2pDevice> of p2p devices
	 * @return Collection<WifiP2pDevice> 
	 */
	public Collection<WifiP2pDevice> getP2PDevices(){
		return Ourpeers.getDeviceList();
	}
	
	/**
	 * Get device from discovered P2P devices by Mac address
	 * @param mac Mac Address
	 * @return WifiP2pDevice
	 */
	public WifiP2pDevice getDeviceByMac(String mac)
	{
		WifiP2pDevice ret = new WifiP2pDevice();
		try{
			Collection<WifiP2pDevice> devs = Ourpeers.getDeviceList();
			for (WifiP2pDevice dev : devs){
				if( dev.deviceAddress.trim().equalsIgnoreCase(mac.trim())){
					ret = dev;
					break;
				}
			}
		}
		catch(Exception ex){}
		return ret;
	}
    
    /**
     * Get P2P Group Name/SSID
     * @return String
     */
    public String getP2pGroupName(){
    	return m_P2pSSID;
    }
    
    //*****************************************************************//
    //*****************************************************************//
    //*****************************************************************//
    /**
     * Implementation of ConnectionInfoListener
     * @author Andrey Shamis
     */
    private class ConnInfoList implements ConnectionInfoListener{
		@Override
		public void onConnectionInfoAvailable(final WifiP2pInfo info) {
	        if (info.groupFormed && info.isGroupOwner) {
	        	connectStatus	=	"GO";
	        } else if (info.groupFormed) {
	        	connectStatus	=	"Client";
	        }
	    }
    }
    //*****************************************************************//
    private class GroupInfoL implements GroupInfoListener{
		@Override
		public void onGroupInfoAvailable(WifiP2pGroup group) {
			m_P2PGroup = group;
			if(m_P2PGroup != null){
				m_P2PInterfaceName = m_P2PGroup.getInterface();
				m_P2pSSID = m_P2PGroup.getNetworkName();
			}
		}
    	
    }
    //*****************************************************************//
    /**
     * Implementation of ActionListener
     * @author Andrey Shamis
     */
    private class ActionListenerImpl implements ActionListener{
    	public ActionListenerImpl(String newActionType ){
    	}

		@Override
		public void onFailure(int reason) {
		}
		
		@Override
		public void onSuccess() {
		}
    }
    //*****************************************************************//
    //*****************************************************************//
    //*****************************************************************//
}
