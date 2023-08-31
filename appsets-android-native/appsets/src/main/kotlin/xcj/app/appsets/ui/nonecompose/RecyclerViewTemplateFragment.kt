package xcj.app.appsets.ui.nonecompose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import xcj.app.appsets.databinding.FragmentRecyclerviewTemplateBinding
import xcj.app.appsets.databinding.ItemLevel0Binding
import xcj.app.appsets.databinding.ItemLevel1Binding
import xcj.app.appsets.databinding.ItemLevel2Binding
import xcj.app.appsets.databinding.ItemLevel3Binding
import xcj.app.appsets.ui.nonecompose.base.BaseAdapter
import xcj.app.appsets.ui.nonecompose.base.Diffable
import xcj.app.appsets.ui.nonecompose.base.MyViewHolder
import xcj.app.core.foundation.Group
import kotlin.reflect.KClass

class RecyclerViewTemplateFragment : Fragment() {

    companion object {
        fun newInstance() = RecyclerViewTemplateFragment()
    }
    private val TAG = "RecyclerViewTemplateFragment"

    private lateinit var viewModel: RecyclerViewTemplateViewModel
    private lateinit var binding:FragmentRecyclerviewTemplateBinding
    private lateinit var myAdapter:MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[RecyclerViewTemplateViewModel::class.java]
        myAdapter = MyAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecyclerviewTemplateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            val gridLayoutManager = GridLayoutManager(requireContext(), 4)
            gridLayoutManager.spanSizeLookup = object :GridLayoutManager.SpanSizeLookup(){
                override fun getSpanSize(position: Int): Int {
                    return when (myAdapter.data[position].type) {
                        MyAdapter.TYPE_LEVEL_0-> 4
                        MyAdapter.TYPE_LEVEL_1-> 3
                        MyAdapter.TYPE_LEVEL_2-> 2
                        MyAdapter.TYPE_LEVEL_3-> 1
                        else -> 4
                    }
                }
            }
            layoutManager = gridLayoutManager
            adapter = myAdapter
        }
        myAdapter.itemClickListener = {itemBinding, position ->
            val item = myAdapter.data[position]
            when (item) {
                is Group<*> -> {
                    item.expand = !item.expand
                    if(item.expand){
                        myAdapter.expand(position, item)
                    }else{
                        myAdapter.fold(position, item)
                    }
                }
                else -> {

                }
            }
        }
        getMockData().let {
            myAdapter.data.addAll(it)
            myAdapter.notifyDataSetChanged()
        }
    }

    private fun getMockData():List<Diffable> {
        val level3List_1 = mutableListOf<Level3>()
        repeat(4){
            level3List_1.add(Level3(it.toString(), MyAdapter.TYPE_LEVEL_3))
        }
        val level3List_2 = mutableListOf<Level3>()
        repeat(4){
            level3List_2.add(Level3(it.toString(), MyAdapter.TYPE_LEVEL_3))
        }
        val level2List_1 = mutableListOf<Level2>()
        repeat(11){
            when (it) {
                9 -> {
                    val element = Level2(it.toString(), MyAdapter.TYPE_LEVEL_2)
                    element.subItems = level3List_1
                    level2List_1.add(element)
                }
                7 -> {
                    val element = Level2(it.toString(), MyAdapter.TYPE_LEVEL_2)
                    element.subItems = level3List_2
                    level2List_1.add(element)
                }
                else -> {
                    level2List_1.add(Level2(it.toString(), MyAdapter.TYPE_LEVEL_2))
                }
            }

        }
        val level1List_1 = mutableListOf<Level1>()
        repeat(6){
            if(it==4){
                val element = Level1(it.toString(), MyAdapter.TYPE_LEVEL_1)
                element.subItems = level2List_1
                level1List_1.add(element)
            }else{
                level1List_1.add(Level1(it.toString(), MyAdapter.TYPE_LEVEL_1))
            }

        }
        val level0 = Level0("level0", MyAdapter.TYPE_LEVEL_0)
        level0.subItems = level1List_1
        val allItems = mutableListOf<Diffable>()
        addB(allItems, level0)
        val gson = Gson()
        Log.e(TAG, gson.toJson(allItems))
        return allItems
    }

    fun addB(list:MutableList<Diffable>, item:Any?){
        if(item is Diffable){
            list.add(item)
        }
        if(item is Group<*>){
            if(item.subItems!=null){
                for (i in item.subItems!!){
                    addB(list, i as? Group<*>)
                }
            }
        }
    }


    class Level3(val name: String, override val type: Int) : Diffable

    class Level2(override var name: String, override val type: Int) : Group<Level3>(), Diffable

    class Level1(override var name: String, override val type: Int) : Group<Level2>(), Diffable

    class Level0(override var name: String, override val type: Int) : Group<Level1>(), Diffable


    class MyAdapter : BaseAdapter<Diffable>() {
        override fun bind(holder: MyViewHolder<Diffable>, position: Int) {
            when (data[position].type) {
                TYPE_LEVEL_0 -> {}
                TYPE_LEVEL_1 -> {}
                TYPE_LEVEL_2 -> {}
                TYPE_LEVEL_3 -> {}
            }
        }

        override fun initItems(itemsTypeAndBindingClass: MutableMap<Int, KClass<*>>) {
            itemsTypeAndBindingClass[TYPE_LEVEL_0] = ItemLevel0Binding::class
            itemsTypeAndBindingClass[TYPE_LEVEL_1] = ItemLevel1Binding::class
            itemsTypeAndBindingClass[TYPE_LEVEL_2] = ItemLevel2Binding::class
            itemsTypeAndBindingClass[TYPE_LEVEL_3] = ItemLevel3Binding::class
        }



        fun fold(position: Int, item: Group<*>) {
            if(item.subItems.isNullOrEmpty())
                return
            for(i in position+1 until position+1+item.subItems!!.size){
                data.removeAt(i)
            }
            notifyItemRangeRemoved(position+1, item.subItems!!.size)
        }

        fun expand(position: Int, item: Group<*>) {
            if(item.subItems.isNullOrEmpty())
                return
            for(i in position+1 until position+1+ item.subItems!!.size){
                data.addAll(i, item.subItems as List<Diffable>)
            }
            notifyItemRangeInserted(position+1, item.subItems!!.size)
        }

        companion object{
            const val TYPE_LEVEL_0 = 0
            const val TYPE_LEVEL_1 = 1
            const val TYPE_LEVEL_2 = 2
            const val TYPE_LEVEL_3 = 3
        }
    }

}
