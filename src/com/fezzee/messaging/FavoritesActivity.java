package com.fezzee.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.vessel.VesselSDK;
//import android.preference.PreferenceManager;


public class FavoritesActivity extends Activity {
	
	//INVESTIGATION- there were two ways of specifying PREFS- one using the default PREFS 
	//and one specifying- is there any security implications of not using default?
	//private static final String PREFS_NAME = "TwitterLogin";
	SharedPreferences settings = null;
	SharedPreferences.Editor editor = null;
	
	
	protected static String USERNAME;// = "gene"; //fola  //test  //gene4
	private static String PASSWORD;// = "gene123"; //fola123  //test123  //gene4
	protected static String HOST;// = "ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
	private static int PORT;// = 5222;
	private static String SERVICE;// = "ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
	
	private static final String RESOURCE = "Smack"; //INVESTIGATION-this should REALLY be a unique deviceID right?
	
    //private ArrayList<String> contacts = new ArrayList<String>();
    private ArrayList<FavoriteItem> contacts = new ArrayList<FavoriteItem>();
    
    private Handler mHandler = new Handler();
	
	protected static XMPPConnection connection;
	
    private ListView listview;
    private Collection<RosterEntry> entries;
    private Roster roster;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		VesselSDK.initialize(getApplicationContext(),"bVJXMkJ3clVqbEs1czBkblpUSHUxekNQ");
		setContentView(R.layout.activity_favorites);
		
		Log.d("FavoritesActivity","Entered...");
		
		//settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = settings.edit();
		
		USERNAME = settings.getString("userName","");//gene //fola
		PASSWORD = settings.getString("userPwd","");//"gene123"; //fola123
		HOST = settings.getString("host","");//"ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		PORT = Integer.parseInt( settings.getString("port","0") );//5222;
		SERVICE = settings.getString("service","");//"ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		 
        // first time run?
        if (settings.getBoolean("firstTimeRun", true)) 
        {
        	
            startActivity(new Intent(getBaseContext(), SettingsPreferenceActivity.class));
            finish();
            return;
        }
        
        setTitle("Mashing as " + USERNAME.split("@")[0]);
        
		listview = (ListView) this.findViewById(R.id.listContacts);
		
		
	    final ProgressDialog dialog = ProgressDialog.show(this, "Connecting...", "Please wait...", false);
	    Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	        // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(HOST, PORT, SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	           connection.connect();
	           Log.d("FavoritesActivity",  "[SettingsDialog] Connected to "+connection.getHost());
	        
	            connection.login(USERNAME, PASSWORD, RESOURCE);
	            Log.d("FavoritesActivity::onCreate",  "Logged in as " + connection.getUser());
	            
	            // Set My status to available
	            Presence presence = new Presence(Presence.Type.available);
	            connection.sendPacket(presence);
	            
	            //fetch the roster from the server
	           roster = connection.getRoster();
	           //Auto subscribe is the default
	           roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
	           
	           entries = roster.getEntries();
	           
	           //IMPORTANT NOTE- The Roster that is fetched here may not have accurate presence data. The RosterListener takes over and 
	           //presenceChanged is called once the Roster is fully updated. After addRosterListener is defined, Subscription
	           //notifications are listened for by the call to connection.addPacketListener
	           try{
	            	for (RosterEntry entry : entries) 
	            	{
	            		
	            		Presence entryPresence = roster.getPresence(entry.getUser()); 
	            		//when this is first run, the presence of all may be 'unavailable'
	            		
	            		Log.d("onCreate", "Initial Presence : " + entryPresence);
		             
	            		FavoriteItem item = new FavoriteItem(entry.getUser(), R.drawable.ic_launcher, entry.getName(), entryPresence.getStatus(), R.drawable.red_balls);
	 		       	    contacts.add(item);
	            	}
	            
	            	if (connection != null)
	            	{
	            		mHandler.post(new Runnable() {
	            			public void run() {
	            				Log.d("onCreate",  "On UI Thread, setListAdapter");
	            				setListAdapter();
	            			}
	            		});
	            	}
	            	
	           } catch (Exception e) {
	            	Log.e("onCreate",  "Error: " + e.getMessage());
	            	e.printStackTrace();
	            }
	           

	           //this will add a FavoriteItem if its doesn't already exist
	            roster.addRosterListener(new RosterListener() {

	                public void entriesAdded(Collection<String> param) {
	                	
	                	Log.i("entriesAdded",  "Entered: " + param);
	                	
                            //if we find any existing jid's in the param collection, remove it from the param colection
	                		for (int i = 0; i < contacts.size(); i++)
				            {
				            	FavoriteItem fav= contacts.get(i);
				            	if ( param.contains(fav.getJID()))
				            	{
				            		Log.d("entriesAdded",  "User Found, so removing it");
				            		param.remove(fav.getJID());
				            	}
				            }
	                		//now add any remaining JIDs to contacts.
	                		for (Iterator<String> Params = param.iterator(); Params.hasNext();) {
	                			FavoriteItem item = new FavoriteItem(Params.next(),R.drawable.ic_launcher,"","",0);
	                			contacts.add(item);
	                		}
	                	    //this isn't really necessary, as we're only adding a stub
	    	            	if (connection != null)
	    	            	{
	    	            		mHandler.post(new Runnable() {
	    	            			public void run() {
	    	            				Log.d("entriesAdded",  "On UI Thread, setListAdapter");
	    	            				setListAdapter();
	    	            			}
	    	            		});
	    	            	}
	    	            
	                }

	                public void entriesDeleted(Collection<String> addresses) {
	                	Log.i("entriesDeleted",  "Entered: " + addresses);
	                	
	                	for (int i = 0; i < contacts.size(); i++)
			            {
			            	FavoriteItem fav= contacts.get(i);
			            	Log.i("entriesDeleted",  "Fav item: " + i + " : " + fav);
			            	if ( addresses.contains(fav.getJID()))
			            	{
			            		Log.d("entriesDeleted",  "User Found, Deleting: " + fav.getJID());
			            		contacts.remove(fav);
			            	}
			            }
	                	Log.d("entriesDeleted",  "Contact removed: ");
	                	try{
	                	Thread.sleep(1000);
	                	} catch (Exception e) {
	                		return;
	                	}
	    	            if (connection != null)
	    	            {
	    	            	mHandler.post(new Runnable() {
	    	            		public void run() {
	    	            			Log.d("entriesDeleted",  "RUN setListAdapter");
	    	            			setListAdapter();
	    	            		}
	    	            	});
	    	            }
	    	              
	                }

	                public void entriesUpdated(Collection<String> addresses) {
	                	Log.i("entriesUpdated",  "Entered: " + addresses);
	                	
	                    //not sure what this should do- read spec!
	                	//for now lets just reset it to unavailable and clear the status
	                	
                		
                		for (int i = 0; i < contacts.size(); i++)
			            {
			            	FavoriteItem fav = contacts.get(i);
			            	if (addresses.contains(fav.getJID()))
			            	{
			            		Log.d("entriesUpdated",  "Resetting...");
			            		fav.setPresence(R.drawable.red_balls);
			            		fav.setStatus("");
			            	}
			            }
	                	

	    	            if (connection != null)
	    	            {
	    	            	mHandler.post(new Runnable() {
	    	            		public void run() {
	    	            			Log.d("entriesUpdated",  "RUN setListAdapter");
	    	            			setListAdapter();
	    	            		}
	    	            	});
	    	            }
	    	            
	                }

	                //TODO: Note that the device identifier is NOT used here!
	                //This method updates the Presence only
	                public void presenceChanged(Presence presence) {

	                	Log.d("presenceChanged",  "Entered for user(without device): " + presence.getFrom().split("/")[0]);
	                	
	                    //find the existing Roster entry- with new presence 
	                	RosterEntry entry = roster.getEntry(presence.getFrom().split("/")[0]);
	                	//find the associated FavoriteItem and update the presence and status
	                	for (int i = 0; i < contacts.size(); i++)
			            {
			            	FavoriteItem fav= contacts.get(i);
			            	if (fav.getJID() == entry.getUser())
			            	{
			            		Log.d("presenceChanged",  "UserFound");
			            		fav.setPresence((presence.getType() == Presence.Type.available)?R.drawable.green_balls:R.drawable.red_balls);
			            		fav.setStatus(presence.getStatus());
			            		break;
			            	}
			            }
	                	
	                	//finally, update the UI
	    	            if (connection != null)
	    	            {
	    	            	mHandler.post(new Runnable() {
	    	            		public void run() {
	    	            			Log.d("presenceChanged",  "RUN setListAdapter");
		    	            		setListAdapter();
	    	            		}
	    	            	});
	    	            }
	    	            
	                } // end of presenceChanged
	                 
	             });

	            connection.addPacketListener(new PacketListener() {
	                public void processPacket(Packet paramPacket) {
	                    
	                    if (paramPacket instanceof Presence) {
	                        Presence presence = (Presence) paramPacket;
	                        String email = presence.getFrom();
	                        Log.i("processPacket","chat invite status changed by user: : "
	                                + email + " calling listner");
	                        Log.i("processPacket","presence: " + presence.getFrom()
	                                + "; type: " + presence.getType() + "; to: "
	                                + presence.getTo() + "; " + presence.toXML());
	                        //Roster roster = connection.getRoster();
	                        //for (RosterEntry rosterEntry : roster.getEntries()) {
	                        //    System.out.println("jid: " + rosterEntry.getUser()
	                        //            + "; type: " + rosterEntry.getType()
	                        //            + "; status: " + rosterEntry.getStatus());
	                        //}
	                        //System.out.println("\n\n\n");
	                        if (presence.getType().equals(Presence.Type.subscribe)) {
	                        	Log.i("processPacket","SUBSCRIBE");
	                        	
	                        	FavoriteItem item = new FavoriteItem(presence.getFrom().split("/")[0],R.drawable.ic_launcher, presence.getFrom().split("@")[0], "SUBSCRIBE NOTIFICATION",R.drawable.blue_balls);
	        		       	    contacts.add(item); 
	                        	//contacts.add(presence.getFrom().split("@")[0] + " : " + "SUBSCRIBE NOTIFICATION");
	                        	/*
	                            Presence newp = new Presence(Presence.Type.subscribed);
	                            newp.setMode(Presence.Mode.available);
	                            newp.setPriority(24);
	                            newp.setTo(presence.getFrom());
	                            connection.sendPacket(newp);
	                            Presence subscription = new Presence(
	                                    Presence.Type.subscribe);
	                            subscription.setTo(presence.getFrom());
	                            connection.sendPacket(subscription);
	                            */
	                        	if (connection != null)
		    	            	{
		    	            		mHandler.post(new Runnable() {
		    	            			public void run() {
		    	            				Log.d("processPacket",  "RUN Subscribe");
		    	            				setListAdapter();
		    	            			}
		    	            		});
		    	            	}

	                        } else if (presence.getType().equals(
	                                Presence.Type.subscribed)) {
	                        	Log.i("processPacket","SUBSCRIBED");
	                        	/*
	                            Presence newp = new Presence(Presence.Type.unsubscribed);
	                            newp.setMode(Presence.Mode.available);
	                            newp.setPriority(24);
	                            newp.setTo(presence.getFrom());
	                            connection.sendPacket(newp);
	                            */
	                        } else if (presence.getType().equals(
	                                Presence.Type.unsubscribe)) {
	                        	Log.i("processPacket","UNSUBSCRIBE");
	                        	/*
	                            Presence newp = new Presence(Presence.Type.unsubscribed);
	                            newp.setMode(Presence.Mode.available);
	                            newp.setPriority(24);
	                            newp.setTo(presence.getFrom());
	                            connection.sendPacket(newp);
	                            */
	                        } else if (presence.getType().equals(
	                                Presence.Type.unsubscribed)) {
	                        	Log.i("processPacket","UNSUBSCRIBED");
	                        	/*
	                            Presence newp = new Presence(Presence.Type.unsubscribed);
	                            newp.setMode(Presence.Mode.available);
	                            newp.setPriority(24);
	                            newp.setTo(presence.getFrom());
	                            connection.sendPacket(newp);
	                            */
	                        }
	                    }

	                }
	            }, new PacketFilter() {
	                public boolean accept(Packet packet) {
	                    if (packet instanceof Presence) {
	                        Presence presence = (Presence) packet;
	                        if (presence.getType().equals(Presence.Type.subscribed)
	                                || presence.getType().equals(
	                                        Presence.Type.subscribe)
	                                || presence.getType().equals(
	                                        Presence.Type.unsubscribed)
	                                || presence.getType().equals(
	                                        Presence.Type.unsubscribe)) {
	                            return true;
	                        }
	                    }
	                    return false;
	                }
	            }); //end of AddPacketListener
	            

	         } catch (XMPPException ex) {
	                Log.e("FavoritesActivity", "Failed to log in as "+  USERNAME);
	                Log.e("FavoritesActivity", ex.toString());
	                connection = null;    
	         } catch (Exception e) {
	              //all other exceptions
	        	   Log.e("FavoritesActivity", "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   connection = null;
	         }
	         dialog.dismiss();
	      }
	   }); // end of thread

	    t.start();
	    dialog.show();
	}
	
	
	private void setListAdapter() 
	{
        FavoritesListAdapter adapter = new FavoritesListAdapter(this,
                R.layout.listitem_favs2, contacts);
        
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listitem_favorites, contacts);
		listview.setAdapter(adapter);
		// React to user clicks on item
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				     public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
				                             long id) {

				        // We know the View is a TextView so we can cast it
				        //TextView clickedView = (TextView) view;
				        //Toast.makeText(MainActivity.this, "Item with id ["+id+"] - Position ["+position+"] - Planet ["+clickedView.getText()+"]", Toast.LENGTH_SHORT).show();
				        
				    	RosterEntry entry = (RosterEntry)entries.toArray()[position];
				        Intent newActivity = new Intent(FavoritesActivity.this, ChatActivity.class); 
				        newActivity.putExtra("RECIPIENT", entry.getUser());
			            startActivity(newActivity);
			            
				     }
				});
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		//TODO: DIRTYHACK: but works  for now
	    		//LEAVE until we check if token already exists!!!!!
	    		editor.putString("accessTokenToken", null);
	    		editor.putString("accessTokenSecret", null);
	    		editor.putString("accessTokenKey", null);
	    		editor.putBoolean("firstTimeRun", true);
				editor.apply();
				finish();
	    		startActivity(new Intent(getBaseContext(), SettingsPreferenceActivity.class));
	            
	    		return true;
	        case R.id.create_new:
	        	newContact();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	Editable editable = null;
	
	public void newContact()
	{
		
		
		// Set an EditText view to get user input 
		final EditText input = new EditText(FavoritesActivity.this);
		String message = "Enter a username to add:";
	    

		new AlertDialog.Builder(FavoritesActivity.this)
		    .setTitle("Add Favorite")
		    .setMessage(message)
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		             editable = input.getText(); 
		             // deal with the editable
		             //Toast.makeText(FavoritesActivity.this, editable.toString(), Toast.LENGTH_SHORT).show();
		             
		             if (connection != null)
		     		{
		            	try
		            	{
		            		String JID = editable.toString() + "@" + HOST;
		            		
		            		
		                    
		            		Roster roster = connection.getRoster();
		            		// jid: String, name: String, groups: String[]
		            		String[] groups = new String[1];
		     		    	roster.createEntry(JID, editable.toString(), groups);
		     		    	
		     		    	
		     		    	Presence newp = new Presence(Presence.Type.subscribed);
		                    //newp.setMode(Presence.Mode.available);
		                    newp.setPriority(24);
		                    newp.setTo(JID);
		                    FavoritesActivity.connection.sendPacket(newp);
		     		    	
		                	
		                    //Presence subscription = new Presence(
		                    //        Presence.Type.subscribe);
		                    //subscription.setTo(JID);
		                    //FavoritesActivity.connection.sendPacket(subscription);
		            	} catch (XMPPException xmppe) {
		            		Log.e("",xmppe.getMessage());
		            		xmppe.printStackTrace();
		            	}
		     		}
		         }
		    })
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		                // Do nothing.
		         }
		    }).show();
		
		
	}

}
