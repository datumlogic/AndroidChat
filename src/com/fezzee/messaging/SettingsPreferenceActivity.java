package com.fezzee.messaging;


import java.util.Map;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.http.AccessToken;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
//import android.preference.EditTextPreference;
//import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
//import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
//import android.preference.PreferenceManager;

public class SettingsPreferenceActivity extends PreferenceActivity  {
	
	//private Twitter twitter;
	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	//TODO: create a CONSTANTS file
	private String CONSUMER_KEY = "mgfY6EtzU1JhpBZ0kbJIHw";                            //Constants.CONSUMER_KEY;
	private String CONSUMER_SECRET = "TjoGiQDbejvltPkqTFiRf0eo0E7fr7fNVY3qep1D0";      // Constants.CONSUMER_SECRET;
	private String CALLBACK_URL = "callback://fezzee";

	//private static final String PREFS_NAME = "TwitterLogin";
	
	
	SharedPreferences settings;
	SharedPreferences.Editor editor;
    
	@SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences);
        
        //NOW FIXED AND NOT REQUIRED
        //StrictMode.enableDefaults();
        
      //get consumer and provider on the Application service
		getConsumerProvider();
        
		Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			Preference btnTwitter = (Preference)findPreference("btnTwitter");
			Preference btnExit = (Preference)findPreference("btnExit");
			//TODO: PreferenceManager.getDefaultSharedPreferences persists values auto-magically
			//You must do this implicitly if you define your own PREF_NAME
			//settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
			settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			//get the preferences editor
			 editor = settings.edit();
			//check for saved login details..
			checkForSavedLogin();
			
        
			btnTwitter.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	   
				@Override
				public boolean onPreferenceClick(Preference arg0) { 
					//code for what you want it to do
					Log.d("SettingsPreferenceActivity","Twitter Pressed");
        	   
					askOAuth();
        	        
					// avoid for next run
					//editor.putBoolean("firstTimeRun", false);
					//editor.commit();
					return true;
					}
			    });
			
			//temp solution
			btnExit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
	        	   
				@Override
				public boolean onPreferenceClick(Preference arg0) { 
					//code for what you want it to do
					Log.d("SettingsPreferenceActivity","Exit Pressed");
        	   
					finish();
					editor.putBoolean("firstTimeRun", false);
					editor.apply();
					startFirstActivity();

					return true;
					}
			    });
	    }
		});
		t.start();
        
    }
	
	
	
	//this wrapper may no longer be necessary
	private void checkForSavedLogin() {
		Log.d("checkForSavedLogin","Starting Check For Saved Login");
		if (getAccessToken()!=null) 	//if there are credentials stored then close this activity
		{
			Log.d("checkForSavedLogin","Token is Stored; Closing Activity");
			
			startFirstActivity();
			finish();
		}
	}
	
	/**
	 * This method checks the shared prefs to see if we have persisted a user token/secret
	 * if it has then it logs on using them, otherwise return null
	 * 
	 * @return AccessToken from persisted prefs
	 */
	private AccessToken getAccessToken() {
		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		String token = settings.getString("accessTokenToken", "");
		String tokenSecret = settings.getString("accessTokenSecret", "");
		if (token!=null && tokenSecret!=null && !"".equals(tokenSecret) && !"".equals(token)){
			return new AccessToken(token, tokenSecret);
		}
		return null;
	}
	
	/**
	 * Open the browser and asks the user to authorize the app.
	 * Afterwards, we redirect the user back here!
	 */
	private void askOAuth() {
		
		Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
		    	  try {
		    		  consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		    		  provider = new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token", "https://api.twitter.com/oauth/authorize");
		    		  String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
			
			
		    		  //If you  don't set this the registration won't be successful
		    		  setConsumerProvider();
		    		 
		    		  //Creates the WebUI for the callback!
		    		  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
		    		  finish();
			
		    	  } catch (Exception e) {
		    		  Log.e("askOAuth exception",e.getMessage() + " : " + e.getStackTrace()[0].toString());
		    		  //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		    	  }
	       }});
		   t.start();
		    
	}
	
	/**
	 * This method persists the Access Token information so that a user
	 * is not required to re-login every time the app is used
	 * Need to store it to web service first  and only if successful do we save it local WITH the ID of the web service
	 * @param a - the access token
	 */
	private String storeAccessToken(AccessToken a) {
		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		//SharedPreferences.Editor editor = settings.edit();
        
		//TODO: The keys names should have CONSTANTS
		editor.putString("accessTokenToken", a.getToken());
		editor.putString("accessTokenSecret", a.getTokenSecret());
		editor.commit();
		Log.d("storeAccessToken","Access Token: "+ a.getToken());
		Log.d("storeAccessToken","Access Token Secret: "+ a.getTokenSecret());
		String rtn = sendAccessToken(a);
		editor.putString("accessTokenKey", rtn);
		Log.d("sendAccessToken","Access Token Key: " + rtn);
		editor.apply();
		Log.d("storeAccessToken","storeAccessToken Complete");
		//for debug only
		printAllPrefs();
		return rtn;
	}
	
	/**
	 * Kick off the activity to display 
	 */
	private void startFirstActivity() {
		Log.d("startFirstActivity","STARTING FIRST ACTIVITY!");
		Intent i = new Intent(this, FavoritesActivity.class);
		startActivityForResult(i,0);
	}
	
	
	/*
	 * Send access token to Server
	 * TODO: This webservice SHOULD check to see if this token alread exists (prev install on other device or uninstall...)
	 * maybe return all 'indetities' (phone numbers, email addresses) with the Key
	 */
	private String sendAccessToken(AccessToken a){
		
			Log.d("sendAccessToken","Start Webservice call");
			RestClient client = new RestClient("http://ec2-54-201-47-27.us-west-2.compute.amazonaws.com:8080/users");
			client.AddParam("phone", "07525-366-960");
			client.AddParam("token", a.getToken());
			client.AddParam("secret", a.getTokenSecret());
			client.AddHeader("GData-Version", "2");

			try {
				client.Execute(1);//POST
			} catch (Exception e) {
				Log.e("sendAccessToken","Error : " + e.getMessage());
				e.printStackTrace();
			}

			String response = client.getResponse();
			return response;
	}
	
	/*
	 * Prints all prefs to the log at debug level
	 */
	private void printAllPrefs()
	{
		
		Map<String,?> keys = settings.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
		            Log.d("map values",entry.getKey() + ": " + 
		                                   entry.getValue().toString());            
		 }
	
	}
	
	
	//array is non primitive type so can be final (?)
	final int[] rtn = new int[1];
	/**
	 * As soon as the user successfully authorized the app, we are notified
	 * here. Now we need to get the verifier from the callback URL, retrieve
	 * token and token_secret and feed them to twitter4j (as well as
	 * consumer key and secret).
	 */
	@Override
	protected void onResume() {
		super.onResume();
		//Log.i("OnResume","Starting OnResume: " + this.getIntent() + " -- " + this.getIntent().getData() );
		if (this.getIntent()!=null && this.getIntent().getData()!=null){
			Log.d("OnResume","Intent not null");
			Uri uri = this.getIntent().getData();
			Log.d("OnResume","URI: "+ uri.toString());
			if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
				final String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
				Log.d("OnResume","Verifier: " + verifier);
								
				Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
				try {
					
					//put finish at the start of the callback so you dont see the settings dialog before the Favorites
					finish();
					
					Log.d("OnResume","Trying to retrieve access token: " + consumer + " : " + provider);
					// this will populate token and token_secret in consumer
					provider.retrieveAccessToken(consumer, verifier);
					
					Log.d("OnResume","Access token retrieved");
					// Get Access Token and persist it
					AccessToken a = new AccessToken(consumer.getToken(), consumer.getTokenSecret());
					
					//de-"stringify"- remove quotes and empty spaces
					String key = storeAccessToken(a).replace("\"", "").trim();
					Log.d("OnResume","Stored value key: '" +  key + "'");
					if (!key.equals(""))
						rtn[0] = Integer.parseInt(key);
					
					editor.putBoolean("firstTimeRun", false);
					editor.apply();
					

					Log.d("onResume","Token retrieved and stored successfully: " + rtn[0]);
					
					showToast("Token retrieved and stored successfully: " + rtn[0]);
					
					startFirstActivity();
					
					

				} catch (Exception e) {
					//Log.i("ERROR", e.getMessage());
					Log.e("onResume", "ERROR: " + e.getMessage());
					e.printStackTrace();
					
				}
		        }});
				t.start();
			}
		}
	}
	
	
	//display Toast from any thread
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(SettingsPreferenceActivity.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	/**
	 * Set the consumer and provider from the application service (in the case that the
	 * activity is restarted so the objects are not lost)
	 * 
	 * in order for the cast to work, you need to add:  android:name="com.fezzee.messaging.TwitterApplication"
	 * to the application object in AndroidManifest.xml
	 */
	private void setConsumerProvider() {
		if (provider!=null){
			((TwitterApplication)getApplication()).setProvider(provider);
		}
		if (consumer!=null){
			((TwitterApplication)getApplication()).setConsumer(consumer);
		}
	}

	/**
	 * Get the consumer and provider from the application service (in the case that the
	 * activity is restarted so the objects are not lost
	 * 
	 * in order for the cast to work, you need to add:  android:name="com.fezzee.messaging.TwitterApplication"
	 * to the application object in AndroidManifest.xml
	 */
	private void getConsumerProvider() {
		OAuthProvider p = ((TwitterApplication)getApplication()).getProvider();
		if (p!=null){
			provider = p;
		}
		CommonsHttpOAuthConsumer c = ((TwitterApplication)getApplication()).getConsumer();
		if (c!=null){
			consumer = c;
		}
	}
	

}
