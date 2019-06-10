# Simple Swipe Menu Layout - SSML
Swipeable layout for Android

## 0. Preview

![Imgur](https://i.imgur.com/9Ny55DA.jpg)
![Imgur](https://i.imgur.com/2jas6aK.gif)
![Imgur](https://i.imgur.com/pVMGYjD.jpg)

## 1. Dependency

```
implementation 'io.sulek.ssml:simpleswipemenulayout:1.0.4'
```
```
repositories {
    maven {
        url  "https://dl.bintray.com/sulo61/Android"
    }
}
```

## 2. Usage

### XML

```
<app:io.sulek.ssml.SimpleSwipeMenuLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/swipeContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:dynamicMenuWidth="false"
        app:menuSide="Right">

    <LinearLayout
            android:id="@+id/backgroundContainer"
            android:background="#BBBBBB"
            android:layout_width="200dp"
            android:layout_height="0dp">

    <FrameLayout
            android:id="@+id/foregroundContainer"
            android:background="#EEEEEE"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>

</app:io.sulek.ssml.SimpleSwipeMenuLayout>
```

#### important:
- all layouts (main SimpleSwipeMenuLayout, foreground container and background container) must have IDs
- background container should have 0dp height
- choose menu side: app:menuSide="Left|Right"
- app:dynamicMenuWidth="true|false"
    - false use for better performance, background container width must be specified (like in example 200dp)
    - true use for dynamic menu width, when background container has wrap_content width


### Recycler View

```
recyclerView.layoutManager = SSMLLinearLayoutManager(context)
```


### Store and restore state

#### Keep state in model
```
data class SampleData(..., var isExpanded: Boolean = false)
```

#### Listen for changes
```
itemView.swipeContainer.setOnSwipeListener(object : OnSwipeListener {
    override fun onSwipe(isExpanded: Boolean) {
        sampleData.isExpanded = isExpanded
    }
})
```

### Apply
```
// at the end of binding data function, this function perform some measurements
itemView.swipeContainer.apply(sampleData.isExpanded)
```

## 3. Licence

```
Copyright 2019 Michał Sułek

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
