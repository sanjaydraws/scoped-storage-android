package com.sanjay.scopedstorage.androidstorage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanjay.scopedstorage.databinding.ItemPhotoBinding

class InternalStoragePhotoAdapter(private val onPhotoClick: (InternalStoragePhoto) -> Unit) : ListAdapter<InternalStoragePhoto, InternalStoragePhotoAdapter.PhotoViewHolder>(Companion) {


    companion object : DiffUtil.ItemCallback<InternalStoragePhoto>() {
        override fun areItemsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: InternalStoragePhoto, newItem: InternalStoragePhoto): Boolean {
            return oldItem.name == newItem.name && oldItem.bmp.sameAs(newItem.bmp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = currentList[position]
        holder.setUpData(photo)

    }
    inner class PhotoViewHolder(val binding: ItemPhotoBinding): RecyclerView.ViewHolder(binding.root) {
        fun setUpData(photo: InternalStoragePhoto?) {
            binding.apply {
                ivPhoto.setImageBitmap(photo?.bmp)

                val width:Float? = photo?.bmp?.width?.toFloat()
                val height:Float? = photo?.bmp?.height?.toFloat()
//                width?.let {
//                    val aspectRatio:Float = width / height!!
//                }
//
//                    ConstraintSet().apply {
//                    clone(root)
//                    setDimensionRatio(ivPhoto.id, aspectRatio.toString())
//                    applyTo(root)
//                }
            }
            binding.ivPhoto.setOnLongClickListener {
                if (photo != null) {
                    onPhotoClick(photo)
                }
                true
            }

        }
    }

}