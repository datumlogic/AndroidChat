package com.fezzee.messaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

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
import android.widget.ImageButton;
import android.widget.ListView;


public class ChatActivity extends Activity {
	
    
    private ArrayList<String> messages = new ArrayList<String>();
    private Handler mHandler = new Handler();

    private String recipient;
    private EditText textMessage;
    private ListView listview;
    
    private OutgoingFileTransfer transfer= null;
    private SmackAndroid asmk = null;
    
    private OutputStreamWriter osw;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		
		asmk = SmackAndroid.init(this);
		
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
	    ImageButton upload = (ImageButton) this.findViewById(R.id.uploadBtn);
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
	    
	    upload.setOnClickListener(new View.OnClickListener() {
		      public void onClick(View view) {
		        Log.e("ChatAtivity::upload-onClick","Entered");
		        

		        
		         
		        //If you want to save a static file in your application at compile time, 
		        //save the file in your project res/raw/ directory. You can open it with 
		        //openRawResource(), passing the R.raw.<filename> resource ID. This method 
		        //returns an InputStream that you can use to read the file (but you cannot 
		        //write to the original file).
		        //InputStream ins = getResources().openRawResource(R.raw.test);
		        
		     // Send the file
		        try{
		           // Create the file transfer manager
		           if (FavoritesActivity.connection==null)
		           {
		        	   Log.e("ChatAtivity::upload-onClick","[FavoritesActivity.connection = null]");
		        	   return;
		           }
		           
		           InputStream input = getResources().openRawResource(R.raw.test);
		           File file = null;
		           try {
		        	    file = new File(getCacheDir(), "test.txt");
		        	    final OutputStream output = new FileOutputStream(file);
		        	    try {
		        	        try {
		        	            final byte[] buffer = new byte[1024];
		        	            int read;

		        	            while ((read = input.read(buffer)) != -1)
		        	                output.write(buffer, 0, read);

		        	            output.flush();
		        	        } finally {
		        	            output.close();
		        	        }
		        	    } catch (Exception e) {
		        	        e.printStackTrace();
		        	    }
		        	} finally {
		        	    input.close();
		        	}
		           
		           
		           FileTransferManager manager = new FileTransferManager(FavoritesActivity.connection);
		  		
		           // Create the outgoing file transfer
		           transfer = manager.createOutgoingFileTransfer("gene@ec2-54-201-47-27.us-west-2.compute.amazonaws.com/Genes-MacBookPro");
		           
		        	transfer.sendFile(file, "Test_File_Transfer");
                    //Thread.sleep(25000);
		        	
		        	
		        } catch (Exception e) {
		        	
		        	Log.e("ChatAtivity::upload-onClick","[Exception] " + e.getMessage());
		        	e.printStackTrace();
		        	return;
		        }
		        
		        Log.e("ChatAtivity::upload-onClick","Complete");
		        while(!transfer.isDone()) {
		        	   if(transfer.getStatus().equals(Status.error)) {
		        	      System.out.println("ERROR!!! " + transfer.getError());
		        	   } else if (transfer.getStatus().equals(Status.cancelled)
		        	                    || transfer.getStatus().equals(Status.refused)) {
		        	      System.out.println("Cancelled!!! " + transfer.getError());
		        	   }
		        	   try {
		        	      Thread.sleep(1000L);
		        	   } catch (InterruptedException e) {
		        	      e.printStackTrace();
		        	   }
		        	}
		        	if(transfer.getStatus().equals(Status.refused) || transfer.getStatus().equals(Status.error)
		        	 || transfer.getStatus().equals(Status.cancelled)){
		        	   System.out.println("refused cancelled error " + transfer.getError());
		        	} else {
		        	   System.out.println("Success");
		        	}

		
		      }
	    
		    });
	    
	    setConnection(FavoritesActivity.connection);
		
	}
	

	
	@Override
  public void onPause() {
      super.onPause();
      Log.d("PublishActivity::onPause","Entered");
      
      //listview.setOnItemClickListener(null);


  }

  @Override
  public void onResume() {
      super.onResume();
      Log.d("PublishActivity::onResume","Entered");
          //mHandler = new Handler();
          //if(listview != null){
             // listview.setOnItemClickListener(exampleListener);
        //	  setListAdapter();
          // }
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
	    //connection.disconnect(); 
	  }  //end of onDestroy
	  




}
