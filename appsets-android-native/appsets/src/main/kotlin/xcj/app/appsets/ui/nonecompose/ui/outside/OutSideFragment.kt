package xcj.app.appsets.ui.nonecompose.ui.outside

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import xcj.app.appsets.R
import xcj.app.appsets.databinding.FragmentOutsideBinding
import xcj.app.appsets.databinding.ItemEmptyStyle1Binding
import xcj.app.appsets.databinding.ItemOutsideSection1Binding
import xcj.app.appsets.databinding.ItemOutsideSection2Binding
import xcj.app.appsets.databinding.ItemOutsideSection3Binding
import xcj.app.appsets.ui.nonecompose.base.BaseAdapter
import xcj.app.appsets.ui.nonecompose.base.BaseFragment
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.appsets.ui.nonecompose.base.Diffable
import xcj.app.appsets.ui.nonecompose.base.MyViewHolder
import xcj.app.core.android.toplevelfun.inflateViewDataBindingFromClass
import kotlin.reflect.KClass

class OutSideVM : BaseViewModel() {

}

class OutSideFragment :
    BaseFragment<FragmentOutsideBinding, OutSideVM, BaseViewModelFactory<OutSideVM>>() {
    lateinit var mAdapter: OutSideAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun initView() {
        binding?.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = getOutSideAdapter().also {
                    mAdapter = it
                }
            }
        }
    }

    private fun getOutSideAdapter(): OutSideAdapter {
        return OutSideAdapter().apply {
            data.apply {
                val s1 = OutSideModel(1, 0)
                val s2 = OutSideModel(2, 1)
                val s3 = OutSideModel(3, 2)
                val s4 = OutSideModel(3, 2)
                val s5 = OutSideModel(3, 0)
                val s6 = OutSideModel(3, 0)
                val s7 = OutSideModel(3, 0)
                val s8 = OutSideModel(3, 0)
                add(s1)
                add(s2)
                add(s3)
                add(s4)
                add(s5)
                add(s6)
                add(s7)
                add(s8)
            }
        }
    }

}
class OutSideAdapter: BaseAdapter<OutSideModel>({
    val rawData:MutableList<OutSideModel> = mutableListOf()
    addClickableIdsInsideItem(R.id.tv_section1_child1)
    itemClickListener = {itemBinding, position->
        when (itemBinding) {
            is ItemOutsideSection1Binding -> {
                //navigationTo(R.id.loginFragment)
                if(data.isNotEmpty()){
                    rawData.addAll(data)
                    data.clear()
                    notifyDataSetChanged()
                }
            }
            is ItemOutsideSection2Binding->{
            }
        }
        if(itemBinding is ItemEmptyStyle1Binding){
            if (data.isEmpty() && rawData.isNotEmpty()) {
                data.addAll(rawData)
                rawData.clear()
                notifyDataSetChanged()
            }
        }
    }
    itemChildClickListener = {itemBinding, clickedView, position->
        if(clickedView.id==R.id.tv_section1_child1){
            navigationTo(R.id.signupFragment)
        }
    }
}){


    override fun bind(holder: MyViewHolder<OutSideModel>, position: Int) {
        when(holder.data?.type){
            0->bindSection1(holder.binding as ItemOutsideSection1Binding)
            1->bindSection2(holder.binding as ItemOutsideSection2Binding)
            3->bindSection3(holder.binding as ItemOutsideSection3Binding)
        }
    }

    private fun bindSection1(binding: ItemOutsideSection1Binding){}
    private fun bindSection2(binding: ItemOutsideSection2Binding){}
    private fun bindSection3(binding: ItemOutsideSection3Binding){}

    override fun initItems(itemsTypeAndBindingClass: MutableMap<Int, KClass<*>>) {
        itemsTypeAndBindingClass.apply {
            put(0, ItemOutsideSection1Binding::class)
            put(1, ItemOutsideSection2Binding::class)
            put(2, ItemOutsideSection3Binding::class)
        }
    }

    override fun getCustomEmptyView(context: Context, parent: FrameLayout): ViewDataBinding? {
        return inflateViewDataBindingFromClass(
            context,
            ItemEmptyStyle1Binding::class.java,
            parent
        )
    }

    override fun <T : Any> load(appCompatImageView: AppCompatImageView, t: T?) {

    }

}
data class OutSideModel(val d:Any, override var type: Int): Diffable