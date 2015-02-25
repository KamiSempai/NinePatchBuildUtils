NinePatchBuildUtils
==========
Utils to provide easy way of creation NinePatchDrawable from any Drawable.

## Usage
### NinePatchBuilder
As example you can convert VectorDrawable to NinePatchDrawable
```java
NinePatchBuilder ninePathBuilder = new NinePatchBuilder(resources);
ninePathBuilder.addStretchAreaX(0.65f, 0.66f);
ninePathBuilder.addStretchAreaY(0.45f, 0.46f);
ninePathBuilder.setDrawable(R.drawable.vector_state_list,
        (int) resources.getDimension(R.dimen.vector_border_width),
        (int) resources.getDimension(R.dimen.vector_border_height));
findViewById(R.id.content).setBackground(ninePathBuilder.build());
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