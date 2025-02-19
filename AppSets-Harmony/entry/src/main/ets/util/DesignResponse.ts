interface IResponseStatus {
  success(): boolean
}

export class DesignResponse<D> implements IResponseStatus {
  code: number = 0
  info: string | null = null
  data: D | null = null

  success(): boolean {
    return this.code == 0
  }
}