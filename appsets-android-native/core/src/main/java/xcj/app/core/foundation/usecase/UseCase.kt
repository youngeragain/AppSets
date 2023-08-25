package xcj.app.core.foundation.usecase

import java.io.Closeable

interface UseCase<C : UseCaseConfig?> : Closeable {
    val config: UseCaseConfig?
}