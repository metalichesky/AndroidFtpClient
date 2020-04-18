package com.example.ftpclient.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ftpclient.App
import com.example.ftpclient.R
import kotlinx.android.synthetic.main.item_file_list.view.*
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.util.*
import kotlin.Comparator

class FileListAdapter() : RecyclerView.Adapter<FileListAdapter.ViewHolder>(){
    private var items: LinkedList<FTPFile> = LinkedList()
    var onOptionsClick: (itemNumber: Int)->Unit = {}
    var onItemLongClick: (itemNumber: Int)-> Unit = {}
    var onItemClick: (itemNumber: Int)-> Unit = {}

    var comparator = object: Comparator<FTPFile> {
        override fun compare(file1: FTPFile?, file2: FTPFile?): Int {
            if (file1 == null && file2 == null) return 0
            file1 ?: return -1
            file2 ?: return 1
            var sum1 = if (file1.isDirectory) 10 else 0
            var sum2 = if (file2.isDirectory) 10 else 0
            return  sum2.compareTo(sum1)
        }
    }

    fun getItems(): LinkedList<FTPFile> = items
    override fun getItemCount(): Int = items.size
    var selectedItem = -1

    fun getItem(idx: Int): FTPFile? = items.getOrNull(idx)

    fun setItems(newItems: Collection<FTPFile>){
        items.clear()
        val sortedItems = newItems.sortedWith(comparator)
        items.addAll(sortedItems)
        notifyDataSetChanged()
    }

    fun addItem(newItem: FTPFile){
        items.add(newItem)
        notifyItemInserted(items.size - 1)
    }

    fun removeItems(){
        val previousSize = items.size
        items.clear()
        notifyItemRangeRemoved(0, previousSize)
    }

    fun removeItem(index: Int){
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun selectItem(item: Int){
        val previousSelection = selectedItem
        selectedItem = item
        if (previousSelection in items.indices) {
            notifyItemChanged(previousSelection)
        }
        notifyItemChanged(selectedItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = inflater.inflate(R.layout.item_file_list, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemToBind = items[holder.adapterPosition]
        holder.bind(itemToBind)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val type = view.ivType
        val fileName = view.tvFilename
        val optionsButton = view.btnOptions
        val container = view.container
        fun bind(item: FTPFile){
//            if (adapterPosition == selectedItem) {
//                container.setBackgroundColor(ContextCompat.getColor(App.getContext(), R.color.colorSelection))
//            } else {
//                container.setBackgroundColor(ContextCompat.getColor(App.getContext(), R.color.colorBackground))
//            }
            container.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(adapterPosition)
                }
            }
            type.setImageResource(if (item.isDirectory) {
                R.drawable.ic_folder
            } else {
                R.drawable.ic_file
            })
            fileName.setText(item.name)
//            fileName.setOnClickListener {
//
//            }

            container.setOnGenericMotionListener(object: View.OnGenericMotionListener{
                override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
                    return true
                }
            })

            optionsButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onOptionsClick(adapterPosition)
                }
            }
        }
    }
}