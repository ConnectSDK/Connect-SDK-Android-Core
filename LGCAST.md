LG Cast
====================
LG Cast는 Connect SDK의 WebOSTVService에서 추가적으로 제공되는 기능으로   
홈네트워크 환경에서 모바일 앱과 LG TV간의 화면 공유 기능을 제공합니다.   
TV에 화면을 출력하는 방법으로는 아래와 같이 두 가지 방법이 제공됩니다.  
   
+ 화면 미러링 - 앱 화면 전체를 TV에 출력하는 방법 입니다. 
+ 듀얼 스크린 - 앱의 화면과 별개의 도로 은 그대로 두고 두 번째 화면을 생성하여 TV에 출력하는 방법 입니다. 
<br>


지원 OS 버전 
------------------
Android 플랫폼에서 LG Cast SDK를 사용하기 위해서는 다음과 같은 사양이 요구됩니다.   
지원되지 않는 OS 버전의 경우, 알림 팝업 등의 사용을 제한하는 적절한 기능을 추가하여야 합니다. 
   
+	Android Version 10(Q) 이상
+	Android API 레벨 29 이상
<br>


외부 라이브러리  
------------------
LG Cast는 LGPL (Lesser General Public License) 라이센스를 갖는 GStreamer 오픈소스 멀티미디어 프레임워크를 사용합니다.   
GStreamer는 libgstreamer_android.so Shared Object에 포함되어 동적으로 로딩됩니다.   
아래의 링크에서 libgstreamer_android.so 코드를 다운받아 다시 빌드할 수 있습니다.   
https://github.com/ConnectSDK/Connect-SDK-Android-Core/blob/master/jniLibs/libgstreamer_android.tar   
<br>


LG Cast SDK 설정 
------------------
LG Cast SDK는 다음과 같이 Android Studio의 Project gradle에 의존성을 설정하여 사용하여야 합니다.   
현재 제공되는 최선버전은 https://github.com/ConnectSDK/Connect-SDK-Android에서 Release된 내역을 확인하십시오. 

```gradle
dependencies {
    //.....
    compile 'com.connectsdk:connect-sdk-android:1.x.x'
}
```
<br>


퍼미션 
------------------
LG Cast SDK에 적용되어 있는 퍼미션은 다음과 같습니다. 이중 RECORD_AUDIO은 런타임에서   
사용자 동의를 득하는 절차를 거쳐야 하며, 자세한 사항은 아래 설명을 참고하여 주십시오.   

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
```
<br>


스크린 미러링 
------------------
스크린 미러링은 앱 전체의 화면과 오디오를 TV로 출력할 수 있습니다.  
이의 실행은 다음과 같은 순서로 진행합니다.  
<br>

#### 1. Android 버전 확인
LG Cast는 Android 10이상에서만 구동되기 때문에 앱 시작 시 OS 버전을 확인하여야 합니다.  
만약 지원 가능한 OS가 아닌 경우 팝업 등의 사용자 가이드를 제공하고, 사용을 제한하여야 합니다.  

```java
if (ScreenMirroringHelper.isOsCompatible() == false) {
    Toast.makeText(this, "This OS can't run LG Cast.", Toast.LENGTH_LONG).show();
    finish();
}
```
<br>


#### 2. TV 검색   
앱 실행 시 우선적으로 LG Cast를 지원하는 TV를 검색하여야 합니다.   
TV는 동일 네트워크에 연결되어 있어야 하며, LG Cast를 지원하는 TV만을 검색하기 위해서는   
다음과 같이 Capability 필터를 설정합니다.  

```java
List<String> capabilities = new ArrayList<>();
capabilities.add(LGCastControl.ScreenMirroring);
CapabilityFilter filter = new CapabilityFilter(capabilities);

DiscoveryManager.init(this);
DiscoveryManager.getInstance().setPairingLevel(DiscoveryManager.PairingLevel.ON);
DiscoveryManager.getInstance().setCapabilityFilters(filter);
DiscoveryManager.getInstance().start();
```
<br>


#### 3. 퍼미션 확인   
스크린 미러링을 위해서는 오디오 사용권한을 필요로 합니다.   
미러링 시작 시 오디오 사용권한을 확인하고, 없는 경우 퍼미션 동의 절차를 진행합니다.   
퍼미션 동의는 최초 실행 시 한번만 진행됩니다. 

```java
public void onClickStartMirroring(View v) {
    if (hasCapturePermission())
        chooseDevice(); // Do next step
    else
        requestCapturePermission();
}

private boolean hasCapturePermission() {
    return checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
}

private void requestCapturePermission() {
    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 0x100);
}

@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == 0x100) {
        if (hasCapturePermission())
            chooseDevice(); // Do next step
        else
            Toast.makeText(this, "Allow to record audio to use screen mirroring", Toast.LENGTH_LONG).show();
    }
}
```
<br>


#### 4. TV 선택 
검색된 TV 목록을 출력하고 스크린을 미러링할 TV를 선택합니다.   
TV 목록은 기 정의된 Dialog로 제공되며, LG Cast API 접근을 위해 TV 선택 시   
WebOS 서비스 객체를 멤버변수로 저장하여야 합니다. 

```java
AdapterView.OnItemClickListener listener = (adapter, parent, position, id) -> {
    ConnectableDevice device = (ConnectableDevice) adapter.getItemAtPosition(position);
    mWebOSTVService = (WebOSTVService) device.getServiceByName(WebOSTVService.ID);
    requestCaptureConsent(); // Do next step
};

AlertDialog dialog = new DevicePicker(this).getPickerDialog("Select TV", listener);
dialog.show();
```
<br>


#### 5. Screen Capture에 대한 사용자 승인 
화면을 캡쳐하기 위해서는 사용자 승인을 득하여야 한다.   
사용자 승인을 위한 고지 내용은 시스템 팝업으로 정의되어 있고 MediaProjectionManager를 통해 호출하여야 합니다.   
스크린 캡쳐 동의 팝업 호출을 위한 절차는 다음과 같습니다.   
동의 시 Intent 데이터를 LG Cast API로 전달하여야 합니다.

```java
private void requestCaptureConsent() {    
    MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    startActivityForResult(projectionManager.createScreenCaptureIntent(), 0x200);
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);    

    if (requestCode == 0x200 && resultCode == RESULT_OK) 
        startMirroring(data);    
}
```
<br>


#### 6. 미러링 시작 
TV 디바이스 선택 시 저장된 mWebOSTVService의 startScreenMirroring API를 호출하여 미러링을 시작합니다.   
미러링 요청 후 호출되는 리스너 함수는 다음과 같습니다.

+ onPairing
– TV에 최초 연결 시 Paring 요청이 있을 경우 호출됩니다.
- Paring 요청이 있을 경우 이를 사용자에게 고지하고 Paring이 완료될 때까지 대기가 필요합니다.  
+ onSuccess 
– 미러링이 성공한 경우 호출됩니다.   
+ onError 
– 미러링이 실패하거나 실행 중 비정상적으로 종료된 경우 호출됩니다.   
```java
ProgressDialog progress = new ProgressDialog(this);
progress.setMessage("TV에 연결하는 중...");
progress.show();

AlertDialog pairingAlert = new AlertDialog.Builder(this)
        .setTitle("알림")
        .setCancelable(false)
        .setMessage("TV에서 페어링 요청을 수락해주세요.")
        .setNegativeButton(android.R.string.ok, null)
        .create();

mWebOSTVService.startScreenMirroring(this, projectionData, new LGCastControl.ScreenMirroringStartListener() {
    @Override
    public void onPairing() {
        pairingAlert.show();
    }

    @Override
    public void onSuccess(SecondScreen secondScreen) {
        Toast.makeText(ScreenMirroringActivity.this, "미러링을 시작합니다.", Toast.LENGTH_LONG).show();
        updateButtonVisibility();
        pairingAlert.dismiss();
        progress.dismiss();
    }

    @Override
    public void onError(ServiceCommandError error) {
        Toast.makeText(ScreenMirroringActivity.this, "미러링을 실패하였습니다.", Toast.LENGTH_LONG).show();
        updateButtonVisibility();
        pairingAlert.dismiss();
        progress.dismiss();
    }
});
```
<br>


#### 7. 미러링 종료 
미러링 종료 시는 mWebOSTVService의 stopScreenMirroring을 호출합니다.   
미러링 종료가 완료되면 onSuccess 리스너가 호출합니다.   

```java
ProgressDialog progress = new ProgressDialog(this);
progress.setMessage("TV 연결을 종료하는 중...");
progress.show();

mWebOSTVService.stopScreenMirroring(this, new LGCastControl.ScreenMirroringStopListener() {
    @Override
    public void onSuccess(String message) {
        Toast.makeText(ScreenMirroringActivity.this, "미러링을 종료하였습니다.", Toast.LENGTH_LONG).show();
        updateButtonVisibility();
        progress.dismiss();
    }

    @Override
    public void onError(ServiceCommandError error) {
        // Do nothing
    }
});

```
<br>


#### 8. 디바이스 검색 종료 
앱 종료 시 또는 디바이스 검색이 불필요한 경우 다음과 같이 디바이스 검색을 종료합니다.

```java
DiscoveryManager.getInstance().stop();
DiscoveryManager.destroy();
```
<br>


듀얼 스크린 
------------------
듀얼 스크린 기능은 앱의 화면 (First Screen)과 별개의 화면 (Second Screen)을 생성하여 이를 TV로 출력하는 기능입니다.   
기본적인 절차는 상기의 스크린 미러링과 동일하며 아래에서는 차이가 나는 부분만 별도로 설명합니다.   
<br>

#### 1. Second Screen 생성  
SecondScreen 클래스는 세컨드 스크린 생성에 필요한 기능이 추상화된 Dialog 객체입니다.   
이를 상속받아 세컨드 스크린을 자유롭게 정의합니다.   
<br>
아래는 세컨드 스크린에서 별개의 영상을 재생하는 예제입니다.

```java
public class DualSecondScreenActivity extends SecondScreen {
    private Context mContext;
    private SimpleMediaPlayer mMediaPlayer;

    public DualSecondScreenActivity(Context context, Display display) {
        super(context, display);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondscreen_activity);
        mMediaPlayer = new SimpleMediaPlayer(mContext, findViewById(R.id.ssPlayerSurface));
    }

    public void start(String url, int position) {
        mMediaPlayer.play(url, position, true);
    }

    public void stop() {
        if (mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void resume() {
        mMediaPlayer.resume();
    }
}

```
<br>


#### 2. 미러링 시작 
미러링 시작 시 위에서 정의한 세컨드 스크린 클래스 원형을 파라메터에 추가합니다.   
객체를 생성하지 않고 정의한 클래스 타입만 넘겨주어야 합니다.   
본 예제에서 정의한 세컨드 스크린 클래스는 DualSecondScreenActivity.class 입니다.   
<br>
세컨드 스크린을 미러링 성공하고 나면 생성된 세컨드 스크린 객체가 콜백으로 전달됩니다.   
이를 참조로 세컨드 스크린을 제어할 수 있습니다.   

```java
private void startMirroring(Intent projectionData) {        
    if (mWebOSTVService == null) return;

    ProgressDialog progress = new ProgressDialog(this);
    progress.setMessage("TV에 연결하는 중...");
    progress.show();

    AlertDialog pairingAlert = new AlertDialog.Builder(this)
            .setTitle("알림")
            .setCancelable(false)
            .setMessage("TV에서 페어링 요청을 수락해주세요.")
            .setNegativeButton(android.R.string.ok, null)
            .create();

    mWebOSTVService.startScreenMirroring(this, projectionData, DualSecondScreenActivity.class, new LGCastControl.ScreenMirroringStartListener() {
        @Override
        public void onPairing() {
            pairingAlert.show();
        }

        @Override
        public void onSuccess(SecondScreen secondScreen) {
            Toast.makeText(getBaseContext(), "미러링을 시작합니다.", Toast.LENGTH_LONG).show();
            updateButtonVisibility();
            pairingAlert.dismiss();
            progress.dismiss();

            if (secondScreen != null) {
                mSecondScreen = (DualSecondScreenActivity) secondScreen;
                mSecondScreen.start(mMediaPlayer.getContentUrl(), mMediaPlayer.getCurrentPosition());

                // Stop current player
                mMediaPlayer.stop();

                findViewById(R.id.fsSecondScreenPause).setVisibility(View.VISIBLE);
                findViewById(R.id.fsSecondScreenResume).setVisibility(View.GONE);
            }
        }

        @Override
        public void onError(ServiceCommandError error) {
            Toast.makeText(DualFirstScreenActivity.this, "미러링을 실패하였습니다.", Toast.LENGTH_LONG).show();                
            updateButtonVisibility();
            pairingAlert.dismiss();
            progress.dismiss();
        }
    });
} 
```
<br>


샘플 코드 
------------------
전체 샘플코드는 아래를 참고하여 주십시요.   
https://github.com/ConnectSDK/LGCast-Android-API-Sampler
<br>
