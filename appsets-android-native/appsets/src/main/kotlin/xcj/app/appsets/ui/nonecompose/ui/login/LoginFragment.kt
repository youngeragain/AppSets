package xcj.app.appsets.ui.nonecompose.ui.login

import xcj.app.appsets.databinding.FragmentLoginBinding
import xcj.app.appsets.ui.nonecompose.base.BaseFragment
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory

class LoginFragment :
    BaseFragment<FragmentLoginBinding, LoginFragmentVM, BaseViewModelFactory<LoginFragmentVM>>() {


    fun initView() {
        binding?.vm = viewModel
    }

    companion object {
        fun newInstance() = LoginFragment()
    }

}