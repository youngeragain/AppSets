package xcj.app.appsets.service

import android.inputmethodservice.InputMethodService
import android.view.View
import xcj.app.appsets.databinding.TemplateFastInputImeBinding

class FastInputIMEService : InputMethodService() {
    lateinit var binding: TemplateFastInputImeBinding
    override fun onCreateInputView(): View {
        binding = TemplateFastInputImeBinding.inflate(layoutInflater)
        return binding.root
    }
}