package com.the8way.digitaldiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.the8way.digitaldiary.DiaryApplication
import com.the8way.digitaldiary.R
import com.the8way.digitaldiary.data.DiaryEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val diaryEntryViewModel: DiaryEntryViewModel by viewModels {
        DiaryEntryViewModelFactory((application as DiaryApplication).database.diaryEntryDao())
    }

    private lateinit var adapter: DiaryEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AppInitializer.initializeDatabase(this)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, AddEntryActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        val noEntriesTextView = findViewById<TextView>(R.id.noEntriesTextView)

        adapter = DiaryEntryAdapter(emptyList(), ::onItemClick, ::onItemLongClick)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fab.setOnClickListener {
            startActivity(Intent(this, AddEntryActivity::class.java))
        }

        diaryEntryViewModel.diaryEntries.observe(this) { entries ->
            adapter = DiaryEntryAdapter(entries, ::onItemClick, ::onItemLongClick)
            recyclerView.adapter = adapter
            noEntriesTextView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun onItemClick(diaryEntry: DiaryEntry) {
        val intent = Intent(this, EditEntryActivity::class.java)
        intent.putExtra("ENTRY_ID", diaryEntry.id)
        startActivity(intent)
    }

    private fun onItemLongClick(diaryEntry: DiaryEntry) {
        lifecycleScope.launch {
            diaryEntryViewModel.deleteEntryById(diaryEntry.id)
            Toast.makeText(this@MainActivity, R.string.delete, Toast.LENGTH_SHORT).show()
        }
    }
}
