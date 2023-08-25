package xcj.app.appsets.ui.nonecompose.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class MyViewHolder<T:Any>(val binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root){
    var data:T? = null
    fun bind(data:T){
        this.data = data
    }
}