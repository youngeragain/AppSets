package xcj.app.core.foundation.usecase

interface UseCaseConfig

abstract class NoConfigUseCase: UseCase<Nothing> {
    override val config: UseCaseConfig? = null
    override fun close() {

    }
}

