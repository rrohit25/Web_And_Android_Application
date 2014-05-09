package com.example.weatherapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.facebook.*;
import com.facebook.model.*;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;

public class MainActivity extends Activity {

	static String url1 = "http://cs-server.usc.edu:38675/examples/myServletWeather?location=";
	static String url2="&tempUnit=";
	String url;//="http://cs-server.usc.edu:38675/examples/myServletWeather?location=90007&tempUnit=c";
	static String link = "";
	String[] day=new String[5];
	String[] weather=new String[5];
	String[] high=new String[5];
	String[] low=new String[5];
	String city="";
	String state="";
	String country="";
	String currentText="";
	String currentTemp="";
	String units="";
	Bitmap bmp;
	String feed;
	String weatehrimage;
	JSONObject jsonObject;
	String tempSym="";
	String loca="";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		try {
	        PackageInfo info = getPackageManager().getPackageInfo(
	                "com.example.weatherapp", 
	                PackageManager.GET_SIGNATURES);
	        for (Signature signature : info.signatures) {
	            MessageDigest md = MessageDigest.getInstance("SHA");
	            md.update(signature.toByteArray());
	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	            }
	    } catch (NameNotFoundException e) {

	    } catch (NoSuchAlgorithmException e) {

	    }
		Button button= (Button) findViewById(R.id.searchButton);
		button.setOnClickListener(new OnClickListener() {           

			  @Override
			  public void onClick(View v) 
			  {
				  int parse=0;
				  EditText edit = (EditText) findViewById(R.id.locationText);
					loca = edit.getText().toString();
					RadioButton radio1 = (RadioButton) findViewById(R.id.faranhiteRadio);
					RadioButton radio2 = (RadioButton) findViewById(R.id.degreeRadio);
					if (radio1.isChecked())	tempSym = "f";
					if (radio2.isChecked())	tempSym = "c";
					if (loca.length() == 0) 
					{
						Toast.makeText(getApplicationContext(),"Please enter a Zipcode or location",Toast.LENGTH_LONG).show();
						return;
					} 
					else 
					{
						try {
							Integer.parseInt(loca);
							if (loca.length() != 5) 
							{
								Toast.makeText(getApplicationContext(),"Invalid zipcode: must be five digits",Toast.LENGTH_LONG).show();
								return;

							} 
							else 
							{
								parse = 1;
							}
							} catch (NumberFormatException e) {
								String[] words = loca.split(",");
								if (words.length < 2) {
									Toast.makeText(getApplicationContext(),"Invalid location: must include state or country ",Toast.LENGTH_LONG).show();
									return;
								} 
								else 
								{
								parse = 1;
								}
						}
					}
					if(parse==1)	
					{
						new MyAsyncTask().execute();
					}
			  }    
			});
		

		
	}
	public void onClick(View view){
		EditText edit = (EditText) findViewById(R.id.locationText);
		loca = edit.getText().toString();
		
		
		RadioButton radio1 = (RadioButton) findViewById(R.id.faranhiteRadio);
		RadioButton radio2 = (RadioButton) findViewById(R.id.degreeRadio);
		if (radio1.isChecked())	tempSym = "f";
		if (radio2.isChecked())	tempSym = "c";
		new MyAsyncTask().execute();
	}
	public void getSession(final int flag) {

		
		// start Facebook Login
    Session.openActiveSession(this, true, new Session.StatusCallback() {

      // callback when session changes state
      @Override
      public void call(Session session, SessionState state, Exception exception) {
        if (session.isOpened()) {

          // make request to the /me API
          Request.newMeRequest(session, new Request.GraphUserCallback() {

            // callback after Graph API response with user object
            @Override
            public void onCompleted(GraphUser user, Response response) {
              if (user != null) {
                TextView welcome = (TextView) findViewById(R.id.welcome);
                //welcome.setText("Hello " + user.getName() + "!");
                if(0 == flag) {
					publishCurrWeatherFeed();
				}else{
					publishforecast();
				}	
			}else {
				System.out.println("ERROR: NO USER FOUND !!!!");
			}
            }
          }).executeAsync();
        }
      }
    });
  }
	

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
  }
		
	
	   
	   private void publishCurrWeatherFeed() {
		    Bundle params = new Bundle();

		    try {
		    	String name = city + ", " +state + ", " + country;
		    	String cap = "The current condition for " + city + " is " + currentTemp + ".";
		    	String des="";
		    	if(tempSym=="f")
		    		des = "Temperature is " + currentText  +" \u2109" + ".";
		    	else
		    		des = "Temperature is " + currentText  +" \u2103" + ".";
				params.putString("name", name);		
				params.putString("caption", cap);
				params.putString("description", des);
				params.putString("link", feed);
			    params.putString("picture", weatehrimage);

				
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          
		    WebDialog feedDialog = (
		        new WebDialog.FeedDialogBuilder(MainActivity.this, Session.getActiveSession(),params))
		        .setOnCompleteListener(new OnCompleteListener() {
		            public void onComplete(Bundle values, FacebookException error) {
		                if (error == null) {
		                    // When the story is posted, echo the success
		                    // and the post Id.
		                    final String postId = values.getString("post_id");
		                    if (postId != null) {
		                        Toast.makeText(MainActivity.this,
		                            "Posted story, id: "+postId,
		                            Toast.LENGTH_SHORT).show();
		                    } else {
		                        // User clicked the Cancel button
		                        Toast.makeText(MainActivity.this.getApplicationContext(), 
		                            "Publish cancelled", 
		                            Toast.LENGTH_SHORT).show();
		                    }
		                } else if (error instanceof FacebookOperationCanceledException) {
		                    // User clicked the "x" button
		                    Toast.makeText(MainActivity.this.getApplicationContext(), 
		                        "Publish cancelled", 
		                        Toast.LENGTH_SHORT).show();
		                } else {
		                    // Generic, ex: network error
		                    Toast.makeText(MainActivity.this.getApplicationContext(), 
		                        "Error posting story", 
		                        Toast.LENGTH_SHORT).show();
		                }
		            }

		        })
		        .build();
		    feedDialog.show();
		}
	   
	   private void publishforecast() {
		    Bundle params = new Bundle();

		    try {
		    	String name = city + ", " +state + ", " + country;
		    	String cap = "Weather forecast for "+city;
		    	String des;
		    	if(tempSym=="f")
		    		des = day[0]+": "+weather[0]+", "+high[0]+"/"+low[0]+" \u2109" +";"+day[1]+": "+weather[1]+", "+high[1]+"/"+low[1]+" \u2109" +";"+day[2]+": "+weather[2]+", "+high[2]+"/"+low[2]+" \u2109" +";"+day[3]+": "+weather[3]+", "+high[3]+"/"+low[3]+" \u2109" +";"+day[4]+": "+weather[4]+", "+high[4]+"/"+low[4]+" \u2109" +".";
		    	else
		    		des = day[0]+": "+weather[0]+", "+high[0]+"/"+low[0]+" \u2103" +";"+day[1]+": "+weather[1]+", "+high[1]+"/"+low[1]+" \u2103" +";"+day[2]+": "+weather[2]+", "+high[2]+"/"+low[2]+" \u2103" +";"+day[3]+": "+weather[3]+", "+high[3]+"/"+low[3]+" \u2103" +";"+day[4]+": "+weather[4]+", "+high[4]+"/"+low[4]+" \u2103" +".";
		    	
		    	String picture = "http://www-scf.usc.edu/~csci571/2013Fall/hw8/weather.jpg";
				params.putString("name", name);		
				params.putString("caption", cap);
				params.putString("description", des);
				params.putString("link", feed);
			    params.putString("picture", picture);

				
		    } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	          
		    WebDialog feedDialog = (
		        new WebDialog.FeedDialogBuilder(MainActivity.this, Session.getActiveSession(),params))
		        .setOnCompleteListener(new OnCompleteListener() {
		            public void onComplete(Bundle values, FacebookException error) {
		                if (error == null) {
		                    // When the story is posted, echo the success
		                    // and the post Id.
		                    final String postId = values.getString("post_id");
		                    if (postId != null) {
		                        Toast.makeText(MainActivity.this,
		                            "Posted story, id: "+postId,
		                            Toast.LENGTH_SHORT).show();
		                    } else {
		                        // User clicked the Cancel button
		                        Toast.makeText(MainActivity.this.getApplicationContext(), 
		                            "Publish cancelled", 
		                            Toast.LENGTH_SHORT).show();
		                    }
		                } else if (error instanceof FacebookOperationCanceledException) {
		                    // User clicked the "x" button
		                    Toast.makeText(MainActivity.this.getApplicationContext(), 
		                        "Publish cancelled", 
		                        Toast.LENGTH_SHORT).show();
		                } else {
		                    // Generic, ex: network error
		                    Toast.makeText(MainActivity.this.getApplicationContext(), 
		                        "Error posting story", 
		                        Toast.LENGTH_SHORT).show();
		                }
		            }

		        })
		        .build();
		    feedDialog.show();
		}

	   
	   
	private class MyAsyncTask extends AsyncTask<String, Void, String> {
		
		protected String doInBackground(String... arg0) {

	
			DefaultHttpClient httpclient = new DefaultHttpClient();
			try {
				loca=URLEncoder.encode(loca, "utf-8");
				loca.replace("'"," ");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			url="http://cs-server.usc.edu:38675/examples/myServletWeather?location="+loca+"&tempUnit="+tempSym;
			HttpGet httpget = new HttpGet(url);
			//httpget.setHeader("Content-type", "application/json");
			InputStream inputStream = null;
			String result = null;

			try {
				HttpResponse response = httpclient.execute(httpget);        
				HttpEntity entity = response.getEntity();
				inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder theStringBuilder = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					theStringBuilder.append(line + "\n");
				}
				result = theStringBuilder.toString();

			} catch (Exception e) { 
				e.printStackTrace();
			}
			finally {
				try{if(inputStream != null)inputStream.close();}
				catch(Exception e){}
			}
			
			try {
				jsonObject = new JSONObject(result);
				link = jsonObject.getJSONObject("weather").getString("link");
				JSONObject temp= jsonObject.getJSONObject("weather");
				JSONArray forecast = temp.getJSONArray("forecast");
				
				city= temp.getJSONObject("location").getString("city");
				state=jsonObject.getJSONObject("weather").getJSONObject("location").getString("region");
				country=jsonObject.getJSONObject("weather").getJSONObject("location").getString("country");
				currentText=jsonObject.getJSONObject("weather").getJSONObject("condition").getString("temp");
				currentTemp=jsonObject.getJSONObject("weather").getJSONObject("condition").getString("text");
				for (int i = 0; i < forecast.length(); i++) {
				    JSONObject row = forecast.getJSONObject(i);
				    day[i] = row.getString("day");
				    weather[i] = row.getString("text");
				    high[i] = row.getString("high");
				    low[i] = row.getString("low");
				}
				weatehrimage=jsonObject.getJSONObject("weather").getString("img");
				feed=jsonObject.getJSONObject("weather").getString("feed");
				units=jsonObject.getJSONObject("weather").getJSONObject("units").getString("temprature");
				URL url = new URL(weatehrimage);
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return result;

		}
		


		protected void onPostExecute(String result){
			ImageView imageView=(ImageView) findViewById(R.id.weatherImg);
			imageView.setImageBitmap(bmp);
			TextView cuntry =(TextView) findViewById(R.id.city);
			cuntry.setText(city);
			TextView st =(TextView) findViewById(R.id.stateCountry);
			st.setText(state+", "+country);
			TextView curtxt =(TextView) findViewById(R.id.currentTemprature);
			curtxt.setText(currentTemp);
			TextView curtmp =(TextView) findViewById(R.id.currentWeather);
			if(tempSym=="f")
				curtmp.setText(currentText+"\u2109");
			else
				curtmp.setText(currentText+"\u2103");
			
			//forecast table
			//all days
			TextView day1 =(TextView) findViewById(R.id.day1TextView);
			day1.setText(day[0]);
			TextView day2 =(TextView) findViewById(R.id.day2TextView);
			day2.setText(day[1]);
			TextView day3 =(TextView) findViewById(R.id.day3TextView);
			day3.setText(day[2]);
			TextView day4 =(TextView) findViewById(R.id.day4TextView);
			day4.setText(day[3]);
			TextView day5 =(TextView) findViewById(R.id.day5TextView);
			day5.setText(day[4]);
			
			//weather
			TextView weather1 =(TextView) findViewById(R.id.weather1TextView);
			weather1.setText(weather[0]);
			TextView weather2 =(TextView) findViewById(R.id.weather2TextView);
			weather2.setText(weather[1]);
			TextView weather3 =(TextView) findViewById(R.id.weather3TextView);
			weather3.setText(weather[2]);
			TextView weather4 =(TextView) findViewById(R.id.weather4TextView);
			weather4.setText(weather[3]);
			TextView weather5 =(TextView) findViewById(R.id.weather5TextView);
			weather5.setText(weather[4]);
			
			//high
			TextView high1 =(TextView) findViewById(R.id.high1TextView);
			if(tempSym=="f")
				high1.setText(high[0]+"\u2109");
			else
				high1.setText(high[0]+"\u2103");
			TextView high2 =(TextView) findViewById(R.id.high2TextView);
			if(tempSym=="f")
				high2.setText(high[1]+"\u2109");
			else
				high2.setText(high[1]+"\u2103");
			TextView high3 =(TextView) findViewById(R.id.high3TextView);
			if(tempSym=="f")
				high3.setText(high[2]+"\u2109");
			else
				high3.setText(high[2]+"\u2103");
			TextView high4 =(TextView) findViewById(R.id.high4TextView);
			if(tempSym=="f")
				high4.setText(high[3]+"\u2109");
			else
				high4.setText(high[3]+"\u2103");
			TextView high5 =(TextView) findViewById(R.id.high5TextView);
			if(tempSym=="f")
				high5.setText(high[4]+"\u2109");
			else
				high5.setText(high[4]+"\u2103");
			
			//low
			TextView low1 =(TextView) findViewById(R.id.low1TextView);
			if(tempSym=="f")
				low1.setText(low[0]+"\u2109");
			else
				low1.setText(low[0]+"\u2103");
			TextView low2 =(TextView) findViewById(R.id.low2TextView);
			if(tempSym=="f")
				low2.setText(low[1]+"\u2109");
			else
				low2.setText(low[1]+"\u2103");
			TextView low3 =(TextView) findViewById(R.id.low3TextView);
			if(tempSym=="f")
				low3.setText(low[2]+"\u2109");
			else
				low3.setText(low[2]+"\u2103");
			TextView low4 =(TextView) findViewById(R.id.low4TextView);
			if(tempSym=="f")
				low4.setText(low[3]+"\u2109");
			else
				low4.setText(low[3]+"\u2103");
			TextView low5 =(TextView) findViewById(R.id.low5TextView);
			if(tempSym=="f")
				low5.setText(low[4]+"\u2109");
			else
				low5.setText(low[4]+"\u2103");
						
			TextView frc =(TextView) findViewById(R.id.caption);
			frc.setVisibility(View.VISIBLE);
			
			TableLayout tl = (TableLayout)findViewById(R.id.forecastTable);

			tl.setVisibility(View.VISIBLE);
			
			TextView weatherForecastPost =(TextView) findViewById(R.id.shareCurrentWeather);
			weatherForecastPost.setOnClickListener(new OnClickListener() {   
				
				public void onClick(View v) {
					final Dialog customDialog = new Dialog(MainActivity.this);
					customDialog.setContentView(R.layout.postfb);
					Button postFbtn = (Button)customDialog.findViewById(R.id.shareCurrentWeatherButton);
					Button canbtn = (Button)customDialog.findViewById(R.id.fbCancelButton);
					customDialog.setTitle("Post to Facebook");
					postFbtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							getSession(0);
							customDialog.dismiss();

						}

					});
					canbtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							customDialog.dismiss();

						}
					});
					customDialog.show();

				}
			});
			
			
			TextView weatherForecastPost1 =(TextView) findViewById(R.id.shareWeatherForecast);
			weatherForecastPost1.setOnClickListener(new OnClickListener() {   
				
				public void onClick(View v) {
					final Dialog customDialog1 = new Dialog(MainActivity.this);
					customDialog1.setContentView(R.layout.fbpost2);
					Button postFbtn = (Button)customDialog1.findViewById(R.id.shareWeatherForecastButton);
					Button canbtn = (Button)customDialog1.findViewById(R.id.fbCancelButton);
					customDialog1.setTitle("Post to Facebook");
					postFbtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							getSession(1);
							customDialog1.dismiss();

						}

					});
					canbtn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// TODO Auto-generated method stub
							customDialog1.dismiss();

						}
					});
					customDialog1.show();

				}
			});
			//fbLink.addView(fbcastView);


		}
		
	}

}
