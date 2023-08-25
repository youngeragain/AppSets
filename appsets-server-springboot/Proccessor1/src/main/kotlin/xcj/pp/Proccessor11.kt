package xcj.pp

import xcj.pp.annotations.Name
import jakarta.annotation.processing.AbstractProcessor
import jakarta.annotation.processing.Messager
import jakarta.annotation.processing.ProcessingEnvironment
import jakarta.annotation.processing.RoundEnvironment
import jakarta.lang.model.SourceVersion
import jakarta.lang.model.element.TypeElement
import jakarta.tools.Diagnostic

class Proccessor11:AbstractProcessor() {
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