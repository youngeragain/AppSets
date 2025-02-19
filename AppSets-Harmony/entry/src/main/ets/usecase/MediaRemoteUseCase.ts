import UseCase from './UseCase';
import media from '@ohos.multimedia.media';
import uri from '@ohos.uri';
import List from '@ohos.util.List';
import { faceDetector } from '@kit.CoreVisionKit';


class MediaMetadata {
  title: string | null
  artist: string | null
}

const DEFAULT_EMPTY_UUID = "00000000000000000000"
const STR_EMPTY = ""

function isStringNullOrEmpty(str: string): Boolean {
  if (str == null) {
    return true
  }
  if (str.length == 0) {
    return true
  }
  if (str.trim().length == 0) {
    return true
  }
  return false
}

export class AudioPlayerState {
  playbackState: number = null
  mediaMetadata: MediaMetadata | null = null
  id: string = DEFAULT_EMPTY_UUID
  duration: string = STR_EMPTY
  durationRawValue: number = 0
  currentDuration: string = STR_EMPTY
  currentDurationRawValue: number = 0
  defaultOrder: number = 0

  title(): string {
    if (isStringNullOrEmpty(this.mediaMetadata?.title)) {
      return STR_EMPTY
    }
    return this.mediaMetadata?.title
  }


  art(): string {
    if (isStringNullOrEmpty(this.mediaMetadata?.artist)) {
      return STR_EMPTY
    }
    return this.mediaMetadata?.artist
  }
}


export default class MediaRemoteUseCase implements UseCase {
  mediaMusicItems:List<string> = new List<string>()
  avPlayer: any | null = null

  public audioPlayerState: AudioPlayerState = new AudioPlayerState()

  private isPlaying:boolean = false

  init() {
    console.log("MediaRemoteUseCase init");
    this.mediaMusicItems.add("https://appsets-2022-1258462798.cos.ap-chengdu.myqcloud.com/files-dev/m/see_you_again.mp3")
    media.createAVPlayer((error, avPlayer) => {
      if (avPlayer != null) {
        this.avPlayer = avPlayer;
        console.info('createAVPlayer success');
      } else {
        console.error(`createAVPlayer fail, error message:${error.message}`);
      }
    });
  }

  setAVPlayerCallback() {
    // seek操作结果回调函数
    this.avPlayer.on('seekDone', (seekDoneTime) => {
      console.info(`AVPlayer seek succeeded, seek time is ${seekDoneTime}`);
    })
    // error回调监听函数,当avPlayer在操作过程中出现错误时调用reset接口触发重置流程
    this.avPlayer.on('error', (err) => {
      console.error(`Invoke avPlayer failed, code is ${err.code}, message is ${err.message}`);
      this.avPlayer.reset(); // 调用reset重置资源，触发idle状态
    })
    // 状态机变化回调函数
    this.avPlayer.on('stateChange', async (state, reason) => {
      switch (state) {
        case 'idle': // 成功调用reset接口后触发该状态机上报
          console.info('AVPlayer state idle called.');
          this.avPlayer.release(); // 调用release接口销毁实例对象
          break;
        case 'initialized': // avplayer 设置播放源后触发该状态上报
          console.info('AVPlayerstate initialized called.');
          this.avPlayer.prepare().then(() => {
            console.info('AVPlayer prepare succeeded.');
          }, (err) => {
            console.error(`Invoke prepare failed, code is ${err.code}, message is ${err.message}`);
          });
          break;
        case 'prepared': // prepare调用成功后上报该状态机
          console.info('AVPlayer state prepared called.');
          this.avPlayer.play(); // 调用播放接口开始播放
          break;
        case 'playing': // play成功调用后触发该状态机上报
          console.info('AVPlayer state playing called.');
         /* if (this.count !== 0) {
            console.info('AVPlayer start to seek.');
            this.avPlayer.seek(this.avPlayer.duration); //seek到音频末尾
          } else {
            this.avPlayer.pause(); // 调用暂停接口暂停播放
          }
          this.count++;*/
          break;
        case 'paused': // pause成功调用后触发该状态机上报
          console.info('AVPlayer state paused called.');
          //this.avPlayer.play(); // 再次播放接口开始播放
          break;
        case 'completed': // 播放结束后触发该状态机上报
          console.info('AVPlayer state completed called.');
          //this.avPlayer.stop(); //调用播放结束接口
          break;
        case 'stopped': // stop接口成功调用后触发该状态机上报
          console.info('AVPlayer state stopped called.');
          //his.avPlayer.reset(); // 调用reset接口初始化avplayer状态
          break;
        case 'released':
          console.info('AVPlayer state released called.');
          break;
        default:
          console.info('AVPlayer state unknown called.');
          break;
      }
    })
  }

  async play() {
    this.isPlaying = true
    this.playWithUrl(this.mediaMusicItems.get(0))
  }

  async pause() {
    this.isPlaying = false
    this.avPlayer.pause()
  }

  async playWithUri(uri: uri.URI) {
    if (this.avPlayer == null) {
      return
    }
    this.avPlayer.url = uri.toString()
    this.setAVPlayerCallback()
    this.avPlayer.prepare()
    //this.avPlayer.play()
  }

  async playWithUrl(url: string) {
    if (this.avPlayer == null) {
      return
    }
    this.avPlayer.url = url
    this.setAVPlayerCallback()
    this.avPlayer.prepare()
    //this.avPlayer.play()
  }
}