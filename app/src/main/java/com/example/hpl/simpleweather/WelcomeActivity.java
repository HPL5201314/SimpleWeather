package com.example.hpl.simpleweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.example.hpl.simpleweather.entity.MHttpEntity;
import com.example.hpl.simpleweather.entity.ResponseWrapper;
import com.example.hpl.simpleweather.entity.SendDataEntity;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


public class WelcomeActivity extends Activity {

	
	public static ResponseWrapper response;// 数据结构的对象
	public static final int succeed = 1;
	public static final int fail = 2;
	public static final int nonet = 3;
	public String normalDistrict;
	public String locationCity = "合肥";
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_activity);
		
		initData();
		
	}

	private void initData()
	{
		showProgressDialog();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendRequest();
			}
		}).start();
	}


	private void showProgressDialog()
	{

		pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage("正在获取数据...");
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.show();
	}
	
	private void sendRequest() {
		
		String getData = null;
		MHttpEntity mhe = null;
		try {
			SendDataEntity.setCity(normalDistrict);
			Log.e("TAG", normalDistrict + "==>>normalDistrict");
			mhe = MHttpEntity.sendHttpRequest(SendDataEntity.getData());
			if (mhe.getHentity() != null) {
				getData = EntityUtils.toString(mhe.getHentity());
				GsonBuilder gson = new GsonBuilder();//
				response = gson.create().fromJson(getData,
						ResponseWrapper.class);
				Log.i("TAG", response.getError() + "-->response.getError()");
				if (response.getError() == -3) {
					
					SendDataEntity.setCity(normalDistrict);
					mhe = MHttpEntity.sendHttpRequest(SendDataEntity.getData());
					if (mhe.getHentity() != null) {
						getData = EntityUtils.toString(mhe.getHentity());
						Log.i("weather_info", getData + "-->getData");
					}
					if (response.getError() == -3) {
						SendDataEntity.setCity(locationCity);
						
						mhe = MHttpEntity
								.sendHttpRequest(SendDataEntity.getData());
						if (mhe.getHentity() != null) {
							Log.e("TAG", mhe.getHentity() + "==>>mhe.getHentity()");
							getData = EntityUtils.toString(mhe.getHentity());
							
						}
					}
				}
				mhe.getMessage().obj = getData;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.sendMessage(mhe.getMessage());// 使用Handler对网络状态做处理
	}

	/**
	 * 对网络连接状态做处理
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if(pDialog != null){
				pDialog.dismiss();
			}
			if (msg != null)
				switch (msg.arg1) {
				case succeed:// 与服务器连接成功，则传递数据并跳转
					Intent intent = new Intent(WelcomeActivity.this,
							MainActivity.class);
					if (msg.obj != null)
						intent.putExtra("weather_data", (String) msg.obj);
					intent.putExtra("normal_city", locationCity);
					startActivity(intent);
					finish();
					break;
				case fail:// 与服务器连接失败，弹出错误提示Toast
					Toast.makeText(WelcomeActivity.this,
							getString(R.string.net_fail), Toast.LENGTH_SHORT)
							.show();
					 intent = new Intent(WelcomeActivity.this,
							MainActivity.class);
					 intent.putExtra("weather_data", (String) msg.obj);
					 startActivity(intent);
					 finish();
					break;
				}
		}
	};

	/**
	 * 拦截返回键
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


}
