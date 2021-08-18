package com.example.practice_permission_usecamera_and_usegallery

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.practice_permission_usecamera_and_usegallery.databinding.ActivityMainBinding
import java.io.IOException
import java.text.SimpleDateFormat

/*
* 카메라, 외부저장소 사용자 권한
* 찍은 이미지 contentResolver로 가져와 Uri 생성.
* Uri를 Bitmap으로 변환후 setImageBitmap
* 갤러리 이동해서 선택하면 imageView에 뿌리기
* */

//xo 카메라
//co 갤러리

@Suppress("DEPRECATION")
class MainActivity : BaseActivity() {

	private val permissionCamera = 100 //카메라 권한 요청
	private val reqCamera = 101 // 실제 카메라 사용후 resultCode

	private val permissionCameraAndPreview = 102 //카메라 사용 후 이미지 뿌리기

	private val permissionGallery = 200 // 외부저장소 권한 요청
	private val reqGallery = 201 // 외부저장소 resultCode

	// xo 1. 실제 이미지 Uri 넣어줄 realUri 변수
	private var realUri: Uri? = null

	override fun permissionGranted(requestCode: Int) {
		when (requestCode) {
			// xo 3. 카메라 권한이 승인될 경우 openCamera 메서드 호출
			permissionCamera -> openCamera()
			permissionCameraAndPreview -> openCameraAndPreview()

			// co 2. 외부저장소 권한이 승인될 경우 openGallery 메서드 호출
			permissionGallery -> openGallery()

		}

	}

	override fun permissionDenied(requestCode: Int) {
		when (requestCode) {
			//xo 4. 카메라 권한이 거부될 경우 toast
			permissionCamera -> Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
			permissionCameraAndPreview -> Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT)
				.show()
		}

	}

	private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		setListener()


	}

	private fun setListener() {
		binding.run {
			//xo 2. 카메라 버튼 클릭하면 카메라 사용 권한 요청
			buttonCamera.setOnClickListener() {
				requirePermission(arrayOf(Manifest.permission.CAMERA), permissionCamera)
			}

			buttonCameraAndGallery.setOnClickListener() {
				requirePermission(arrayOf(Manifest.permission.CAMERA), permissionCameraAndPreview)
			}

			//co 1. 갤러리 버튼을 누르면 뫼부저장소 사용 권한 요청
			buttonGallery.setOnClickListener(){
				requirePermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),permissionGallery)


			}
		}
	}


	// xo 5. 카메라 여는 메서드
	private fun openCamera() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

		// xo 8. 생성한 Uri를 호출하고, 매개변수의 fileName에는 newFileName을, 확장자에는 모든 이미지 확장자를 정의.
		// xo	 그 후 생성한 uri를 realUri 변수에 담는다.
		createImageUri(newFileName(), "image/*").let { uri ->
			realUri = uri // uri일 경우 realUri에 저장

			//xo 9. intent에 realUri 데이터를 담는다.
			intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)

			//xo 10. intent와 함께 reqCamera Code도 함께 보낸다.
			//		 여기까지 하면 카메라를 키고 사진을 찍은다음 갤러리에 저장하는 것.
				startActivity(intent)

			//xo 11. realUri안에 있는 Uri를 없애준다.
			realUri = null
		}
	}

	// xo 6. 촬영한 이미지를 저장할 Uri를 MediaStore에 생성하는 메서드
	// xo 	 contentResolver를 반환하는데 EXTERNAL_CONTENT_URI, 파일명과 확장자를 담은 value를 반환한다.
	private fun createImageUri(fileName: String, mimeType: String): Uri? {
		val value = ContentValues()
		value.put(MediaStore.Images.Media.DISPLAY_NAME, fileName) // MediaStore에서 생성한 이름을 fileName에 저장할거야
		value.put(MediaStore.Images.Media.MIME_TYPE, mimeType) // MediaStore에서 생성한 확장자를 mimeType에 저장할거야

		//콘텐트리졸버로 찍은 사진의 MediaStore.Uri를 가져오고 value값에 저장.
		return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)
	}

	//xo 7. openCamera에서 사용할 creatImageUri 메서드 안에 들어갈 fileName을 생성하는 메서드.
	//xo    서로 중복되지 않도록 파일 생성 시간을 활용해 이름을 짓는다.
	private fun newFileName(): String {
		val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
		val fileName = sdf.format(System.currentTimeMillis())

		return "$fileName.jpg"
	}

	//xo 12. 그냥 openCamera 메서드와 동일하나, 그 후 Preview 기능을 위해 해당 기능의 메서드를 하나 더 만들어준다.
	private fun openCameraAndPreview() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

		createImageUri(newFileName(), "image/*").let { uri ->
			realUri = uri

			intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)

			//그냥 카메라를 열기만 할 땐 Result가 필요없었지만 이번엔 Perview까지 해야하므로 startActivityForResult 호출 한뒤 requestCode까지.
			startActivityForResult(intent, reqCamera)
		}
	}

	// xo 13. 갤러리에 저장된 imageUri를 사용해 미디어 스토어에 저장된 이미지를 읽어오는 메서드
	// xo	  loadBitmap의 매개변수에는 uri값이 들어오는데 그것을 Bitmap으로 반환시킨다.
	private fun loadBitmap(photoUri : Uri) : Bitmap? {
		var image : Bitmap? = null

		try { //API별로 이미지 처리 위한 try catch
			image = if(Build.VERSION.SDK_INT > 27){
				val source : ImageDecoder.Source = ImageDecoder.createSource(this.contentResolver, photoUri)
				ImageDecoder.decodeBitmap(source)
			} else {
				MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)
			}

		} catch (e : IOException){
			e.printStackTrace()
		}
		return image
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if(resultCode == RESULT_OK)
			when(requestCode){
				//xo 14. resultCode값이 다 맞고 requstCode값이 reqCamera일 경우
				reqCamera -> {
					realUri?.let {
						// uri를 bitmap으로 반환하는 loadBitmap 메서드에 realUri의 uri를 넣어준다.
							uri -> val bitmap = loadBitmap(uri)
						//xo 15. 그렇게 uri가 Bitmap으로 잘 변환이 되었으면 Preview 해준다.
						binding.imagePreview.setImageBitmap(bitmap)

						realUri = null
					}
				}

				// co 4. 갤러리로 들어온 이후 기능
				reqGallery ->{
					data?.data.let { uri ->
						binding.imagePreview.setImageURI(uri)
					}
				}
			}
	}

	// co 3. 갤러리를 여는 메서드. Reulst메서드에 보내는 데이터로 type과 data를 보낸다.
	private fun openGallery(){
		val intent = Intent(Intent.ACTION_PICK)
		intent.type = MediaStore.Images.Media.CONTENT_TYPE
		intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

		startActivityForResult(intent, reqGallery)
	}


}