"""Writes the launcher icon, the notification icon and the small XML resources.

python tools/icon.py

The icon is the meteorologist's station mark (a cloud circle half filled, a wind barb)
in white over the polar night, with a band of aurora.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"


def path(fill, d, extra=""):
    return f'        <path android:fillColor="{fill}" {extra}android:pathData="{d}" />'


def stroke(color, d, w):
    return f'        <path android:strokeColor="{color}" android:strokeWidth="{w}" android:strokeLineCap="round" android:pathData="{d}" />'


def mark(color, w=4.2):
    # Circle at (60,60) r 12; the barb runs up-left to (33,33) with two full feathers and a half.
    ring = "M48,60a12,12 0,1 0,24,0a12,12 0,1 0,-24,0"
    half = "M60,48a12,12 0,0 1,0,24z"
    return "\n".join([
        path(color, half),
        stroke(color, ring, w),
        stroke(color, "M51.5,51.5L31,31", w),
        stroke(color, "M31,31L37,22", w),
        stroke(color, "M35.5,35.5L41.5,26.5", w),
        stroke(color, "M40,40L43,35.5", w),
    ])


def vector(w, vw, body):
    return (f'<?xml version="1.0" encoding="utf-8"?>\n'
            f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{w}dp" android:height="{w}dp" android:viewportWidth="{vw}" android:viewportHeight="{vw}">\n'
            f'{body}\n</vector>\n')


def main():
    for d in ("drawable", "mipmap-anydpi-v26", "xml", "values"):
        (RES / d).mkdir(parents=True, exist_ok=True)
    fg = f'    <group android:translateX="-6" android:translateY="-4">\n{mark("#FFF7FAFC")}\n    </group>'
    (RES / "drawable/ic_launcher_foreground.xml").write_text(vector(108, 108, fg), encoding="utf-8")
    bg = "\n".join([
        path("#0B1830", "M0,0h108v108h-108z"),
        path("#1D3A63", "M0,66C30,58 70,62 108,52V108H0Z"),
        path("#6CEFB4", "M0,40C28,30 58,44 108,26V36C60,52 30,40 0,50Z", 'android:fillAlpha="0.35" '),
        path("#6CEFB4", "M0,30C34,22 60,34 108,16V22C62,38 32,28 0,38Z", 'android:fillAlpha="0.2" '),
        path("#E4ECF5", "M0,84C30,76 72,80 108,72V108H0Z"),
        path("#D2452B", "M72,78h16v-8l-8,-6l-8,6z"),
    ])
    (RES / "drawable/ic_launcher_background.xml").write_text(vector(108, 108, bg), encoding="utf-8")
    mono = f'    <group android:translateX="-6" android:translateY="-4">\n{mark("#FFFFFFFF")}\n    </group>'
    (RES / "drawable/ic_launcher_monochrome.xml").write_text(vector(108, 108, mono), encoding="utf-8")
    notify = f'    <group android:translateX="-4.5" android:translateY="-4.5" android:scaleX="0.4" android:scaleY="0.4">\n{mark("#FFFFFFFF", 6)}\n    </group>'
    (RES / "drawable/ic_notify.xml").write_text(vector(24, 24, notify), encoding="utf-8")
    (RES / "mipmap-anydpi-v26/ic_launcher.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        '</adaptive-icon>\n', encoding="utf-8")
    (RES / "xml/files.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<paths>\n    <cache-path name="share" path="share/" />\n</paths>\n', encoding="utf-8")
    (RES / "values/colors.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="window">#0A1322</color>\n    <color name="accent">#FF8152</color>\n</resources>\n',
        encoding="utf-8")
    (RES / "values/themes.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
        '    <style name="Theme.Overwinter" parent="Theme.AppCompat.NoActionBar">\n'
        '        <item name="android:windowBackground">@color/window</item>\n'
        '        <item name="android:statusBarColor">@android:color/transparent</item>\n'
        '        <item name="android:navigationBarColor">@android:color/transparent</item>\n'
        '        <item name="colorPrimary">@color/accent</item>\n'
        '        <item name="colorAccent">@color/accent</item>\n'
        '    </style>\n</resources>\n', encoding="utf-8")


if __name__ == "__main__":
    main()
