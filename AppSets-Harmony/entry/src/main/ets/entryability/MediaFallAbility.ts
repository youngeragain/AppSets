import UIAbility from '@ohos.app.ability.UIAbility';
import hilog from '@ohos.hilog';
import window from '@ohos.window';

export default class MediaFallAbility extends UIAbility {
  TAG:string = "MediaFallAbility"
  onCreate(want, launchParam) {
    hilog.info(0x0000, this.TAG, '%{public}s', 'onCreate');
  }

  onDestroy() {
    hilog.info(0x0000, this.TAG, '%{public}s', 'onDestroy');
  }

  onWindowStageCreate(windowStage: window.WindowStage) {
    // Main window is created, set main page for this ability
    hilog.info(0x0000, this.TAG, '%{public}s', 'onWindowStageCreate');

    windowStage.loadContent('pages/media_fall/MediaFallPage', (err, data) => {
      if (err.code) {
        hilog.error(0x0000, this.TAG, 'Failed to load the content. Cause: %{public}s', JSON.stringify(err) ?? '');
        return;
      }
      hilog.info(0x0000, this.TAG, 'Succeeded in loading the content. Data: %{public}s', JSON.stringify(data) ?? '');
    });
  }

  onWindowStageDestroy() {
    // Main window is destroyed, release UI related resources
    hilog.info(0x0000, this.TAG, '%{public}s', 'onWindowStageDestroy');
  }

  onForeground() {
    // Ability has brought to foreground
    hilog.info(0x0000, this.TAG, '%{public}s', 'onForeground');
  }

  onBackground() {
    // Ability has back to background
    hilog.info(0x0000, this.TAG, '%{public}s', 'onBackground');
  }
};