package com.example.hangraquaymobile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.example.hangraquaymobile.databinding.FragmentExportBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ExportFragment : Fragment() {
    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    private var exportedFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button: Tạo phiếu xuất hàng
        binding.btnCreate.setOnClickListener {
            val maNV = binding.editTextMNV.text.toString().trim()
            if (maNV.isEmpty()) {
                binding.logOutput.text = "Vui lòng nhập mã nhân viên."
                return@setOnClickListener
            }

            startExportProcess(maNV)
        }

        binding.btnShare.setOnClickListener {
            exportedFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Chia sẻ phiếu xuất"))
            } ?: run {
                binding.logOutput.append("⚠️ Không tìm thấy file để chia sẻ.\n")
            }
        }


        // Button: Clear
        binding.btnClear.setOnClickListener {
            binding.editTextMNV.setText("")
            binding.logOutput.text = ""
            binding.progressBar.progress = 0
            binding.btnShare.isEnabled = false
        }
    }

    private fun startExportProcess(maNV: String) {
        binding.spinner.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.progressBar.progress = 0
        binding.logOutput.text = "🔄 Đang tạo phiếu xuất cho Mã NV: $maNV...\n"

        mainScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("https://fa57-113-161-61-210.ngrok-free.app/get_data?mnv=$maNV&template=phieu_xuat") // 🔁 Update with your API URL
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val fileName = "phieu_xuat_$maNV.docx"

                    // Save using scoped storage: safe for Android 10+
                    val file = File(requireContext().getExternalFilesDir(null), fileName)
                    val outputStream = FileOutputStream(file)

                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    withContext(Dispatchers.Main) {
                        exportedFile = file
                        binding.progressBar.progress = 100
                        binding.logOutput.append("✅ Đã lưu thành công tại:\n${file.absolutePath}\n")
                        binding.spinner.visibility = View.GONE
                        binding.btnShare.isEnabled = true
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.logOutput.append("❌ Lỗi API: ${response.code}\n")
                        binding.spinner.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.logOutput.append("❌ Lỗi kết nối: ${e.message}\n")
                    binding.spinner.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainScope.cancel() // Cancel coroutines to prevent leaks
        _binding = null
    }

}