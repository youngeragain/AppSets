package xcj.test

import xcj.test.annotations.Name
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class PurpleProcessor: AbstractProcessor() {
    private lateinit var messager: Messager
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        annotations?.forEach{
            roundEnv?.getElementsAnnotatedWith(it)?.forEach {ele->
                messager.printMessage(Diagnostic.Kind.NOTE, "annotations.size:${ele.simpleName}")
            }

        }
        return roundEnv?.processingOver()?:false
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_11
    }
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf<String>().apply {
            add(Name::class.java.canonicalName)
        }
    }
}