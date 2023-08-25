package xcj.app.core.android


/**
 * 标记模块的主入口，实现模块内的自动初始化,
 * 想象一下这样的情况下：被标记该接口的Activity作为入口后，
 * 在super.onCreate前或者Activity类创建时或者其他适当时机时调用InitModule可以初始化其他可用信息，
 * 这样还可以起到一定的延迟初始化的作用
 *
 * @see ApplicationHelper.moduleInit()
 * @sample
 * class SampleActivity:AppCompatActivity(),ModuleMainEntry{
 *     init{
 *          initModule()
 *     }
 *     override fun initModule() {
 *         ModuleInitDelegate().initModule()
 *     }
 * }
 * class ModuleInitDelegate:ModuleMainEntry{
 *      override fun initModule() {
 *          ApplicationHelper.moduleInit(MODULE_PACKAGE_NAME){
 *              // do your init module logic
 *          }
 *      }
 *      companion object{
 *          const val MODULE_PACKAGE_NAME = "MODULE_PACKAGE_NAME"
 *      }
 * }
 *
 */
interface ModuleMainEntry

