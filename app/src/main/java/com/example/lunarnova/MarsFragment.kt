package com.example.lunarnova

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

        val buttonUploadCsv: Button = view.findViewById(R.id.uploadCsvButton)
        val textViewFileName: TextView = view.findViewById(R.id.fileNameText)
        val predictedEventsTextView: TextView = view.findViewById(R.id.predictedEventsTextView)
        val plotImageView: ImageView = view.findViewById(R.id.plotImageView)

        buttonUploadCsv.setOnClickListener {
            selectFile()
        }

        view.findViewById<Button>(R.id.uploadCsvButton).setOnClickListener {
            fileUri?.let {
                uploadFile(it)
            } ?: Toast.makeText(requireContext(), "Please select a file first", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/csv"
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

            val requestFile = RequestBody.create(MediaType.parse("text/csv"), file)
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val call = RetrofitClient.apiService.uploadFile(multipartBody)
            call.enqueue(object : Callback<SeismicResponse> {
                override fun onResponse(call: Call<SeismicResponse>, response: Response<SeismicResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            // Handle the response data here
                            val encodedPlot = it.plot // Base64 encoded string
                            val predictedSeismicEvents = it.predicted_seismic_events

                            // Decode the Base64 string to Bitmap
                            val decodedBytes = Base64.decode(encodedPlot, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                            // Display the predicted seismic events
                            val predictedEventsTextView: TextView = view?.findViewById(R.id.predictedEventsTextView)!!
                            predictedEventsTextView.text = "Predicted Seismic Events: $predictedSeismicEvents"

                            // Display the decoded image in an ImageView
                            val plotImageView: ImageView = view?.findViewById(R.id.plotImageView)!!
                            plotImageView.setImageBitmap(bitmap)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Upload failed: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<SeismicResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    Log.e("MarsFragment", "Upload failed", t)
                }
            })

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("MarsFragment", "File read failed", e)
        }
    }
}
