package com.example.hangraquaymobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hangraquaymobile.databinding.FragmentKphBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import com.example.hangraquaymobile.ExcelTableAdapter
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class KPHFragment : Fragment() {
    private var _binding: FragmentKphBinding? = null
    private val binding get() = _binding!!
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    private var file: File? = null
    private val dataList = mutableListOf<MutableList<String>>()
    private lateinit var adapter: ExcelTableAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ExcelTableAdapter(dataList)
        binding.recyclerView.adapter = adapter

        binding.btnCreate.setOnClickListener {
            val maNV = binding.editTextMNV.text.toString().trim()
            if (maNV.isEmpty()) {
                binding.logOutputTop.text = "Vui lòng nhập mã nhân viên."
                return@setOnClickListener
            }
            fetchExcelFile(maNV)
        }

        binding.btnClear.setOnClickListener {
            binding.editTextMNV.setText("")
            binding.logOutputTop.text = ""
            dataList.clear()
            adapter.notifyDataSetChanged()
            binding.progressBar.progress = 0
            binding.btnPreview.isEnabled = false
            binding.btnShare.isEnabled = false
        }

        binding.btnPreview.setOnClickListener {
            file?.let {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(intent)
            }
        }

        binding.btnShare.setOnClickListener {
            file?.let {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Chia sẻ file KPH"))
            }
        }

        binding.btnPreview.isEnabled = false
        binding.btnShare.isEnabled = false
    }

    private fun fetchExcelFile(maNV: String) {
        binding.progressBar.progress = 0
        binding.logOutputTop.text = "🔄 Đang tạo dữ liệu KPH cho $maNV..."

        mainScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url("https://chrome-api-imq9.onrender.com/generate_export?mnv=$maNV&template=KPH")
                    .build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    val fileName = "KPH_$maNV.xlsx"
                    val fileOut = File(requireContext().getExternalFilesDir(null), fileName)
                    FileOutputStream(fileOut).use { output ->
                        inputStream?.copyTo(output)
                    }

                    file = fileOut

                    parseExcelFile(fileOut)

                    withContext(Dispatchers.Main) {
                        binding.logOutputTop.append("\n✅ File đã lưu: ${fileOut.absolutePath}")
                        binding.btnPreview.isEnabled = true
                        binding.btnShare.isEnabled = true
                        binding.progressBar.progress = 100
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.logOutputTop.append("\n❌ Lỗi API: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.logOutputTop.append("\n❌ Lỗi kết nối: ${e.message}")
                }
            }
        }
    }

    private fun parseExcelFile(file: File) {
        dataList.clear()
        try {
            val inputStream = FileInputStream(file)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            for (row in sheet) {
                val rowData = mutableListOf<String>()
                for (cell in row) {
                    rowData.add(cell.toString())
                }
                dataList.add(rowData)
            }
            inputStream.close()
            workbook.close()
        } catch (e: Exception) {
            Log.e("KPH", "Parse error", e)
        }

        mainScope.launch(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainScope.cancel()
        _binding = null
    }
}
