package com.aminhx.earthquakewarning;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class myHttpManager {
	

	public static boolean isOnline(Context context){
		// permission : ACCESS_NETWORK_STATE
		ConnectivityManager cm = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if(netinfo != null && netinfo.isConnected())
			return true;
		else
			return false;		
	}
	
	public static String getDataHttpClient(String uri){
		// permission : INTERNET
		AndroidHttpClient client = AndroidHttpClient.newInstance("AndroidAgent");
		HttpGet method = new HttpGet(uri);
		try {
			HttpResponse response = client.execute(method);
			int statuscode = response.getStatusLine().getStatusCode();
			if((statuscode/100) == 4){
				return "HttpClient ERROR " + statuscode;
			}
			else{
				String content = EntityUtils.toString(response.getEntity());
				client.close();
				return content;				
			}
		} catch (IOException e) {
			e.printStackTrace();
			client.close();
			return null;
		} 
		
	}
	
	public static String getDataHttpURLConnection(RequestPackage rp){
		// permission : INTERNET
		String uri = rp.getUri();
		if(rp.getMethod().equals("GET")) {
			uri += "?" + rp.getEncodedParams();
		}
		try {
			URL url = new URL(uri);
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(rp.getMethod());
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				return sb.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}


	
	public static class RequestPackage {
		private String uri;
		private String method = "GET";
		private Map<String, String> params;

		public RequestPackage() {
			this.uri = "";
			this.method = "GET";
			params = new HashMap<String, String>();
		}


		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

		public Map<String, String> getParams() {
			return params;
		}

		public void setParams(Map<String, String> params) {
			this.params = params;
		}

		public void setParameter(String key, String value) {
			params.put(key,value);
		}

		public String getEncodedParams() {
			StringBuilder sb = new StringBuilder();
			for(String key : params.keySet()) {
				try {
					String value = URLEncoder.encode(params.get(key), "UTF-8");
					if(sb.length() > 0) {
						sb.append('&');
					}
					sb.append(key + "=" + value);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
	}
	
}
