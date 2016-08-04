package com.example.xiaoliweather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiaoliweather.R;
import com.example.xiaoliweather.model.City;
import com.example.xiaoliweather.model.County;
import com.example.xiaoliweather.model.Province;
import com.example.xiaoliweather.model.XiaoliWeatherDB;
import com.example.xiaoliweather.util.HttpCallbackListener;
import com.example.xiaoliweather.util.HttpUtil;
import com.example.xiaoliweather.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private XiaoliWeatherDB xiaoliweatherDB;
	private List<String> dataList =new ArrayList<String>();
//	省列表
	private List<Province> provinceList;
//	市列表
	private List<City> cityList;
//	县列表
	private List<County> countyList;
//	选中的省份
	private Province selectedProvince;
//	选中的城市
	private City selectedCity;
//	当前选中的级别
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		xiaoliweatherDB=XiaoliWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince=provinceList.get(index);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(index);
					queryCounties();
				}
				
			}});
//	加载省级数据
		queryProvinces();
	}

//	查询全国所有省,优先从数据库查询,如果没有查询到再去服务器查询.

	private void queryProvinces() {
		provinceList =xiaoliweatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province :provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel =LEVEL_PROVINCE;
			
		}else {
			queryFromServer(null,"province");
		}
		
	}

	

//查询省中的所有市,有限从数据库查询,如果没有查询到再去服务器查询.

	protected void queryCities() {
		
		cityList =xiaoliweatherDB.loadCities( selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city :cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel =LEVEL_CITY;
			
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
//	查询所有的县,优先从数据库查询,如果没有再从服务器查询.
	
	protected void queryCounties() {

		countyList =xiaoliweatherDB.loadCounties( selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county :countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel =LEVEL_COUNTY;
			
		}else {
			queryFromServer(selectedCity.getCityCode(),"county");
		}
		
	}
	
//	根据传入的Code从服务器查询数据
	
	private void queryFromServer(final String code, final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){			
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";			
		}else{			
			address="http://www.weather.com.cn/data/list3/city.xml";			
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){

	 		@Override
			public void onFinish(String response) {
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(xiaoliweatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCitiesResponse(xiaoliweatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(xiaoliweatherDB, response, selectedCity.getId());
				}
				if(result){
//		通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}

						});
					
					
					
				}
				
			}

			@Override
			public void onError(Exception e) {
//		通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", 0).show();
						
					}});
				
			}});
	}

private void showProgressDialog() {
	if(progressDialog==null){
		progressDialog=new ProgressDialog(this);
		progressDialog.setMessage("正在加载...");
		progressDialog.setCanceledOnTouchOutside(false);
		
	}
	progressDialog.show();
}
// 关闭进度对话框
private void closeProgressDialog() {
	if(progressDialog !=null){
		progressDialog.dismiss();
	}
	
}
//  捕获Back按键,根据当前的级别来判断,此时应该返回市列表,省列表还是直接退出.
@Override
public void onBackPressed() {
	// TODO Auto-generated method stub
	if(currentLevel==LEVEL_COUNTY){
		queryCities();
	}else if(currentLevel==LEVEL_CITY){
		queryProvinces();
	}else{
		finish();
	}
}

}