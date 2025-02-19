import { DesignResponse } from './DesignResponse';

interface UserApi {

  login(body: Map<string, any>): DesignResponse<String>

  login2(body: Map<string, any>): DesignResponse<String>
}