package xcj.pp

import xcj.pp.annotations.Name
import java.io.File
import java.net.URLDecoder
import jakarta.script.ScriptEngineManager
import jakarta.tools.ToolProvider
import kotlin.math.abs

@Name("you are ok?")
class Dog{
    var name:String?=null
}
@Name("yes please?")
class Cat{
    var name:String?=null
}

fun main() {
/*    val dog = Dog()
    val cat = Cat()
    println(dog.name)
    println(cat.name)
    val path1 = Thread.currentThread().contextClassLoader.getResource("//")?.path
    val path = URLDecoder.decode(path1, Charsets.UTF_8)
    val tempFile = File("$path/xcj/pp/Hello1.java")
    if(!tempFile.exists())
        tempFile.createNewFile()
    val bufferedWriter = tempFile.outputStream().bufferedWriter()
    val fileContent = "package xcj.pp;\n" +
            "public class Hello1{\n" +
            "    String name = \"hello\";\n" +
            "public void say(){\n" +
            "    System.out.println(name);" +
            "}\n" +
            "}"
    bufferedWriter.write(fileContent)
    bufferedWriter.close()
    val c = ToolProvider.getSystemJavaCompiler().run(null, null, null,
        "-encoding", "UTF-8", "-classpath",
        tempFile.absolutePath.toString(), tempFile.absolutePath)
    println("c:$c")
    val forName = Class.forName("xcj.pp.Hello1")
    val newInstance = forName.newInstance()
    val declaredField = forName.getDeclaredField("name")
    declaredField.set(newInstance, "are you set")

    val declaredMethod = forName.getDeclaredMethod("say")
    declaredMethod.invoke(newInstance)*/
    /*println(ClassLoader.getPlatformClassLoader())
    println(ClassLoader.getSystemClassLoader())
    println(Thread.currentThread().contextClassLoader)
    println(Dog::class.java.classLoader)*/
    data class Dog(val name:String="", val age:Int)
    val list = listOf<Int>(1,3,4,5,6,8,9,12,13,17,18,22,25,26, 27, 29, 41)
    val toNaturalContinuousChuckByStep = list.toNaturalContinuousChuckBy({ it }, 2 )
    println(toNaturalContinuousChuckByStep)
}

inline fun <T> List<T>.toNaturalContinuousChuckBy(selector:(T)->Int, step:Int=1):List<List<T>>{
    val ranges = mutableMapOf<Int, MutableList<T>>()
    for(i in indices){
        val t = this[i]
        if(i>1){
            var temp = i
            var key: Int
            var current: Int
            var before: Int
            while (true){
                current = selector(this[temp])
                before = selector(this[temp-1])
                if(abs(current-before) !=step){
                    key = current
                    break
                }
                temp--
            }
            if(key!=-1){
                if(ranges.containsKey(key)){
                    ranges[key]!!.add(t)
                }else{
                    ranges[key] = mutableListOf<T>().apply {
                        add(t)
                    }
                }
            }
        }else{
            val key = selector(t)
            ranges[key] = mutableListOf<T>().apply {
                add(t)
            }
        }
    }
    return ranges.map { it.value }
}