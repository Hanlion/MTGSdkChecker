# MTGSdkChecker
用于检测mintegral sdk的准确性。


# 集成
## 1.在项目的build.gradle里加入 
   `apply plugin : 'com.mintegral.sdkchecker'`  
   
## 2.在app或者需要的module的build.gradle里加入
   ```
      apply plugin : 'com.mintegral.sdkchecker'

      mintegralSdkType {
          type = 'appwall'
          area = 'oversea'
      }
      ```
