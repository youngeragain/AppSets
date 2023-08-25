package xcj.app.appsets.ui.nonecompose.ui.login

import androidx.recyclerview.widget.LinearLayoutManager
import xcj.app.appsets.R
import xcj.app.appsets.databinding.FragmentFirstUseBinding
import xcj.app.appsets.databinding.ItemPermissionBinding
import xcj.app.appsets.ui.nonecompose.base.BaseAdapter
import xcj.app.appsets.ui.nonecompose.base.BaseFragment
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.appsets.ui.nonecompose.base.CommonViewModel
import xcj.app.appsets.ui.nonecompose.base.Diffable
import xcj.app.appsets.ui.nonecompose.base.MyViewHolder
import xcj.app.appsets.ui.nonecompose.base.Page
import kotlin.reflect.KClass

class FirstUseFragment :
    BaseFragment<FragmentFirstUseBinding, CommonViewModel, BaseViewModelFactory<CommonViewModel>>() {
    lateinit var mAdapter: BaseAdapter<PermissionUsage>
    fun initView() {
        binding?.apply {
            tvConfirm.setOnClickListener {
                (requireActivity() as? Page<*, *, *>)?.navController()
                    ?.navigate(R.id.outSideFragment)
            }
            rvPermissions.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@FirstUseFragment.getAdapter().also {
                    mAdapter = it
                }.apply {
                    data.addAll(getPermissionsUsage())
                    notifyDataSetChanged()
                }
            }
        }
    }


    fun getPermissionsUsage(): List<PermissionUsage> {
        val list = mutableListOf<PermissionUsage>().apply {
            add(PermissionUsage("相机", "使用相机"))
            add(PermissionUsage("电话", "使用电话"))
            add(PermissionUsage("网络", "使用网络"))
            add(PermissionUsage("录音", "使用录音"))
            add(PermissionUsage("录音", "使用录音"))
            add(PermissionUsage("录音", "使用录音"))
            add(PermissionUsage("录音", "使用录音"))
            add(PermissionUsage("录音", "使用录音"))
        }
        return list
    }

    fun getAdapter(): BaseAdapter<PermissionUsage> {
        return PermissionUsageAdapter()
    }

    data class PermissionUsage(
        var name: String,
        var explain: String = "",
        val granted: Boolean = false,
        override var type: Int = 0
    ) :
        Diffable

    class PermissionUsageAdapter : BaseAdapter<PermissionUsage>({

    }) {

        override fun bind(holder: MyViewHolder<PermissionUsage>, position: Int) {
            (holder.binding as? ItemPermissionBinding)?.apply {
                tvPermissionName.text = holder.data?.name
                tvPermissionExp.text = holder.data?.explain
            }
        }

        override fun initItems(itemsTypeAndBindingClass: MutableMap<Int, KClass<*>>) {
            itemsTypeAndBindingClass[0] = ItemPermissionBinding::class
        }
    }
}