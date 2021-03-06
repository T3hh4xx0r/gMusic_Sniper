package com.t3hh4xx0r.cloudsniper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.blinkenlights.jid3.ID3Exception;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.MediaFile;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ResultsAdapter extends BaseAdapter {
	ArrayList<SniperResults> resultsList;
	String str;
	URL artUrl;
	Bitmap newArt;
	boolean founded = false;
	
	 private LayoutInflater mInflater;
	 Context ctx;	 

	 public ResultsAdapter(Context context, ArrayList<SniperResults> results) {
	  resultsList = results;
	  mInflater = LayoutInflater.from(context);
	  ctx = context;	  
	 }
	 
	 public int getCount() {
	  return resultsList.size();
	 }

	 public Object getItem(int position) {
	  return resultsList.get(position);
	 }

	 public long getItemId(int position) {
	  return position;
	 }

	 public View getView(final int position, View convertView, ViewGroup parent) {
	  final ViewHolder holder;
	  if (convertView == null) {
		  convertView = mInflater.inflate(R.layout.list_item, null);
		  holder = new ViewHolder();
		  holder.title = (TextView) convertView.findViewById(R.id.title);
		  holder.artist = (TextView) convertView.findViewById(R.id.artist);
		  holder.album = (TextView) convertView.findViewById(R.id.album);
		  holder.art = (ImageView) convertView.findViewById(R.id.art);
		  holder.sponsor = (TextView) convertView.findViewById(R.id.sponsor);
		  convertView.setTag(holder);   	   	  
	  } else {
	   	  holder = (ViewHolder) convertView.getTag();
	  }

	  holder.title.setText(resultsList.get(position).getTitle());
	  holder.album.setText(resultsList.get(position).getAlbum());
	  holder.artist.setText(resultsList.get(position).getArtist());
	  if (!resultsList.get(position).getHasart()) {
		  holder.sponsor.setVisibility(View.VISIBLE);							
	  }
	  Bitmap scaled = Bitmap.createScaledBitmap(resultsList.get(position).getArt(), 150, 150, true);
	  holder.art.setImageBitmap(scaled);
	  convertView.setOnLongClickListener(new OnLongClickListener() {
      	public boolean onLongClick(View v) {
		  Intent i = new Intent(android.content.Intent.ACTION_VIEW); 
          Uri data = Uri.parse("file:///"+Constants.gMusicSniperDir+"/music/"+resultsList.get(position).getArtist()+"/"+resultsList.get(position).getAlbum()+"/"+resultsList.get(position).getTitle()+".mp3"); 
          i.setDataAndType(data,"audio/mp3"); 
          v.getContext().startActivity(i);
      	return false;	
      	}
      });
	  
      convertView.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) { 
  			String artist = resultsList.get(position).getArtist();
  			String album = resultsList.get(position).getAlbum();
  			String title = resultsList.get(position).getTitle();
  			new checkArt().execute(artist, album, title);
  		}
      });      
	  return convertView;
	 }

		public class checkArt extends AsyncTask<String, String, ArrayList<String>> {
			final ArrayList<String> infosList = new ArrayList<String>();
			ProgressDialog mProgressDialog;
			
			@Override
			protected void onPreExecute() {
				mProgressDialog = new ProgressDialog(ctx);
				mProgressDialog.setMessage("Checking last.FM");
				mProgressDialog.setIndeterminate(true);
				mProgressDialog.show();
				if (!infosList.isEmpty()) {
					infosList.clear();
				}
			}
			
		    @Override
			protected ArrayList<String> doInBackground(String... infos) {				
				int i;
	  			String req;
	  			founded = false;
				try {
				    req = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&artist=" + URLEncoder.encode(infos[0].toLowerCase().replaceAll(" ", ""), "UTF-8") + "&album=" + URLEncoder.encode(infos[1].toLowerCase().replaceAll(" ", ""), "UTF-8") + "&api_key=" + URLEncoder.encode("56c3989500c7a00666545eb9bed9ece9", "UTF-8");
					HttpResponse localHttpResponse = new DefaultHttpClient().execute(new HttpGet(req));
					XmlPullParser xpp = Xml.newPullParser();
					xpp.setInput(localHttpResponse.getEntity().getContent(), "UTF-8");
					XmlUtils.beginDocument(xpp,"lfm");  
					i = xpp.getEventType();
					  do{  
					    XmlUtils.nextElement(xpp);  
					    xpp.next();  
					    i = xpp.getEventType();
					    try {
					    	str = new String(xpp.getText());
					    } catch (Exception e) {
					    }
						if(i == XmlPullParser.TEXT && (str.endsWith(".png")||str.endsWith(".jpg")||str.endsWith(".gif"))){  
								artUrl = new URL(str);
								founded = true;
						}
					  } while (i != XmlPullParser.END_DOCUMENT) ;      
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}

				if (founded) {
					for (int j=0;j<infos.length;j++) {
						infosList.add(infos[j]);
					}
					infosList.add(artUrl.toString());
					return infosList;
				} else {
					return null;
				}
		    }
		    
		    @Override
		    protected void onPostExecute(ArrayList<String> aL) {
		    	if (aL == null) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			        builder.setTitle("No artwork found!");
				    builder.setMessage("Last.FM was unable to find any matching artwork for your selection.")
			  		   .setCancelable(false)
			  		   .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			  		       public void onClick(DialogInterface dialog, int id) {
				  				dialog.dismiss();
						    	mProgressDialog.dismiss();	            	    		   

			  		       }
			  		   });
			  		AlertDialog alert = builder.create();
			  		alert.show();
		    	} else {
		           	LayoutInflater factory = LayoutInflater.from(ctx);            
	            	final View resultsPopup = factory.inflate(R.layout.results_popup, null);
	                final ImageView artwork = (ImageView)resultsPopup.findViewById(R.id.artwork); 
	                URL u;
					try {
						u = new URL(infosList.get(3));
						newArt = BitmapFactory.decodeStream(u.openConnection().getInputStream());
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		artwork.setImageBitmap(newArt);
	        		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
	                builder.setView(resultsPopup); 
	          		builder.setTitle("Possible artwork match!"); 
	          		builder.setMessage("The artwork below was pulled from Last.FM. If this is the correct artwork, select \"Embed\" below to embed it in the mp3.")
	          		   .setCancelable(false)
	          		   .setPositiveButton("Embed", new DialogInterface.OnClickListener() {
	          		       public void onClick(DialogInterface dialog, int id) {
	       			    	   mProgressDialog.dismiss();	            	    		   
	          		    	   try {
	             		    	   //holder.art.setImageBitmap(Bitmap.createScaledBitmap(newArt, 150, 150, true));
	              		    	   File mp3 = new File(Constants.gMusicSniperDir+"/music/"+infosList.get(0)+"/"+infosList.get(1)+"/"+infosList.get(2)+".mp3");
	              		    	   MediaFile oMediaFile = new MP3File(mp3);
	              		    	   ID3V2_3_0Tag tag = new ID3V2_3_0Tag();
	              		    	   ByteArrayOutputStream stream = new ByteArrayOutputStream();
	              		    	   newArt.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	              		    	   byte[] b = stream.toByteArray();
	          		    		   APICID3V2Frame newFrontCover = new APICID3V2Frame("image/jpeg",APICID3V2Frame.PictureType.FrontCover,"Album Cover",b);
	          		    		   tag.addAPICFrame(newFrontCover);
	          		    		   tag.setAlbum(infosList.get(0));
	          		    		   tag.setArtist(infosList.get(1));
	          		    		   tag.setTitle(infosList.get(2));
	          		    		   oMediaFile.setID3Tag(tag);
	          		    		   oMediaFile.sync();
	          		    	   } catch (ID3Exception e) {
	          		    		   e.printStackTrace();
	          		    	   }
	   						}
	          		   })
	          		   .setNegativeButton("Nope! Wrong artwork.", new DialogInterface.OnClickListener() {
	          		       public void onClick(DialogInterface dialog, int id) {
	          		    	    mProgressDialog.dismiss();	            	    		   
	       			    		dialog.cancel();
	          		       }
	          		   });
	          		AlertDialog alert = builder.create();
	          		alert.show();
		    	}
		    }
	}		

	static class ViewHolder {
	  TextView artist;
	  TextView title;
	  TextView album;
	  ImageView art;
	  TextView sponsor;
	 }
	}