package com.fezzee.messaging;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class ChatActivity extends Activity {
	
    
    private ArrayList<String> messages = new ArrayList<String>();
    private Handler mHandler = new Handler();

    private String recipient;
    private EditText textMessage;
    private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			recipient  = extras.getString("RECIPIENT");
			setTitle("Mashing as " + FavoritesActivity.USERNAME.split("@")[0] + " with " + recipient.split("@")[0]);
		}
		
		//text1 = (TextView) this.findViewById(R.id.text1);
		//text1.setText(value);
		
		Log.i("XMPP Test","******Recipient: " + recipient );
	    textMessage = (EditText) this.findViewById(R.id.chatET);
	    listview = (ListView) this.findViewById(R.id.listMessages);
	    setListAdapter();

	    // Set a listener to send a chat text message
	    Button send = (Button) this.findViewById(R.id.sendBtn);
	    send.setOnClickListener(new View.OnClickListener() {
	      public void onClick(View view) {
	        //String to = recipient + "@" + HOST;
	        String text = textMessage.getText().toString();          
	        Log.i("XMPPChatDemoActivity ", "Sending text " + text + " to " + recipient);
	        Message msg = new Message(recipient, Message.Type.chat);  
	        msg.setBody(text);
	        if (FavoritesActivity.connection != null) {
	          //add checks here- should have something  to handle an empty recipient, but will do for now
	        	Log.i("XMPPChatDemoActivity ", "TO len: " + recipient.trim().length() + " HOST Len: " + FavoritesActivity.HOST.length());
	          if (text.trim().length() > 0 && recipient.trim().length() > FavoritesActivity.HOST.length() + 1 ) //+1 for @
	          {
	        	  FavoritesActivity.connection.sendPacket(msg);
	        	  //GM New
		          String name = FavoritesActivity.connection.getAccountManager().getAccountAttribute("name");
		          //messages.add(connection.getUser() + ":");
		          //messages.add(text);
		          messages.add(name + ": " + text);
		          setListAdapter();
		          //GM New
		          textMessage.setText("");
		          InputMethodManager imm = (InputMethodManager)getSystemService(
		        	      Context.INPUT_METHOD_SERVICE);
		        	imm.hideSoftInputFromWindow(textMessage.getWindowToken(), 0);
	          }
	        }
	      }
	    });
	    setConnection(FavoritesActivity.connection);
		
	}


	/**
	   * Called by Settings dialog when a connection is establised with 
	   * the XMPP server
	   */
	
	  public void setConnection(XMPPConnection connection) {
	    //this.connection = connection;
	    if (connection != null) {
	      // Add a packet listener to get messages sent to us
	      PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
	      connection.addPacketListener(new PacketListener() {
	        @Override
	        public void processPacket(Packet packet) {
	          Message message = (Message) packet;
	          if (message.getBody() != null) {
	        	
	            String fromName = StringUtils.parseBareAddress(message.getFrom());
	            
	            Log.i("processPacket", " Text Recieved " + message.getBody() + " from " +  fromName);
	           
	            messages.add(fromName.split("@")[0] + ": " + message.getBody());
	            //if the keyboard is showing, hide it
	            //InputMethodManager imm = (InputMethodManager)getSystemService(
		        //	      Context.INPUT_METHOD_SERVICE);
		        //	imm.hideSoftInputFromWindow(textMessage.getWindowToken(), 0);
	            // Add the incoming message to the list view
	            mHandler.post(new Runnable() {
	              public void run() {
	                setListAdapter();
	              }
	            });
	          }  else { // end of if (message.getBody()
		        	Presence presence = (Presence) packet;
		        	if (presence.getType() == Type.subscribe) {
		        		Log.d("processPacket", "presence subscription from " + presence.getFrom());
		        	} else {
		            	Log.d("processPacket", "presence type " + presence.getType());
			        }
		      }
	          
	        } // end of processPacket
	      }, filter);
	    }
	    else
	    {
	    	Log.e("ChatActivity:seyConnection","connection is null");
	    }
	  }
     
	
	  private void setListAdapter() {
	    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.chat_item_orig, messages);
	    ArrayAdapter<String> adapter = new CustomArrayAdapter(getApplicationContext(), R.layout.activity_chat, messages);
	    listview.setAdapter(adapter);
	  }

	  @Override
	  protected void onDestroy() {
	    super.onDestroy();
	    try {
	      //connection.disconnect();
	    } catch (Exception e) {

	    }
	  }



}
