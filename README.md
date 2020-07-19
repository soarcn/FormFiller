FormFiller
=========
![Maven](https://maven-badges.herokuapp.com/maven-central/com.cocosw/formfiller/badge.png?style=plastic)

Android helper library to populate form fields with predefined data set

![Sample](https://github.com/soarcn/FormFiller/blob/master/arts/formfiller.gif?raw=true)


Usage
=======

```kotlin
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable FormFiller for debug builds only
        if (BuildConfig.DEBUG) {
            FormFiller.Builder(this)
                // Fill the form by pressing f key from external keyboard    
                .keyCode(KeyEvent.KEYCODE_F)
                // Fill the form by double tapping on ui    
                .doubleTap()
                .scenario {
                    id(R.id.username, "username")
                    id(R.id.password, "password")
                }
                .build()
        }
    }
}
```

Advanced usage
=======

Define different data-set and switch between them inside the app

```kotlin
            FormFiller.Builder(this)
                .doubleTap()
                // Enable scenario switcher and open ui by long pressing with 2 fingers on ui
                .enableScenariosSwitcher()
                .scenario {
                    id(R.id.username, "username")
                    id(R.id.password, "password")
                }
                .scenario("Unhappy") {
                    id(R.id.username, "wrong")
                    id(R.id.password, "wrong")
                }
                .build()
```


```kotlin
//select edittext by tag name
tag("username") {
   //manipulate edit text
   it.setText(Random.toString())
}
```

Download
=====
```groovy
    implementation 'com.cocosw:formfiller:1.0'
```