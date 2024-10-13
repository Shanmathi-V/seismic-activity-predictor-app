package com.example.lunarnova

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MarsFragment : Fragment() {

    private val PICK_FILE_REQUEST = 1
    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mars, container, false)

        val buttonSelectCsv: Button = view.findViewById(R.id.selectCsvButton)
        val buttonUploadCsv: Button = view.findViewById(R.id.uploadCsvButton)
        val textViewFileName: TextView = view.findViewById(R.id.fileNameText)

        buttonSelectCsv.setOnClickListener {
            selectFile()
        }

        buttonUploadCsv.setOnClickListener {
            fileUri?.let {
                uploadFile(it)
            } ?: Toast.makeText(requireContext(), "Please select a file first", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"  // "*/*" Allows all file types
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, PICK_FILE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                fileUri = it
                val fileName = it.path?.split("/")?.last() ?: "Unknown file"
                view?.findViewById<TextView>(R.id.fileNameText)?.text = "File selected: $fileName"
            }
        }
    }

    private fun uploadFile(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "uploaded.csv")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            // Read a snippet of file content to log
            val fileContentSnippet = file.readText().take(100) // Logs only the first 500 characters
            Log.e("uploadFile", "Request File Content Snippet:\n$fileContentSnippet")

            //Fuel
//            Fuel.upload("https://nasa-api-seismic-waves.onrender.com/predict-seismic-events/")
//                .add(FileDataPart(file,filename = file.name, contentType = "text/csv")) // Add your CSV file
//                .timeout(120_000) // Set timeout to 120 seconds
//                .response { _, _, result ->
//                    result.fold(
//                        { data -> Log.d("UploadFile", "Success: $data") },
//                        { error -> Log.e("UploadFile", "Error: ${error.message}") }
//                    )
//                }


            //temp commented BEGIN

//            val requestFile = RequestBody.create(MediaType.parse("text/csv"), file)
            val requestFile = file.asRequestBody("text/csv".toMediaTypeOrNull()) //changed

            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Log request details
            Log.e("uploadFile", "Request File Name: ${file.name}")
            Log.e("uploadFile", "Request File Size: ${file.length()} bytes")
            Log.e("UploadFile", "MultipartBody: ${multipartBody.body.contentLength()} bytes, contentType: ${multipartBody.body.contentType()}")


            val call = RetrofitClient.apiService.uploadFile(multipartBody)
            call.enqueue(object : Callback<SeismicResponse> {
                override fun onResponse(call: Call<SeismicResponse>, response: Response<SeismicResponse>) {

                    if (response.isSuccessful) {
                        response.body()?.let {
                            // Handle the response data here

                            Toast.makeText(requireContext(), "Success", Toast.LENGTH_LONG).show()
                            Log.e("uploadFile","onresponse success: ${response.body()}")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(requireContext(), "Upload failed : response not successful- ${errorBody}", Toast.LENGTH_LONG).show()
                        Log.e("uploadFile","onresponse not success $errorBody")
                    }
                }

                override fun onFailure(call: Call<SeismicResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("uploadFile", "onfailure"+ t.localizedMessage)
                    // Log detailed stack trace
                    Log.e("uploadFile", "onfailure Error: ${t.message}")
                    Log.e("uploadFile", "onfailure Stack Trace: ${t.stackTraceToString()}")
                }
            })
            // temp commented END

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("onresponse", "File read failed : exception" + e.message)
        }
    }
}