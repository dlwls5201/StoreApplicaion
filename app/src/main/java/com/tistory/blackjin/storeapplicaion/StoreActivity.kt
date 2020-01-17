package com.tistory.blackjin.storeapplicaion

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.opengl.GLES30
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import kotlinx.android.synthetic.main.activity_store.*
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class StoreActivity : AppCompatActivity() {

    private var tempFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        innerStoreExample()

        chkPermission()

        btnGallery.setOnClickListener {
            goToAlbum()
        }

        btnCamera.setOnClickListener {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Timber.d("requestCode : $requestCode , grantResults : ${grantResults.contentToString()}")
        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Timber.e("permission reject")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Timber.d("requestCode : $requestCode , resultCode : $resultCode")

        if (resultCode != Activity.RESULT_OK) {
            //기존에 생성한 파일 제거
            Timber.e("취소")
            tempFile?.run {
                Timber.e("tempFile exists : ${exists()}")
                if (exists()) {
                    delete()
                }
                return
            }
        }

        if (requestCode == PICK_FROM_ALBUM) {

            intent?.data?.let { photoUri ->

                Timber.d("onActivityResult photoUri : $photoUri")

                /**
                 * 읽기 권한이 없어도 동작됩니다.
                 */
                val selectedImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    /**
                     * 하드웨어 가속이 필요
                     * java.lang.IllegalArgumentException: Software rendering doesn't support hardware bitmaps
                     */
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    contentResolver.openInputStream(photoUri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }

                    //MediaStore.Images.Media.getBitmap(this.contentResolver, photoUri)

                    //val input = contentResolver.openInputStream(photoUri)
                    //setBitmap(BitmapFactory.decodeStream(input, null, null))
                    //input?.close()
                }

                setBitmap(selectedImage)

                return
            }

        } else if (requestCode == PICK_FROM_CAMERA) {
            //카메라 에서는 intent,data == null 입니다.
            setBitmap(
                BitmapFactory.decodeFile(tempFile?.absolutePath)
            )
        }
    }

    // 메모리보다 사진 사이즈가 크다면
    // OpenGLRenderer: Bitmap too large to be uploaded into a texture (4160x2340, max=4096x4096)
    // https://yollo.tistory.com/12
    private fun setBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {

            if (bitmap.width > GLES30.GL_MAX_TEXTURE_SIZE || bitmap.height > GLES30.GL_MAX_TEXTURE_SIZE) {
                imageView.setImageBitmap(bitmap.scale(1024, 1024))
            } else {
                imageView.setImageBitmap(bitmap)
            }

        }

    }

    /**
     * 카메라 권한
     */
    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    /**
     * WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
     * 둘 중 하나만 받으면 적용 됩니다.
     */
    private val permissionCheckWrite by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    private val permissionCheckRead by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun chkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (permissionCheckCamera == PackageManager.PERMISSION_DENIED
                || permissionCheckRead == PackageManager.PERMISSION_DENIED
                || permissionCheckWrite == PackageManager.PERMISSION_DENIED
            ) {

                // 권한 없음
                showRequestPermission()

            } else {

                // 권한 있음
                Timber.e("---- already have permission ----")
                //makeFile()
                getQuery()

            }
        }
    }

    private fun showRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    private fun goToAlbum() {
        /*val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, PICK_FROM_ALBUM)*/

        /*val intent = Intent(
            Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_FROM_ALBUM)*/

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FROM_ALBUM)
    }

    private fun takePhoto() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        tempFile = createImageFile()

        tempFile?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                /**
                 * photoUri1 ->  file:///storage/emulated/0/Android/data/{packageName}/files/Pictures/blackJin/.. 경로가 나옴
                 * photoUri2 ->  content://{Manifest 에서 설정한 provider 정보}/Android/data/{packageName}/files/Pictures/blackJin/.. 경로가 나옴
                 */
                val photoUri1 = Uri.fromFile(it)

                //내부 저장소에 저장시 여기서 에러가 나옵니다. -> java.lang.IllegalArgumentException:
                val photoUri2 = FileProvider.getUriForFile(
                    this,
                    "${application.packageName}.provider",
                    it
                )

                Timber.d("photoUri1 : $photoUri1")
                Timber.d("photoUri2 : $photoUri2")

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri2)
                startActivityForResult(intent, PICK_FROM_CAMERA)

            } else {

                val photoUri = Uri.fromFile(it)
                Timber.d("photoUri : $photoUri")

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }

    }

    private fun createImageFile(): File? {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "blackJin"

        /**
         *  내부 저장소 : filesDir, cacheDir
         *  > android:allowBackup="true" 이면 데이터가 남아 있다
         *  > data/data/{package}
         *
         *  외부 앱 공간 저장소 : getExternalFilesDir(Environment.DIRECTORY_PICTURES)
         *  > android:allowBackup="true" 이면 데이터가 남아 있다
         *  > storage/self/primary/Android/data/{package} , sdcard/Android/data/{package} 같이 저장 - 한쪽 파일을 지우면 같이 제거
         *  > 앱 제거 시 둘다 삭제
         *
         *  외부저장소인 경우에는 권한이 필요하다.(외부 저장소에 파일을 생성할 경우 안드로이드Q 이상에서는 권한이 있어도 에러가 발생합니다.)
         *
         *  외부 저장소 : Environment.getExternalStorageDirectory()
         *  > storage/self/primary , sdcard 같이 저장  - 한쪽 파일을 지우면 같이 제거
         */
        //val path = cacheDir.absolutePath
        //val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val path = Environment.getExternalStorageDirectory().absolutePath

        val storageDir = File(path)

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        Timber.d("path 경로 : $path")

        try {
            val tempFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            Timber.d("createImageFile 경로 : ${tempFile.absolutePath}")

            return tempFile

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_CAMERA = 1
        private const val PICK_FROM_ALBUM = 2
    }

    private fun innerStoreExample() {

        /**
         * 내부 저장소
         *
         * 파일 저장소
         * 캐시 저장소
         *
         *  <앱 삭제시 제거 됨>
         */
        Timber.e("--내부 저장소--")
        Timber.d("getFileDir : ${filesDir.absolutePath}")
        Timber.d("getCacheDir : ${cacheDir.absolutePath}")

        /**
         *  외부 저장소 App 전용
         *
         *  파일 저장소
         *  캐시 저장소
         *
         *  <앱 삭제시 제거 됨>
         */
        Timber.e("--외부 앱 전용 공간--")
        Timber.d("getExternalCacheDir : ${externalCacheDir?.absolutePath}")
        Timber.d("getExternalFilesDir : ${getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath}")

        /**
         * 외부 저장소
         *
         * <저장소에 남음>
         */
        Timber.e("--외부 저장소--")
        Timber.d("getExternalStorageDirectory1 : ${Environment.getExternalStorageDirectory()}")
        Timber.d(
            "getExternalStorageDirectory2 : ${Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )}"
        )
    }

    private val INDEX_MEDIA_URI = MediaStore.MediaColumns.DATA
    private val INDEX_DATE_ADDED = MediaStore.MediaColumns.DATE_ADDED

    private val albumName: String = MediaStore.Images.Media.BUCKET_DISPLAY_NAME

    private fun makeFile() {

        //Device File Explorer 통해 정보를 확인할 수 있다.
        Timber.e("--내부 저장소 쓰기--")
        val fileName = "blackJin"

        /**
         *  외부 저장소에 파일을 생성할 경우 안드로이드Q 이상에서는 권한이 있어도 에러가 발생합니다.
         */
        val innerDir = File(Environment.getExternalStorageDirectory().absolutePath)

        val innerFile = File.createTempFile(fileName, ".txt", innerDir)

        val fos = FileOutputStream(innerFile)
        val writer = BufferedWriter(OutputStreamWriter(fos))
        writer.write("BlackJin Hello")

        writer.flush()
        writer.close()
        fos.close()

        Timber.e("--내부 저장소 읽기--")
        val strBuffer = StringBuffer()
        val `is` = FileInputStream(innerFile)
        val reader = BufferedReader(InputStreamReader(`is`))

        val read = strBuffer.append(reader.readLine())
        Timber.d("path : ${innerFile.absolutePath}")
        Timber.d("read : $read")

        reader.close()
        `is`.close()
    }

    private fun getQuery() {

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        /*uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        albumName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME*/

        val sortOrder = "$INDEX_DATE_ADDED DESC"
        val projection = arrayOf(INDEX_MEDIA_URI, albumName, INDEX_DATE_ADDED)

        //requires android.permission.READ_EXTERNAL_STORAGE
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        val albumList = cursor?.let {
            Timber.d(Arrays.toString(it.columnNames))

            val list = generateSequence { if (cursor.moveToNext()) cursor else null }
                .map(::getImage)
                .filterNotNull()
                .toList()

        }

        cursor?.close()
    }

    private fun getImage(cursor: Cursor) =
        try {
            cursor.run {
                val folderName = getString(getColumnIndex(albumName))
                val mediaPath = getString(getColumnIndex(INDEX_MEDIA_URI))
                val mediaUri: Uri = Uri.fromFile(File(mediaPath))
                val datedAddedSecond = getLong(getColumnIndex(INDEX_DATE_ADDED))

                //매우 위험한 코드이다. 갤러리에 있는 모든 사진을 삭제해 버린다.
                //안드로이드 Q 에서는 실행은 되나 실제로 파일은 지워지지 않는다... 에러도 없다...?? 어렵다....
                /*val file = File(mediaPath)
                if(file.exists()) {
                    Timber.e("delete")
                    file.delete()
                }*/

                Timber.d("folderName : $folderName , mediaPath : $mediaPath , mediaUri : $mediaUri")
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }


}
