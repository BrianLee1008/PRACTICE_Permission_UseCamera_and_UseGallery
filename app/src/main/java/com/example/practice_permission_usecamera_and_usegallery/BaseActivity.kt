package com.example.practice_permission_usecamera_and_usegallery

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

// Permission 승인에 관련된 반복적인 기능 처리
/*
* require == 필요하다
* request == 요청하다
* replace == 바꾸다
* result == 결과*/

abstract class BaseActivity : AppCompatActivity(){

	abstract fun permissionGranted(requestCode : Int)
	abstract fun permissionDenied(requestCode: Int)

	fun requirePermission(permissions : Array<String>, requestCode: Int){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
			permissionGranted(requestCode)
		} else {
			// it은 String 타입받는 모든 것.
			val isAllPermissionGranted = permissions.all{checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED}

			if(isAllPermissionGranted) {
				permissionGranted(requestCode)
			} else {
				// ActivityCompat의 Permission요청기능 실행
				ActivityCompat.requestPermissions(this, permissions, requestCode)
			}
		}
	}

	//requestPermissions에 대한 답장. 사용자의 승인 혹은 거부 후의 기능 결정
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		if(grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
			permissionGranted(requestCode)
		} else {
			permissionDenied(requestCode)
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

}