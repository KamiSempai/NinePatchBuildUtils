NinePatchBuildUtils
==========
Utility that provide easy creation of NinePatchDrawable from any Drawable.

## Usage
### NinePatchBuilder
As example you can convert VectorDrawable to NinePatchDrawable
```java
NinePatchBuilder ninePatchBuilder = new NinePatchBuilder(resources)
    .addStretchSegmentX(0.65f, 0.66f)
    .addStretchSegmentY(0.45f, 0.46f)
    .setDrawable(R.drawable.vector_state_list,
            (int) resources.getDimension(R.dimen.vector_border_width),
            (int) resources.getDimension(R.dimen.vector_border_height));
findViewById(R.id.content).setBackground(ninePatchBuilder.build());
```

### NinePatchInflater
Use NinePatchInflater to inflate NinePatchDrawable from XML
```xml
<nine-patch-plus
    xmlns:auto="http://schemas.android.com/apk/res-auto"
    auto:src="@drawable/vector_state_list"
    auto:width="@dimen/vector_border_width"
    auto:height="@dimen/vector_border_height"
    auto:stretchX="0.65, 0.66"
    auto:stretchY="0.45, 0.46" />
```
Usage in code
```java
Drawable drawable = NinePatchInflater.inflate(resources, R.drawable.vector_state_list_nine_patch);
```

## License
```
Copyright © 2017 Denis Shurygin. All rights reserved.
Contacts: <mail@pocketbyte.ru>

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