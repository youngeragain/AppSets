import { DesignResponse } from './DesignResponse';

interface AppSetsApi {
  getSpotLight(): DesignResponse<SpotLight>
}

class SpotLight {
}