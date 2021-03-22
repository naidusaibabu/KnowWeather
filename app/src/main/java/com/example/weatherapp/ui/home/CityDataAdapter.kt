package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ItemCityBinding
import com.example.weatherapp.repo.db.room.Bookmark

class CityDataAdapter(
    private val listener: OnBookmarkClickListener?,
    private val itemListener: OnItemClickListener?
) :
    RecyclerView.Adapter<CityDataAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(val binding: ItemCityBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        init {
            binding.bookmark.setOnClickListener(this)
            binding.tvTitle.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (v!!.id == R.id.bookmark)
                    listener?.onBookmarkClick(position)
                if (v.id == R.id.tvTitle)
                    itemListener?.onItemClick(position)
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Bookmark>() {
        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    var bookmarks: MutableList<Bookmark>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun getItemCount() = bookmarks.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            ItemCityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.binding.apply {
            val city = bookmarks[position]
            tvTitle.text = city.cityName
            if (!city.temp.isEmpty()) {
                bookmark.visibility = View.GONE
                temparatureTV.visibility = View.VISIBLE
                temparatureTV.text = city.temp
            }
            when (city.bookmarked) {
                true -> bookmark.setImageResource(R.drawable.ic_baseline_bookmark_fill)
                else -> bookmark.setImageResource(R.drawable.ic_baseline_bookmark_empty)
            }
        }
    }

    interface OnBookmarkClickListener {
        fun onBookmarkClick(position: Int)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}