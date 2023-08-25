package xcj.app.appsets.ui.nonecompose.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import xcj.app.appsets.databinding.ItemEmptyWrapperBinding
import xcj.app.core.android.toplevelfun.inflateViewDataBindingFromClass
import kotlin.reflect.KClass

abstract class BaseAdapter<D: Diffable>(
    dls: BaseAdapter<D>.() -> Unit = {}
): RecyclerView.Adapter<MyViewHolder<D>>(), ImageLoadable {
    lateinit var context: Context
    var recyclerView: RecyclerView?=null
    private set
    private val dataObserver:RecyclerView.AdapterDataObserver by lazy {
        object :RecyclerView.AdapterDataObserver(){
            override fun onChanged() {
                emptyMode = data.isEmpty()
            }
        }
    }
    private var emptyMode = false
    private val emptyData: Diffable by lazy { EmptyDiffale() }
    private val emptyViewWrapperTypeAndBindingClass: Pair<Int, KClass<ViewDataBinding>> by lazy {
        (Int.MIN_VALUE to ItemEmptyWrapperBinding::class) as Pair<Int, KClass<ViewDataBinding>>
    }
    var emptyViewWrapperDataBinding:ItemEmptyWrapperBinding?=null
    private set
    var customEmptyViewDataBinding:ViewDataBinding?=null
    private set

    private val itemsTypeAndBindingClass:MutableMap<Int, KClass<ViewDataBinding>> by lazy { mutableMapOf() }
    val data:MutableList<D> = mutableListOf()

    var itemClickListener: ((itemBinding:ViewDataBinding, position:Int)->Unit)? = null
    var itemChildClickListener: ((temBinding:ViewDataBinding, clickedView:View, position:Int)->Unit)? = null
    private val clickableIdsInsideItem:MutableList<Int> = mutableListOf()

    init {
        dls()
        initItems(itemsTypeAndBindingClass as MutableMap<Int, KClass<*>>)
    }
    fun addClickableIdsInsideItem(vararg ids:Int){
        clickableIdsInsideItem.addAll(ids.toList())
    }
    override fun onViewRecycled(holder: MyViewHolder<D>) {
        super.onViewRecycled(holder)
        recyclerView = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder<D> {
        val bindingClazz: Class<ViewDataBinding>? = if(emptyMode){
            emptyViewWrapperTypeAndBindingClass.second.java
        }else{
            itemsTypeAndBindingClass[viewType]?.java
        }
        assert(bindingClazz != null) {
            "当前item的viewDataBinding Class不存在/1!"
        }
        val binding = inflateViewDataBindingFromClass(
            parent.context,
            bindingClazz!!,
            parent
        ).apply {

        }
        assert(binding != null) {
            "当前item的viewDataBinding不存在/2!"
        }
        if(emptyMode){
            emptyViewWrapperDataBinding = binding as? ItemEmptyWrapperBinding
            (binding!!.root as? FrameLayout)?.apply {
                customEmptyViewDataBinding = getCustomEmptyView(parent.context, this)
                customEmptyViewDataBinding?.root?.let {
                    addView(it)
                }
            }
        }
        val myViewHolder = MyViewHolder<D>(binding!!)
        setListenerForItem(myViewHolder)
        return myViewHolder
    }

    private fun setListenerForItem(myViewHolder: MyViewHolder<D>) {
        itemClickListener?.let {
            val binding = myViewHolder.binding
            binding.root.setOnClickListener { itemView->
                val position = myViewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val guessedBinding = if(emptyMode&&(binding is ItemEmptyWrapperBinding)){
                        customEmptyViewDataBinding?:emptyViewWrapperDataBinding
                    }else{
                        binding
                    }?:return@setOnClickListener
                    it.invoke(guessedBinding, position)
                }
            }
        }
        itemChildClickListener?.let {
            clickableIdsInsideItem.forEach { clickableViewId->
                val itemBinding = myViewHolder.binding
                itemBinding.root.findViewById<View>(clickableViewId)?.setOnClickListener {view->
                    val position = myViewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        it.invoke(itemBinding, view, position)
                    }
                }
            }
        }
    }

    open fun getCustomEmptyView(context: Context, parent: FrameLayout):ViewDataBinding?{
        return null
    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        emptyMode = data.isEmpty()
        this.recyclerView = recyclerView
        this.context = recyclerView.context
        registerAdapterDataObserver(dataObserver)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
        unregisterAdapterDataObserver(dataObserver)
    }
    override fun onBindViewHolder(holder: MyViewHolder<D>, position: Int) {
        if(!emptyMode){
            holder.bind(data[position])
            bind(holder, position)
        }else{
            holder.bind(emptyData as D)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder<D>, position: Int, payloads: MutableList<Any>) {
        bind(holder, position, payloads)
    }

    override fun getItemCount(): Int {
        return if(emptyMode){
            1
        }else{
            data.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(!emptyMode){
            val type = data[position].type
            if(!itemsTypeAndBindingClass.containsKey(type)) {
                throw Exception("item的type:${type}没有对应的viewDataBinding!")
            }else if(type==emptyViewWrapperTypeAndBindingClass.first){
                throw Exception("item的type与内置空视图type相同,空视图type:${type}!")
            }
            type
        }else{
            emptyViewWrapperTypeAndBindingClass.first
        }
    }

    abstract fun bind(holder: MyViewHolder<D>, position: Int)

    open fun bind(holder: MyViewHolder<D>, position: Int, payloads:List<Any>) {
        onBindViewHolder(holder, position)
    }

    abstract fun initItems(itemsTypeAndBindingClass: MutableMap<Int, KClass<*>>)

    fun navigationTo(destination:Int, args:Bundle?=null, navOptions: NavOptions?=null){
        (context as? Page<*, *, *>)?.navController()?.navigate(destination, args, navOptions)
    }

    override fun <T : Any> load(appCompatImageView: AppCompatImageView, t: T?) {

    }
}