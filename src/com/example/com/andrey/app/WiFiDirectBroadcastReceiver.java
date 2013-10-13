package com.example.com.andrey.app;

import java.net.InetAddress;
import java.util.Collection;

import android.R.string;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
@SuppressLint("NewApi")
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	public class P2PGroupOwner extends Thread
	{
		
	}
	
	public class P2PClient extends Thread
	{
		
	}
	public P2PClient m_Client;
	public P2PGroupOwner m_Go;
	public Boolean isGO = false;
    public class ConnInfoList implements ConnectionInfoListener
    {

		@Override
		public void onConnectionInfoAvailable(final WifiP2pInfo info) {
	        // InetAddress from WifiP2pInfo struct.
	        //InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
	        // After the group negotiation, we can determine the group owner.
	        if (info.groupFormed && info.isGroupOwner) {
	        	AppendToText("Do whatever tasks are specific to the group owner. One common case is creating a server thread and accepting incoming connections");
	            // Do whatever tasks are specific to the group owner.
	            // One common case is creating a server thread and accepting
	            // incoming connections.
	        	isGO = true;
	        	
	        	m_Go = new P2PGroupOwner();
	        	
	        } else if (info.groupFormed) {
	        	AppendToText("The other device acts as the client. In this case,	 you'll want to create a client thread that connects to the group owner.");
	            // The other device acts as the client. In this case,
	            // you'll want to create a client thread that connects to the group
	            // owner.
	        	m_Client = new P2PClient();
	        }
	    }
    }  
	
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;
    WifiP2pDeviceList Ourpeers = new WifiP2pDeviceList();
    WifiP2pDevice device = new WifiP2pDevice();
    
	public WifiP2pConfig config = new WifiP2pConfig();   
    
	public void SetDeviceMacAddress(String mac)
    {
    	config.deviceAddress = mac;
    }
    
    
    PeerListListener myPeerListListener = new PeerListListener() {
		@Override
		public void onPeersAvailable(WifiP2pDeviceList peers) {
			// TODO Auto-generated method stub
			printPeers(peers);
		}
	};

	
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
    		MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        
    }

    private void ConnectionChangedAction(Intent intent)
    {
    	AppendToText("WIFI_P2P_CONNECTION_CHANGED_ACTION");  
    	
        NetworkInfo networkInfo = (NetworkInfo) intent
                .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

        if (networkInfo.isConnected()) {
            // We are connected with the other device, request connection
            // info to find group owner IP
        	AppendToText("We are connected"); 
            ConnectionInfoListener connectionListener = new ConnInfoList();
			mManager.requestConnectionInfo(mChannel, connectionListener );
        }

    }

	@Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
        	this.P2PStateChanged(intent);
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
        	this.RequestPeers(intent);
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
        	this.ConnectionChangedAction(intent);
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
        	AppendToText("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION"); 
            // Respond to this device's wifi state changing
        }
    }

    private void RequestPeers( Intent intent)
    {
        if (mManager != null) {
            mManager.requestPeers(mChannel, myPeerListListener);
            AppendToText("WIFI_P2P_PEERS_CHANGED_ACTION");  
        }
    }
    
    private void P2PStateChanged(Intent intent)
    {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
        {
        	AppendToText("Wifi P2P is enabled");
            // Wifi P2P is enabled
        } else {
        	AppendToText("Wi-Fi P2P is not enabled");
            // Wi-Fi P2P is not enabled
        }

    }
	public void printPeers(WifiP2pDeviceList peers)
	{
		Collection<WifiP2pDevice> devs = peers.getDeviceList();
		for (WifiP2pDevice dev : devs){
			AppendToText(dev.deviceName +  " " + dev.deviceAddress);
		}
		Ourpeers = peers;
	}
	
	public WifiP2pDevice getDeviceByMac(String mac)
	{
		WifiP2pDevice ret = new WifiP2pDevice();
		try
		{
			Collection<WifiP2pDevice> devs = Ourpeers.getDeviceList();
			for (WifiP2pDevice dev : devs)
			{
				if( dev.deviceAddress.trim().equalsIgnoreCase(mac.trim()))
				{
					ret = dev;
					break;
				}
			}
		}
		catch(Exception ex){}
		return ret;
	}

    private void PrintToText(String string)
    {
    	this.mActivity.text.setText(string);
    }
    
    private void AppendToText(String string)
    {
    	this.mActivity.text.append("\n" + string);
    }
}