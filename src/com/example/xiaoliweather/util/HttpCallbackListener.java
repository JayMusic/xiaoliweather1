package com.example.xiaoliweather.util;

public interface HttpCallbackListener {

	void onFinish(String response);
	void onError(Exception e);
}
