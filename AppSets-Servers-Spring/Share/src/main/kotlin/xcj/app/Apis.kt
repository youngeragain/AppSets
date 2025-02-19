package xcj.app

interface PayApi<O> {
    fun payOder(orderNo: String): DesignResponse<O>
}